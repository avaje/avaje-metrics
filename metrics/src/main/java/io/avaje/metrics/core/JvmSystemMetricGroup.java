package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.GaugeLongMetric;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;

final class JvmSystemMetricGroup {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  static GaugeLongMetric getOsLoadAvgMetric() {
    GaugeLong osLoadAvg = new OsLoadGauge(ManagementFactory.getOperatingSystemMXBean());
    return new DefaultGaugeLongMetric(new DefaultMetricName("jvm.os.loadAverage"), osLoadAvg);
  }

  static long toLoad(double loadAverage) {
    return BigDecimal.valueOf(loadAverage).multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).longValue();
  }

  private static final class OsLoadGauge implements GaugeLong {

    private final OperatingSystemMXBean osMXbean;

    OsLoadGauge(OperatingSystemMXBean osMXbean) {
      this.osMXbean = osMXbean;
    }

    @Override
    public long getValue() {
      final double loadAverage = osMXbean.getSystemLoadAverage();
      return BigDecimal.valueOf(loadAverage).multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).longValue();
    }

  }

}
