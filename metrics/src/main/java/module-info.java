import io.avaje.metrics.spi.SpiMetricProvider;

module io.avaje.metrics {

  requires transitive io.avaje.metrics.api;
  requires static java.management;

  uses io.avaje.metrics.spi.SpiMetricBuilder;
  provides SpiMetricProvider with io.avaje.metrics.core.DefaultMetricManager;

}
