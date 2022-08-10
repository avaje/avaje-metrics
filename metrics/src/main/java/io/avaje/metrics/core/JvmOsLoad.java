package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.LongSupplier;

final class JvmOsLoad {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  static GaugeLong osLoadAverage() {
    return new DGaugeLong("jvm.os.loadAverage", new OsLoadGauge(ManagementFactory.getOperatingSystemMXBean()));
  }

  static long toLoad(double loadAverage) {
    return BigDecimal.valueOf(loadAverage).multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).longValue();
  }

  private static final class OsLoadGauge implements LongSupplier {

    private final OperatingSystemMXBean osMXbean;

    OsLoadGauge(OperatingSystemMXBean osMXbean) {
      this.osMXbean = osMXbean;
    }

    @Override
    public long getAsLong() {
      final double loadAverage = osMXbean.getSystemLoadAverage();
      return BigDecimal.valueOf(loadAverage).multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).longValue();
    }

  }

}
