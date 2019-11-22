package io.avaje.metrics.core;

import io.avaje.metrics.MetricManager;
import io.avaje.metrics.TimedMetric;

public class TimedMetricTest {

  private boolean useContext = false;

  private TimedMetric _metric = MetricManager.getTimedMetric("org.test.mytimed");

  public void add() {

    boolean requestTiming = _metric.isActiveThreadContext();
    long start = System.nanoTime();

    _metric.operationEnd(13, start, requestTiming);
  }

//  @Test
//  public void addEventDuration() {
//
//    TimedMetric metric = MetricManager.getTimedMetric("org.test.mytimed");
//
//    boolean useContext = metric.isActiveThreadContext();
//    long start = System.nanoTime();
//
//    metric.operationEnd(13, start, useContext);
//
//    assertEquals("org",metric.getName().getGroup());
//    assertEquals("test",metric.getName().getType());
//    assertEquals("mytimed",metric.getName().getName());
//
//    metric.clear();
//    assertEquals(0, metric.getCount());
//    assertEquals(0, metric.getTotal());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getTotal());
//    assertEquals(0, metric.getMean());
//    assertEquals(0, metric.getErrorStatistics(false).getMean());
//
//    metric.addEventDuration(true, TimeUnit.MICROSECONDS.toNanos(1000));
//    assertEquals(1, metric.getCount());
//    assertEquals(1000, metric.getTotal());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//
//    metric.addEventDuration(true, TimeUnit.MICROSECONDS.toNanos(2000));
//    assertEquals(2, metric.getCount());
//    assertEquals(3000, metric.getTotal());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getTotal());
//
//    metric.addEventDuration(false, TimeUnit.MICROSECONDS.toNanos(5000));
//    assertEquals(2, metric.getCount());
//    assertEquals(3000, metric.getTotal());
//    assertEquals(1, metric.getErrorStatistics(false).getCount());
//    assertEquals(5000, metric.getErrorStatistics(false).getTotal());
//
//    assertEquals(1500, metric.getMean());
//    assertEquals(5000, metric.getErrorStatistics(false).getMean());
//
//
//    assertThat(collect(metric)).hasSize(1);
//
//    ValueStatistics collectedSuccessStatistics = metric.getCollectedSuccessStatistics();
//    ValueStatistics collectedErrorStatistics = metric.getCollectedErrorStatistics();
//
//    assertEquals(2, collectedSuccessStatistics.getCount());
//    assertEquals(3000, collectedSuccessStatistics.getTotal());
//    assertEquals(1, collectedErrorStatistics.getCount());
//    assertEquals(5000, collectedErrorStatistics.getTotal());
//
//    assertEquals(1500, collectedSuccessStatistics.getMean());
//    assertEquals(5000, collectedErrorStatistics.getMean());
//
//
//    assertEquals(0, metric.getCount());
//    assertEquals(0, metric.getTotal());
//    assertEquals(0, metric.getErrorStatistics(false).getCount());
//    assertEquals(0, metric.getErrorStatistics(false).getTotal());
//    assertEquals(0, metric.getMean());
//    assertEquals(0, metric.getErrorStatistics(false).getMean());
//
//  }
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
