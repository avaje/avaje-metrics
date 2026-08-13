package io.avaje.metrics;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricsProviderTest {

  @Test
  void forRegistry_collectsUsingRequestedMode() {
    var registry = Metrics.createRegistry();
    registry.counter("provider.test").inc();

    var provider = MetricsProvider.forRegistry(registry);

    assertThat(provider.provide(CollectionMode.CUMULATIVE))
      .singleElement()
      .extracting(Metric.Statistics::name)
      .isEqualTo("provider.test");
  }

  @Test
  void provide_defaultsToDelta() {
    var modes = new ArrayList<CollectionMode>();
    MetricsProvider provider = mode -> {
      modes.add(mode);
      return List.of();
    };

    provider.provide();

    assertThat(modes).containsExactly(CollectionMode.DELTA);
  }

  @Test
  void forRegistry_rejectsNull() {
    assertThatThrownBy(() -> MetricsProvider.forRegistry(null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("registry");
  }
}
