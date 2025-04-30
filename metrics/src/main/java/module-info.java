import io.avaje.metrics.core.DefaultMetricProvider;

module io.avaje.metrics {

  exports io.avaje.metrics;
  exports io.avaje.metrics.stats;
  exports io.avaje.metrics.spi;
  exports io.avaje.metrics.annotation;

  requires transitive io.avaje.applog;
  requires transitive org.jspecify;
  requires static java.management;
  requires static jdk.management;

  uses io.avaje.metrics.spi.SpiMetricBuilder;
  uses io.avaje.metrics.spi.SpiMetricProvider;
  provides io.avaje.metrics.spi.SpiMetricProvider with DefaultMetricProvider;
}
