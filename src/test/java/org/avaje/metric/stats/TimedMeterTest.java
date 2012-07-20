package org.avaje.metric.stats;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.MetricManager;
import org.avaje.metric.MetricStatistics;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.TimedMetricEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TimedMeterTest {

  private TimedMetric meter;

  @Before
  public void setUp() throws Exception {
    this.meter = MetricManager.getTimedMetric(TimedMeterTest.class, "things", "thing",
        TimeUnit.SECONDS);
  }

  @Test
  public void aBlankMeter() throws Exception {

    Assert.assertEquals("the meter has a count of zero", meter.getSuccessStatistics().getSummary()
        .getCount(), 0L);
    Assert.assertTrue("the meter has a mean rate of zero", meter.getSuccessStatistics()
        .getEventRate().getMeanRate() < 0.001);
  }

  @Test
  public void aMeterWithThreeEvents() throws Exception {

    meter.clearStatistics();
    meter.startEvent().endWithSuccess();
    meter.startEvent().endWithSuccess();
    meter.startEvent().endWithSuccess();
    meter.updateStatistics();

    Assert.assertEquals("the meter has a count of three", meter.getSuccessStatistics().getSummary()
        .getCount(), 3L);
    Assert.assertEquals("the meter has a error count of 0", meter.getErrorStatistics().getSummary()
        .getCount(), 0L);
  }

  @Test
  public void testSomeRandom() throws InterruptedException {

    meter.clearStatistics();

    Random random = new Random();
    for (int i = 0; i < 50; i++) {
      TimedMetricEvent startEvent = meter.startEvent();
      Thread.sleep(50 + random.nextInt(150));
      startEvent.endWithSuccess();
    }

    // make sure statistics are current, normally this is
    // left to the background timer to update the statistics
    meter.updateStatistics();
    MetricStatistics successStatistics = meter.getSuccessStatistics();
    System.out.println(successStatistics);
  }
}
