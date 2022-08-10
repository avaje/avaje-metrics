package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.NamingMatch;
import io.avaje.metrics.Timer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DBucketTimerTest {

  private final DefaultMetricProvider mgr = new DefaultMetricProvider();

  private DBucketTimer create() {
    return (DBucketTimer) mgr.timed("foo.bar", 100, 200, 300);
  }

  @Test
  void test() {
    DBucketTimer bucketTimedMetric = create();

    int[] bucketRanges = bucketTimedMetric.bucketRanges;
    assertEquals(3, bucketRanges.length);
    assertEquals(100, bucketRanges[0]);
    assertEquals(200, bucketRanges[1]);
    assertEquals(300, bucketRanges[2]);

    Timer[] buckets = bucketTimedMetric.buckets;
    assertEquals(4, buckets.length);
    for (Timer bucket : buckets) {
      assertEquals("foo.bar", bucket.name());
    }
    assertEquals("0-100", buckets[0].bucketRange());
    assertEquals("100-200", buckets[1].bucketRange());
    assertEquals("200-300", buckets[2].bucketRange());
    assertEquals("300", buckets[3].bucketRange());


    long fiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(50);
    bucketTimedMetric.addEventDuration(true, fiftyMillisAsNanos);

    Timer.Stats stats = collectTimer(buckets[0]);
    assertEquals(1, stats.count());
    assertEquals(50_000, stats.total());

    long oneFiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(150);
    bucketTimedMetric.addEventDuration(true, oneFiftyMillisAsNanos);
    Timer.Stats stats1 = collectTimer(buckets[1]);

    assertEquals(1, stats1.count());

    long twoFiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(250);
    bucketTimedMetric.addEventDuration(true, twoFiftyMillisAsNanos);
    Timer.Stats stats2 = collectTimer(buckets[2]);
    assertEquals(1, stats2.count());


    long threeFiftyMillisAsNanos = TimeUnit.MILLISECONDS.toNanos(350);

    bucketTimedMetric.addEventDuration(true, threeFiftyMillisAsNanos);
    Timer.Stats stats3 = collectTimer(buckets[3]);
    assertEquals(1, stats3.count());

    bucketTimedMetric.addEventDuration(false, threeFiftyMillisAsNanos);
    assertThat(collect(buckets[2])).isEmpty();
  }

  private Timer.Stats collectTimer(Timer timer) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE);
    timer.collect(collector);
    return (Timer.Stats) collector.list().get(0);
  }

  private List<Metric.Statistics> collect(Timer timer) {
    DStatsCollector collector = new DStatsCollector(NamingMatch.INSTANCE);
    timer.collect(collector);
    return collector.list();
  }
}
