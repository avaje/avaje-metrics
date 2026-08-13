package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Tags;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.LongSupplier;

import static java.math.BigDecimal.valueOf;

final class JvmCGroupCpu {

  private static final String CPU_STAT_PATH = "/sys/fs/cgroup/cpu.stat";
  private static final String CPU_MAX_PATH = "/sys/fs/cgroup/cpu.max";
  private static final String MICROSECONDS = "us";
  private static final String MILLICORES = "mCPU";

  static void createGauges(MetricRegistry registry, boolean reportChangesOnly, Tags globalTags) {
    new JvmCGroupCpu().create(registry, reportChangesOnly, globalTags);
  }

  void create(MetricRegistry registry, boolean reportChangesOnly, Tags globalTags) {
    var cpuStat = new FileLines(CPU_STAT_PATH);
    if (!cpuStat.exists()) {
      return;
    }

    var source = new CpuStatsSource(cpuStat);
    registry.register(gauge("jvm.cgroup.cpu.usageMicros", source::usageMicros, reportChangesOnly, globalTags));
    registry.register(gauge("jvm.cgroup.cpu.userMicros", source::userMicros, reportChangesOnly, globalTags));
    registry.register(gauge("jvm.cgroup.cpu.systemMicros", source::systemMicros, reportChangesOnly, globalTags));
    registry.register(gauge("jvm.cgroup.cpu.throttledMicros", source::throttledMicros, reportChangesOnly, globalTags));
    registry.register(gauge("jvm.cgroup.cpu.periods", source::periods, reportChangesOnly, globalTags));
    registry.register(gauge("jvm.cgroup.cpu.throttledPeriods", source::throttledPeriods, reportChangesOnly, globalTags));

    var cpuMax = new FileLines(CPU_MAX_PATH);
    if (cpuMax.exists()) {
      createCGroupCpuLimit(cpuMax, globalTags).ifPresent(registry::register);
    }
  }

  Optional<GaugeLong> createCGroupCpuLimit(FileLines cpuMax, Tags globalTags) {
    return cpuMax.readLines().stream()
      .findFirst()
      .flatMap(this::parseCpuMax)
      .map(limit -> DGaugeLong.once(
        Metric.ID.of("jvm.cgroup.cpu.limitMillicores", globalTags),
        MILLICORES,
        () -> limit));
  }

  Optional<Long> parseCpuMax(String cpuMax) {
    var values = cpuMax.trim().split("\\s+");
    if (values.length != 2) {
      throw new IllegalArgumentException("Expected quota and period in cpu.max: " + cpuMax);
    }
    if ("max".equals(values[0])) {
      return Optional.empty();
    }

    var quotaMicros = Long.parseLong(values[0]);
    var periodMicros = Long.parseLong(values[1]);
    if (quotaMicros <= 0 || periodMicros <= 0) {
      throw new IllegalArgumentException("Expected positive quota and period in cpu.max: " + cpuMax);
    }
    return Optional.of(convertQuotaToMillicores(quotaMicros, periodMicros));
  }

  long convertQuotaToMillicores(long quotaMicros, long periodMicros) {
    return valueOf(quotaMicros)
      .multiply(valueOf(1000))
      .divide(valueOf(periodMicros), RoundingMode.HALF_UP)
      .longValue();
  }

  private GaugeLong gauge(String name, LongSupplier supplier, boolean reportChangesOnly, Tags globalTags) {
    var unit = name.endsWith("Micros") ? MICROSECONDS : "";
    return DGaugeLong.of(Metric.ID.of(name, globalTags), unit, supplier, reportChangesOnly);
  }

  static final class CpuStatsSource {

    private final FileLines source;

    CpuStatsSource(FileLines source) {
      this.source = source;
    }

    long usageMicros() {
      return value("usage_usec");
    }

    long userMicros() {
      return value("user_usec");
    }

    long systemMicros() {
      return value("system_usec");
    }

    long throttledMicros() {
      return value("throttled_usec");
    }

    long periods() {
      return value("nr_periods");
    }

    long throttledPeriods() {
      return value("nr_throttled");
    }

    private long value(String field) {
      for (var line : source.readLines()) {
        var values = line.split("\\s+", 2);
        if (values.length == 2 && field.equals(values[0])) {
          return Long.parseLong(values[1]);
        }
      }
      return 0;
    }
  }
}
