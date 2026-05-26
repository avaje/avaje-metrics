package io.avaje.metrics.core;

import io.avaje.metrics.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TimerTest {

  @Test
  void add() {

    MetricRegistry registry = Metrics.createRegistry();
    Timer metric = registry.timer("org.test.mytimed");
    Timer metric2 = registry.timerBuilder("org.test.mytimed").tags(Tags.of("a", "b")).build();
    Timer metric3 = registry.timerBuilder("myBucket").tags(Tags.of("a", "b")).bucketRanges(400, 900).build();
    assertThat(metric2).isNotSameAs(metric);
    assertThat(metric3).isNotSameAs(metric);

    boolean useContext = false;
    long start = System.nanoTime();

    assertEquals("org.test.mytimed", metric.name());

    metric.add(start);//, useContext);
    metric.addEventSince(true, start);

    List<Metric.Statistics> stats = registry.collectMetrics();

    Timer.Stats stat0 = (Timer.Stats) stats.get(0);

    assertEquals("org.test.mytimed", stat0.name());
    assertEquals(2, stat0.count());
    assertThat(stat0.total()).isGreaterThan(0);
    assertThat(stat0.mean()).isGreaterThan(0);
    assertThat(stat0.max()).isGreaterThan(0);


    metric.addErr(start);//, useContext);
    metric.addEventSince(false, start);
    metric.addErr(start);//, useContext);

    stats = registry.collectMetrics();
    stat0 = (Timer.Stats) stats.get(0);

    assertEquals("org.test.mytimed.error", stat0.name());
    assertEquals(3, stat0.count());
    assertThat(stat0.total()).isGreaterThan(0);

    metric.add(start);//, useContext);
    metric.addErr(start);//, useContext);
    metric.addErr(start);//, useContext);

    stats = registry.collectMetrics();
    stat0 = (Timer.Stats) stats.get(0);
    Timer.Stats stat1 = (Timer.Stats) stats.get(1);

    assertEquals("org.test.mytimed", stat0.name());
    assertEquals(1, stat0.count());
    assertEquals("org.test.mytimed.error", stat1.name());
    assertEquals(2, stat1.count());
  }

  @Test
  void withTags() {
    var registry = Metrics.createRegistry();
    var tags = Tags.of("scope:timer", "env:test");

    var timer0 = registry.timer("api.fastPath.timer", tags);
    var timer1 = registry.timer("api.fastPath.timer", tags);
    var timer2 = registry.timer("api.fastPath.timer", Tags.of("scope:timer", "env:other"));

    assertThat(timer0).isSameAs(timer1);
    assertThat(timer0).isNotSameAs(timer2);
    assertThat(timer0.id().tags()).isEqualTo(tags);
    assertThat(timer2.id().tags()).isEqualTo(Tags.of("scope:timer", "env:other"));
  }

  @Test
  void withTags_whenBuilderCreated_expectCachedMetric() {
    var registry = Metrics.createRegistry();
    var tags = Tags.of("scope:timer", "source:builder");

    var timer = registry.timerBuilder("api.fastPath.timer.builder")
      .tags(tags)
      .build();

    assertThat(registry.timer("api.fastPath.timer.builder", tags)).isSameAs(timer);
  }

  private void resetStatistics() {
    Metrics.collectMetrics();
  }

  @Test
  void timeRunnable() {
    resetStatistics();

    Timer metric = Metrics.timer("test.runnable");
    metric.time(() -> System.out.println("here"));

    final List<Metric.Statistics> stats = Metrics.collectMetrics();
    Timer.Stats stat0 = (Timer.Stats) stats.get(0);

    assertEquals("test.runnable", stat0.name());
    assertEquals(1, stat0.count());
    assertThat(stat0.total()).isGreaterThan(0);
    assertThat(stat0.mean()).isGreaterThan(0);
    assertThat(stat0.max()).isGreaterThan(0);
  }


  private void runAndThrow() {
    System.out.println("here");
    throw new NullPointerException();
  }

  @Test
  void timeRunnable_when_error() {

    resetStatistics();

    Timer metric = Metrics.timer("test.runnable");

    try {
      metric.time(this::runAndThrow);
      fail();
    } catch (NullPointerException e) {

      final List<Metric.Statistics> stats = Metrics.collectMetrics();
      Timer.Stats stat0 = (Timer.Stats) stats.get(0);

      assertEquals("test.runnable.error", stat0.name());
      assertEquals(1, stat0.count());
      assertThat(stat0.total()).isGreaterThan(0);
      assertThat(stat0.mean()).isGreaterThan(0);
      assertThat(stat0.max()).isGreaterThan(0);
    }
  }

  private String callAndThrow() {
    System.out.println("here");
    throw new NullPointerException();
  }

  @Test
  void timeCallable_when_error() {
    resetStatistics();
    Timer metric = Metrics.timer("test.callable");
    try {
      metric.time(this::callAndThrow);
      fail();
    } catch (Exception e) {

      final List<Metric.Statistics> stats = Metrics.collectMetrics();
      Timer.Stats stat0 = (Timer.Stats) stats.get(0);

      assertEquals("test.callable.error", stat0.name());
      assertEquals(1, stat0.count());
      assertThat(stat0.total()).isGreaterThan(0);
      assertThat(stat0.mean()).isGreaterThan(0);
      assertThat(stat0.max()).isGreaterThan(0);
    }
  }

  @Test
  void timeCallable_when_success() {
    resetStatistics();
    Timer metric = Metrics.timer("test.callable");

    String out = metric.time(() -> "foo");
    assertEquals("foo", out);

    final List<Metric.Statistics> stats = Metrics.registry().collectMetrics();
    Timer.Stats stat0 = (Timer.Stats) stats.get(0);

    assertEquals("test.callable", stat0.name());
    assertEquals(1, stat0.count());
    assertThat(stat0.total()).isGreaterThan(0);
    assertThat(stat0.mean()).isGreaterThan(0);
    assertThat(stat0.max()).isGreaterThan(0);
  }

  @Test
  void collectMetrics_cumulative() {
    MetricRegistry registry = Metrics.createRegistry();
    Timer metric = registry.timer("test.timer.cumulative");

    metric.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(5));
    metric.addEventDuration(false, TimeUnit.MILLISECONDS.toNanos(2));

    List<Metric.Statistics> stats = registry.collectMetrics(CollectionMode.CUMULATIVE);
    assertThat(stats).hasSize(2);

    Timer.Stats success = (Timer.Stats) stats.get(0);
    Timer.Stats error = (Timer.Stats) stats.get(1);
    assertEquals("test.timer.cumulative", success.name());
    assertEquals(1, success.count());
    assertEquals(5_000, success.total());
    assertEquals(5_000, success.max());
    assertEquals("test.timer.cumulative.error", error.name());
    assertEquals(1, error.count());
    assertEquals(2_000, error.total());
    assertEquals(2_000, error.max());

    List<Metric.Statistics> stats2 = registry.collectMetrics(CollectionMode.CUMULATIVE);
    assertThat(stats2).hasSize(2);
    assertEquals(1, ((Timer.Stats) stats2.get(0)).count());
    assertEquals(5_000, ((Timer.Stats) stats2.get(0)).total());
    assertEquals(0, ((Timer.Stats) stats2.get(0)).max());
    assertEquals(1, ((Timer.Stats) stats2.get(1)).count());
    assertEquals(2_000, ((Timer.Stats) stats2.get(1)).total());
    assertEquals(0, ((Timer.Stats) stats2.get(1)).max());

    metric.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(3));
    metric.addEventDuration(false, TimeUnit.MILLISECONDS.toNanos(7));
    List<Metric.Statistics> stats3 = registry.collectMetrics(CollectionMode.CUMULATIVE);
    assertThat(stats3).hasSize(2);
    assertEquals(2, ((Timer.Stats) stats3.get(0)).count());
    assertEquals(8_000, ((Timer.Stats) stats3.get(0)).total());
    assertEquals(3_000, ((Timer.Stats) stats3.get(0)).max());
    assertEquals(2, ((Timer.Stats) stats3.get(1)).count());
    assertEquals(9_000, ((Timer.Stats) stats3.get(1)).total());
    assertEquals(7_000, ((Timer.Stats) stats3.get(1)).max());

    List<Metric.Statistics> stats4 = registry.collectMetrics();
    assertThat(stats4).hasSize(2);
    assertEquals(2, ((Timer.Stats) stats4.get(0)).count());
    assertEquals(8_000, ((Timer.Stats) stats4.get(0)).total());
    assertEquals(0, ((Timer.Stats) stats4.get(0)).max());
    assertEquals(2, ((Timer.Stats) stats4.get(1)).count());
    assertEquals(9_000, ((Timer.Stats) stats4.get(1)).total());
    assertEquals(0, ((Timer.Stats) stats4.get(1)).max());
    assertThat(registry.collectMetrics()).isEmpty();
  }

//
//  @Test
//  public void addEventSince() {
//
//      TimedMetric metric = MetricManager.getTimedMetric("org.test.mytimed.since");
//
//      metric.clear();
//
//      metric.addEventSince(true, System.nanoTime() - 950000);
//      ValueStatistics valueStatistics = metric.getSuccessStatistics(false);
//      assertEquals(1, valueStatistics.getCount());
//      System.out.println("Should be close to 1000: "+valueStatistics.getTotal());
//      Assert.assertTrue(valueStatistics.getTotal() > 0);
//      Assert.assertTrue(valueStatistics.getTotal() < 1000);
//      assertEquals(0, metric.getErrorStatistics(false).getCount());
//  }
//
//  @Test
//  public void startEvent() {
//
//    TimedMetric metric = MetricManager.getTimedMetric("org.test.mytimed");
//
//    metric.clearStatistics();
//    assertEquals(0, metric.getSuccessStatistics(false).getCount());
//    assertEquals(0, metric.getSuccessStatistics(false).getTotal());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getTotal());
//    assertEquals(0, metric.getSuccessStatistics(false).getMean());
//    assertEquals(0, metric.getErrorStatistics(false).getMean());
//
//    TimedEvent startEvent = metric.startEvent();
//    startEvent.endWithSuccess();
//
//    assertEquals(1, metric.getSuccessStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//
//    startEvent = metric.startEvent();
//    startEvent.endWithSuccess();
//    assertEquals(2, metric.getSuccessStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getTotal());
//
//    startEvent = metric.startEvent();
//    startEvent.endWithError();
//
//    assertEquals(2, metric.getSuccessStatistics(false).getCount());
//    assertEquals(1, metric.getErrorStatistics(false).getCount());
//
//
//    assertThat(collect(metric)).hasSize(1);
//
//    ValueStatistics collectedSuccessStatistics = metric.getCollectedSuccessStatistics();
//    ValueStatistics collectedErrorStatistics = metric.getCollectedErrorStatistics();
//
//    assertEquals(2, collectedSuccessStatistics.getCount());
//    assertEquals(1, collectedErrorStatistics.getCount());
//
//
//    assertEquals(0, metric.getSuccessStatistics(false).getCount());
//    assertEquals(0, metric.getSuccessStatistics(false).getTotal());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getTotal());
//    assertEquals(0, metric.getSuccessStatistics(false).getMean());
//    assertEquals(0, metric.getErrorStatistics(false).getMean());
//
//  }
//
//  @Test
//  public void operationEnd() {
//
//    TimedMetric metric = MetricManager.getTimedMetric("org.test.mytimed");
//
//    metric.clearStatistics();
//    assertEquals(0, metric.getSuccessStatistics(false).getCount());
//    assertEquals(0, metric.getSuccessStatistics(false).getTotal());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getTotal());
//    assertEquals(0, metric.getSuccessStatistics(false).getMean());
//    assertEquals(0, metric.getErrorStatistics(false).getMean());
//
//    int SUCCESS_OPCODE = 1;
//    int ERROR_OPCODE = 191;
//
//    metric.operationEnd(SUCCESS_OPCODE, System.nanoTime() - TimeUnit.MICROSECONDS.toNanos(1000), useContext);
//    assertEquals(1, metric.getSuccessStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//
//    metric.operationEnd(SUCCESS_OPCODE, System.nanoTime() - TimeUnit.MICROSECONDS.toNanos(2000), useContext);
//    assertEquals(2, metric.getSuccessStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getTotal());
//
//    metric.operationEnd(ERROR_OPCODE, System.nanoTime() - TimeUnit.MICROSECONDS.toNanos(5000), useContext);
//    assertEquals(2, metric.getSuccessStatistics(false).getCount());
//    assertEquals(1, metric.getErrorStatistics(false).getCount());
//
//
//
//    assertThat(collect(metric)).hasSize(1);
//
//    ValueStatistics collectedSuccessStatistics = metric.getCollectedSuccessStatistics();
//    ValueStatistics collectedErrorStatistics = metric.getCollectedErrorStatistics();
//
//    assertEquals(2, collectedSuccessStatistics.getCount());
//    Assert.assertTrue(collectedSuccessStatistics.getTotal() >= 3000);
//    assertEquals(1, collectedErrorStatistics.getCount());
//    Assert.assertTrue(collectedErrorStatistics.getTotal() >= 5000);
//
//    assertEquals(0, metric.getSuccessStatistics(false).getCount());
//    assertEquals(0, metric.getSuccessStatistics(false).getTotal());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getTotal());
//    assertEquals(0, metric.getSuccessStatistics(false).getMean());
//    assertEquals(0, metric.getErrorStatistics(false).getMean());
//
//  }
//
//  private List<Metric> collect(Metric metric) {
//    List<Metric> list = new ArrayList<>();
//    metric.collectStatistics(list);
//    return list;
//  }
}
