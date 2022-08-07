package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.MetricStatsVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertSame;

class BaseTimingEntryTest {

  @Test
  void testCompareTo() {

    int depth = 0;
    List<BaseTimingEntry> entries = new ArrayList<>();
    BaseTimingEntry e0 = new BaseTimingEntry(depth++, null, System.nanoTime());
    BaseTimingEntry e1 = new BaseTimingEntry(depth, null, System.nanoTime());
    BaseTimingEntry e2 = new BaseTimingEntry(depth, null, System.nanoTime());
    BaseTimingEntry e3 = new BaseTimingEntry(depth--, null, System.nanoTime());
    entries.add(e2);
    entries.add(e1);
    entries.add(e3);
    entries.add(e0);

    Collections.sort(entries);

    assertSame(e0, entries.get(0));
    assertSame(e1, entries.get(1));
    assertSame(e2, entries.get(2));
    assertSame(e3, entries.get(3));
  }

  @Test
  void testCompareToWithSameDepth() {

    int depth = 0;
    List<BaseTimingEntry> entries = new ArrayList<>();
    BaseTimingEntry e0 = new BaseTimingEntry(depth++, new TDMetric("test.e0"), System.nanoTime());
    long nanos = System.nanoTime();
    BaseTimingEntry e1 = new BaseTimingEntry(depth++, new TDMetric("test.e1"), nanos);
    BaseTimingEntry e2 = new BaseTimingEntry(depth--, new TDMetric("test.e2"), nanos);
    BaseTimingEntry e3 = new BaseTimingEntry(depth, new TDMetric("test.e3"), System.nanoTime());

    entries.add(e2);
    entries.add(e1);
    entries.add(e3);
    entries.add(e0);

    Collections.sort(entries);

    assertSame(e0, entries.get(0));
    assertSame(e1, entries.get(1));
    assertSame(e2, entries.get(2));
    assertSame(e3, entries.get(3));

  }

  static class TDMetric implements TimedMetric {

    MetricName metricName;

    TDMetric(String name) {
      metricName = new DMetricName(name);
    }

    @Override
    public MetricName name() {
      return metricName;
    }

    @Override
    public boolean isBucket() {
      return false;
    }

    @Override
    public String bucketRange() {
      return null;
    }

    @Override
    public void time(Runnable event) {
      event.run();
    }

    @Override
    public <T> T time(Supplier<T> event) {
      return event.get();
    }

    @Override
    public TimedEvent startEvent() {
      return null;
    }

    @Override
    public void addEventSince(boolean success, long startNanos) {

    }

    @Override
    public void addEventDuration(boolean success, long durationNanos) {

    }

    @Override
    public void add(long startNanos) {

    }

    @Override
    public void add(long startNanos, boolean activeThreadContext) {

    }

    @Override
    public void addErr(long startNanos) {

    }

    @Override
    public void addErr(long startNanos, boolean activeThreadContext) {

    }

    @Override
    public boolean isRequestTiming() {
      return false;
    }

    @Override
    public void setRequestTiming(int collectionCount) {

    }

    @Override
    public int getRequestTiming() {
      return 0;
    }

    @Override
    public void decrementRequestTiming() {

    }

    @Override
    public void collect(MetricStatsVisitor visitor) {

    }

    @Override
    public void reset() {

    }

    @Override
    public Map<String, String> attributes() {
      return null;
    }
  }
}
