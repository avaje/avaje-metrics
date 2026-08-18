package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Meter;
import io.avaje.metrics.NamingMatch;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static io.avaje.metrics.CollectionMode.CUMULATIVE;
import static io.avaje.metrics.CollectionMode.DELTA;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueCounterTest {

  private final AtomicLong nanoTime = new AtomicLong();

  @Test
  void testGetStatisticsWithNoReset() {
    ValueCounter counter = new ValueCounter(Metric.ID.of("junk"));
    assertEquals(0, counter.max());

    counter.add(100);

    assertEquals(1, counter.count());
    assertEquals(100, counter.total());
    assertEquals(100, counter.max());

    counter.add(50);
    // no activity, just get statistics again
    assertEquals(2, counter.count());
    assertEquals(150, counter.total());
    assertEquals(100, counter.max());
  }

  @Test
  void test() {
    ValueCounter counter = new ValueCounter(Metric.ID.of("junk"));

    assertEquals(0, counter.count());
    assertEquals(0, counter.total());
    assertEquals(0, counter.max());

    counter.add(100);
    assertEquals(1, counter.count());
    assertEquals(100, counter.total());
    assertEquals(100, counter.max());

    counter.add(50);
    assertEquals(2, counter.count());
    assertEquals(150, counter.total());
    assertEquals(100, counter.max());

    counter.add(200);
    assertEquals(3, counter.count());
    assertEquals(350, counter.total());
    assertEquals(200, counter.max());

    counter.add(20);
    assertEquals(4, counter.count());
    assertEquals(370, counter.total());
    assertEquals(200, counter.max());

    counter.reset();
    assertEquals(0, counter.count());
    assertEquals(0, counter.total());
    assertEquals(0, counter.max());
  }

  @Test
  void collectSharesMaxAcrossCollectionModes() {
    ValueCounter counter = new ValueCounter(Metric.ID.of("junk"), new ValueMax(nanoTime::get));
    counter.add(100);
    counter.add(50);

    Meter.Stats cumulative = collect(counter, CUMULATIVE);
    assertEquals(100, cumulative.max());

    counter.add(200);
    Meter.Stats delta = collect(counter, DELTA);
    assertEquals(100, delta.max());

    nanoTime.addAndGet(TimeUnit.SECONDS.toNanos(59));
    Meter.Stats next = collect(counter, CUMULATIVE);
    assertEquals(200, next.max());
    counter.add(300);
    assertEquals(200, collect(counter, DELTA).max());
  }

  private Meter.Stats collect(ValueCounter counter, io.avaje.metrics.CollectionMode mode) {
    var collector = new DStatsCollector(NamingMatch.INSTANCE, mode);
    return (Meter.Stats) counter.collect(collector, "units");
  }
}
