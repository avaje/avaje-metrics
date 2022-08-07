import io.avaje.metrics.spi.SpiMetricProvider;

module io.avaje.metrics.api {

  exports io.avaje.metrics;
  exports io.avaje.metrics.spi;
  exports io.avaje.metrics.annotation;

  uses SpiMetricProvider;
}
