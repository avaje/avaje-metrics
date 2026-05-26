package io.avaje.metrics;

import io.avaje.metrics.stats.GaugeLongStats;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsTest {

  private final List<Metric.Statistics> suppliedMetrics = new ArrayList<>();

  @Test
  void addSupplier() {
    Metrics.collectMetrics();

    Metrics.addSupplier(() -> suppliedMetrics);
    suppliedMetrics.add(new GaugeLongStats(Metric.ID.of("supplied0"), "MiBy", 42));

    List<Metric.Statistics> result = Metrics.collectMetrics(CollectionMode.CUMULATIVE);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("supplied0");
    assertThat(result.get(0).unit()).isEqualTo("MiBy");

    suppliedMetrics.clear();

    List<Metric.Statistics> result2 = Metrics.collectMetrics(CollectionMode.CUMULATIVE);
    assertThat(result2).isEmpty();
  }

  @Test
  void metricSupplier_collectionModeDefaultDelegates() {
    MetricSupplier supplier = () -> suppliedMetrics;
    suppliedMetrics.add(new GaugeLongStats(Metric.ID.of("supplied1"), 24));

    assertThat(supplier.collectMetrics(CollectionMode.CUMULATIVE))
      .singleElement()
      .extracting(Metric.Statistics::name)
      .isEqualTo("supplied1");
  }

  @Test
  void timerBuilder_usesDefaultRegistry() {
    Metrics.collectMetrics();

    var timer = Metrics.timerBuilder("app.metrics.builder")
      .tags(Tags.of("env:test"))
      .bucketRanges(100, 200)
      .buildTraced();

    timer.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(50));

    assertThat(Metrics.collectMetrics(CollectionMode.CUMULATIVE))
      .filteredOn(it -> it.name().equals("app.metrics.builder"))
      .singleElement()
      .extracting(Metric.Statistics::id)
      .extracting(Metric.ID::tags)
      .isEqualTo(Tags.of("env:test"));
  }

  @Test
  void metricOverloadsWithTags_useDefaultRegistry() {
    var tags = Tags.of("scope:facade", "env:test");

    var counter = Metrics.counter("app.metrics.facade.counter", tags);
    var meter = Metrics.meter("app.metrics.facade.meter", tags);
    var timer = Metrics.timer("app.metrics.facade.timer", tags);

    assertThat(Metrics.counter("app.metrics.facade.counter", tags)).isSameAs(counter);
    assertThat(Metrics.meter("app.metrics.facade.meter", tags)).isSameAs(meter);
    assertThat(Metrics.timer("app.metrics.facade.timer", tags)).isSameAs(timer);
    assertThat(counter.id().tags()).isEqualTo(tags);
    assertThat(meter.id().tags()).isEqualTo(tags);
    assertThat(timer.id().tags()).isEqualTo(tags);
  }
}
