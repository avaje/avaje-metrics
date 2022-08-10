package io.avaje.metrics.core;

import io.avaje.metrics.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TimedMetricTest {

  @Test
  void add() {

    MetricRegistry registry = Metrics.createRegistry();
    Timer metric = registry.timed("org.test.mytimed");

    boolean useContext = false;//metric.isRequestTiming();
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

    assertEquals("org.test.mytimed.error", stat0.name());
    assertEquals(2, stat0.count());

    assertEquals("org.test.mytimed", stat1.name());
    assertEquals(1, stat1.count());
  }

  private void resetStatistics() {
    Metrics.collectMetrics();
  }

  @Test
  void timeRunnable() {

    resetStatistics();

    Timer metric = Metrics.timer("test.runnable");
    metric.time(() -> {
      System.out.println("here");
    });

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

    final List<Metric.Statistics> stats = Metrics.collectMetrics();
    Timer.Stats stat0 = (Timer.Stats) stats.get(0);

    assertEquals("test.callable", stat0.name());
    assertEquals(1, stat0.count());
    assertThat(stat0.total()).isGreaterThan(0);
    assertThat(stat0.mean()).isGreaterThan(0);
    assertThat(stat0.max()).isGreaterThan(0);
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
