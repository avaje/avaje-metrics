package io.avaje.metrics.spi;

import io.avaje.metrics.MetricRegistry;

public interface SpiMetricProvider extends MetricRegistry {

  /**
   * Create a new registry.
   */
  MetricRegistry createRegistry();

}
