package org.avaje.metric.stats;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.EventMetric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.Stats;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventMeterTest {

  private EventMetric meter;

  @Before
  public void setUp() throws Exception {
    this.meter = MetricManager.getEventMetric(EventMeterTest.class, "eventThings", "thingScope",
        TimeUnit.SECONDS);
  }

  @Test
  public void aBlankMeter() throws Exception {

    meter.clearStatistics();
    Assert.assertEquals("the meter has a count of zero", meter.getEventMovingAverage().getCount(), 0L);
    Assert.assertTrue("the meter has a mean rate of zero",
        meter.getEventMovingAverage().getMeanRate() < 0.001);
  }

  @Test
  public void aMeterWithThreeEvents() throws Exception {

    meter.clearStatistics();
    Assert.assertEquals("the meter has a count of 0", meter.getEventMovingAverage().getCount(), 0L);

    meter.markEvent();
    meter.markEvent();
    meter.markEvent();
    meter.updateStatistics();

    Assert.assertEquals("the meter has a count of three", meter.getEventMovingAverage().getCount(), 3L);

  }

  @Test
  public void testSomeRandom() throws InterruptedException {

    meter.clearStatistics();

    Random random = new Random();
    for (int i = 0; i < 10; i++) {
      meter.markEvent();
      Thread.sleep(50 + random.nextInt(150));
    }

    // make sure statistics are current, normally this is
    // left to the background timer to update the statistics
    meter.updateStatistics();
    Stats.MovingAverages statistics = meter.getEventMovingAverage();
    System.out.println(meter.getName() + " " + statistics);
  }
}
