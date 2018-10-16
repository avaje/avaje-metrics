package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.statistics.MetricStatisticsVisitor;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertSame;

public class BaseTimingEntryTest {

  @Test
  public void testCompareTo() {

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
  public void testCompareToWithSameDepth() throws Exception {

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

  class TDMetric implements TimedMetric {

    MetricName metricName;

    TDMetric(String name) {
      metricName = new DefaultMetricName(name);
    }

    @Override
    public MetricName getName() {
      return metricName;
    }

    @Override
    public boolean isBucket() {
      return false;
    }

    @Override
    public String getBucketRange() {
      return null;
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
    public void operationEnd(int opCode, long startNanos, boolean activeThreadContext) {

    }

    @Override
    public void operationEnd(int opCode, long startNanos) {

    }

    @Override
    public boolean isActiveThreadContext() {
      return false;
    }

    @Override
    public void setRequestTimingCollection(int collectionCount) {

    }

    @Override
    public int getRequestTimingCollection() {
      return 0;
    }

    @Override
    public void decrementCollectionCount() {

    }

    @Override
    public void collect(MetricStatisticsVisitor visitor) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Map<String, String> attributes() {
      return null;
    }
  }
}
