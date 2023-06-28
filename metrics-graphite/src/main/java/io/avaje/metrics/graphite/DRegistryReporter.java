package io.avaje.metrics.graphite;

import io.avaje.metrics.MetricRegistry;

final class DRegistryReporter implements GraphiteSender.Reporter {

  private final MetricRegistry registry;

  DRegistryReporter(MetricRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void report(GraphiteSender sender) {
    sender.send(registry.collectMetrics());
  }
}
