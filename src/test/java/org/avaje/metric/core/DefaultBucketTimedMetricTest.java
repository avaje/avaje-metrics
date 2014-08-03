package org.avaje.metric.core;

import java.util.concurrent.TimeUnit;

import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.MetricName;
import org.avaje.metric.TimedMetric;
import org.junit.Assert;
import org.junit.Test;

public class DefaultBucketTimedMetricTest {

  @Test
  public void test() {
    
    DefaultMetricManager mgr = new DefaultMetricManager();
    
    MetricName name = mgr.name(DefaultBucketTimedMetricTest.class, "test");
    
    BucketTimedMetric bucketTimedMetric = mgr.getBucketTimedMetric(name, 100, 200, 300);
    
    int[] bucketRanges = bucketTimedMetric.getBucketRanges();
    Assert.assertEquals(3, bucketRanges.length);
    Assert.assertEquals(100, bucketRanges[0]);
    Assert.assertEquals(200, bucketRanges[1]);
    Assert.assertEquals(300, bucketRanges[2]);
      
    TimedMetric[] buckets = bucketTimedMetric.getBuckets();
    Assert.assertEquals(4, buckets.length);
    Assert.assertEquals(name+"-0-100", buckets[0].getName().toString());
    Assert.assertEquals(name+"-100-200", buckets[1].getName().toString());
    Assert.assertEquals(name+"-200-300", buckets[2].getName().toString());
    Assert.assertEquals(name+"-300+", buckets[3].getName().toString());
    
    long fiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(50);
    
    Assert.assertEquals(0, buckets[0].getSuccessStatistics(false).getCount());
    bucketTimedMetric.addEventDuration(true, fiftyMillisAsNanos);
    Assert.assertEquals(1, buckets[0].getSuccessStatistics(false).getCount());

    long oneFiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(150);
    
    Assert.assertEquals(0, buckets[1].getSuccessStatistics(false).getCount());
    bucketTimedMetric.addEventDuration(true, oneFiftyMillisAsNanos);
    Assert.assertEquals(1, buckets[1].getSuccessStatistics(false).getCount());

    long twoFiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(250);
    
    Assert.assertEquals(0, buckets[2].getSuccessStatistics(false).getCount());
    bucketTimedMetric.addEventDuration(true, twoFiftyMillisAsNanos);
    Assert.assertEquals(1, buckets[2].getSuccessStatistics(false).getCount());

    long threeFiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(350);
    
    Assert.assertEquals(0, buckets[3].getSuccessStatistics(false).getCount());
    bucketTimedMetric.addEventDuration(true, threeFiftyMillisAsNanos);
    Assert.assertEquals(1, buckets[3].getSuccessStatistics(false).getCount());

  }
}
