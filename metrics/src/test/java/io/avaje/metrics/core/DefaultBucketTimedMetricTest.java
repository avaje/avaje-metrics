package io.avaje.metrics.core;

class DefaultBucketTimedMetricTest {

//  private final DefaultMetricManager mgr = new DefaultMetricManager();
//  private final MetricName name = MetricName.of(DefaultBucketTimedMetricTest.class, "test");
//
//  public DefaultBucketTimedMetricTest() {
//  }
//
//  private TimedMetric create() {
//    return mgr.timed(name, 100, 200, 300);
//  }
//
//  @Test
//  void test() {
//
////    BucketTimedMetric bucketTimedMetric = create();
////
////    int[] bucketRanges = bucketTimedMetric.getBucketRanges();
////    Assert.assertEquals(3, bucketRanges.length);
////    Assert.assertEquals(100, bucketRanges[0]);
////    Assert.assertEquals(200, bucketRanges[1]);
////    Assert.assertEquals(300, bucketRanges[2]);
////
////    TimedMetric[] buckets = bucketTimedMetric.getBuckets();
////    Assert.assertEquals(4, buckets.length);
////    assertNameMatch(buckets[0].getName(), name);
////    assertNameMatch(buckets[1].getName(), name);
////    assertNameMatch(buckets[2].getName(), name);
////    assertNameMatch(buckets[3].getName(), name);
////
////    Assert.assertEquals("0-100", buckets[0].getBucketRange());
////    Assert.assertEquals("100-200", buckets[1].getBucketRange());
////    Assert.assertEquals("200-300", buckets[2].getBucketRange());
////    Assert.assertEquals("300+", buckets[3].getBucketRange());
////
////    long fiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(50);
////
////    Assert.assertEquals(0, buckets[0].getSuccessStatistics(false).getCount());
////    bucketTimedMetric.addEventDuration(true, fiftyMillisAsNanos);
////    Assert.assertEquals(1, buckets[0].getSuccessStatistics(false).getCount());
////
////    long oneFiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(150);
////
////    Assert.assertEquals(0, buckets[1].getSuccessStatistics(false).getCount());
////    bucketTimedMetric.addEventDuration(true, oneFiftyMillisAsNanos);
////    Assert.assertEquals(1, buckets[1].getSuccessStatistics(false).getCount());
////
////    long twoFiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(250);
////
////    Assert.assertEquals(0, buckets[2].getSuccessStatistics(false).getCount());
////    bucketTimedMetric.addEventDuration(true, twoFiftyMillisAsNanos);
////    Assert.assertEquals(1, buckets[2].getSuccessStatistics(false).getCount());
////
////    long threeFiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(350);
////
////    Assert.assertEquals(0, buckets[3].getSuccessStatistics(false).getCount());
////    bucketTimedMetric.addEventDuration(true, threeFiftyMillisAsNanos);
////    Assert.assertEquals(1, buckets[3].getSuccessStatistics(false).getCount());
//
//  }
//
//  private void assertNameMatch(MetricName name, MetricName name1) {
//    assertThat(name.toString()).isEqualTo(name1.toString());
//  }
}
