module io.avaje.metrics {

  requires transitive io.avaje.metrics.api;
  requires static java.management;

  uses io.avaje.metrics.spi.SpiMetricBuilder;
  provides io.avaje.metrics.spi.SpiMetricManager with io.avaje.metrics.core.DefaultMetricManager;

}
