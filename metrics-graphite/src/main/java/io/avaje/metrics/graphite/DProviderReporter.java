package io.avaje.metrics.graphite;

import io.avaje.metrics.CollectionMode;
import io.avaje.metrics.MetricsProvider;

import static java.util.Objects.requireNonNull;

final class DProviderReporter implements GraphiteSender.Reporter {

  private final MetricsProvider metricsProvider;

  DProviderReporter(MetricsProvider metricsProvider) {
    this.metricsProvider = requireNonNull(metricsProvider, "metricsProvider");
  }

  @Override
  public void report(GraphiteSender sender) {
    sender.send(metricsProvider.provide(CollectionMode.DELTA));
  }
}
