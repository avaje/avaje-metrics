import io.avaje.metrics.core.DefaultMetricProvider;
import io.avaje.metrics.spi.SpiMetricProvider;

module io.avaje.metrics {

  exports io.avaje.metrics;
  exports io.avaje.metrics.stats;
  exports io.avaje.metrics.spi;
  exports io.avaje.metrics.annotation;

  requires transitive io.avaje.applog;
  requires static java.management;

  uses io.avaje.metrics.spi.SpiMetricBuilder;
  uses SpiMetricProvider;
  provides SpiMetricProvider with DefaultMetricProvider;
}
