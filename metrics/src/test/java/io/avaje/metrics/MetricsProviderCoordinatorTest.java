package io.avaje.metrics;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricsProviderCoordinatorTest {

  @Test
  void delegatesToInstalledProvider() {
    var coordinator = new MetricsProviderCoordinator();
    var statistics = List.<Metric.Statistics>of();
    var requestedMode = new CollectionMode[1];
    coordinator.install(mode -> {
      requestedMode[0] = mode;
      return statistics;
    });

    assertThat(coordinator.provide(CollectionMode.CUMULATIVE)).isSameAs(statistics);
    assertThat(requestedMode[0]).isEqualTo(CollectionMode.CUMULATIVE);
  }

  @Test
  void rejectsProvideBeforeInstall() {
    var coordinator = new MetricsProviderCoordinator();

    assertThatThrownBy(coordinator::provide)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Metrics provider has not been installed");
  }

  @Test
  void rejectsDuplicateInstall() {
    var coordinator = new MetricsProviderCoordinator();
    coordinator.install(mode -> List.of());

    assertThatThrownBy(() -> coordinator.install(mode -> List.of()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Metrics provider has already been installed");
  }

  @Test
  void rejectsNullInstall() {
    var coordinator = new MetricsProviderCoordinator();

    assertThatThrownBy(() -> coordinator.install(null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("provider");
  }
}
