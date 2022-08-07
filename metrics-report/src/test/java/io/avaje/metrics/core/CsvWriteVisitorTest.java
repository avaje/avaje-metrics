package io.avaje.metrics.core;

import io.avaje.metrics.*;
import io.avaje.metrics.Counter;
import io.avaje.metrics.report.CsvWriteVisitor;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;
import java.util.function.DoubleSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvWriteVisitorTest {

  private static final long NANOS_TO_MILLIS = 1000000L;

  @Test
  void testCounter() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    Counter counter = createCounterMetric();

    csvVisitor.visit((Counter.Stats) collect(counter));
    String counterCsv = writer.toString();

    assertThat(counterCsv).contains(",org.test.CounterFoo.doStuff,10");
  }

  private MetricStats collect(Metric metric) {
    return collectAll(metric).get(0);
  }

  private List<MetricStats> collectAll(Metric metric) {
    HelperStatsCollector collector = new HelperStatsCollector();
    metric.collect(collector);
    return collector.getList();
  }

  private CsvWriteVisitor createVisitor(StringWriter writer) {
    return new CsvWriteVisitor(writer, "10:00:00", 2, ",", "\n", 0);
  }


  @Test
  void testGaugeMetric() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    GaugeDouble metric = createGaugeMetric();

    csvVisitor.visit((GaugeDouble.Stats) collect(metric));
    String csvContent = writer.toString();

    assertThat(csvContent).contains(",org.test.GaugeFoo.doStuff,");
    assertThat(csvContent).contains(",24.0");
  }

  @Test
  void testValueMetric() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    Meter metric = createValueMetric();

    csvVisitor.visit((Meter.Stats) collect(metric));
    String csvContent = writer.toString();

    assertThat(csvContent).contains(",org.test.ValueFoo.doStuff,");
    assertThat(csvContent).contains(",count=3,");
    assertThat(csvContent).contains(",mean=14,");
    assertThat(csvContent).contains(",max=16,");
    assertThat(csvContent).contains(",total=42");
  }


  @Test
  void testTimedMetric() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    Timer metric = createTimedMetric();

    List<MetricStats> statistics = collectAll(metric);
    for (MetricStats statistic : statistics) {
      csvVisitor.visit((Meter.Stats) statistic);
    }
    String csvContent = writer.toString();
    String[] lines = csvContent.split("\n");
    assertEquals(2, lines.length);

    assertThat(lines[0]).contains(",org.test.TimedFoo.doStuff.error,");
    assertThat(lines[0]).contains(",count=2,");
    assertThat(lines[0]).contains(",mean=210,");
    assertThat(lines[0]).contains(",max=220,");
    assertThat(lines[0]).contains(",total=420");

    // values converted into microseconds
    assertThat(lines[1]).contains(",org.test.TimedFoo.doStuff,");
    assertThat(lines[1]).contains(",count=3,");
    assertThat(lines[1]).contains(",mean=120,");
    assertThat(lines[1]).contains(",max=140,");
    assertThat(lines[1]).contains(",total=360");
  }


  /**
   * Test using a BucketTimedMetric with all buckets having values.
   */
  @Test
  void testBucketTimedMetricFull() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    Timer metric = createBucketTimedMetricFull();

    metric.addEventDuration(false, 220 * NANOS_TO_MILLIS);
    metric.addEventDuration(false, 110 * NANOS_TO_MILLIS);
    metric.addEventDuration(false, 110 * NANOS_TO_MILLIS);
    metric.addEventDuration(false, 110 * NANOS_TO_MILLIS);

    List<MetricStats> stats = collectAll(metric);
    for (MetricStats stat : stats) {
      csvVisitor.visit((Timer.Stats) stat);
    }
    String csvContent = writer.toString();

    String[] lines = csvContent.split("\n");
    assertEquals(3, lines.length);

    assertThat(lines[0]).contains("org.test.BucketTimedFoo.doStuff.error,count=4,mean=137500,max=220000,total=550000");

    assertThat(lines[1]).contains(",org.test.BucketTimedFoo.doStuff;bucket=0-150");
    assertThat(lines[1]).contains(",count=3,");
    assertThat(lines[1]).contains(",mean=120000,");
    assertThat(lines[1]).contains(",max=140000,");
    assertThat(lines[1]).contains(",total=360000");

    assertThat(lines[2]).contains(",org.test.BucketTimedFoo.doStuff;bucket=150");
    assertThat(lines[2]).contains(",count=2,");
    assertThat(lines[2]).contains(",mean=210000,");
    assertThat(lines[2]).contains(",max=220000,");
    assertThat(lines[2]).contains(",total=420000");
  }

  /**
   * Test using a BucketTimedMetric with some buckets empty.
   */
  @Test
  void testBucketTimedMetricPartial() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);


    Timer metric = createBucketTimedMetricPartial();

    List<MetricStats> statistics = collectAll(metric);
    for (MetricStats statistic : statistics) {
      csvVisitor.visit((Timer.Stats) statistic);
    }
    String csvContent = writer.toString();

    String[] lines = csvContent.split("\n");
    assertEquals(1, lines.length);

    assertThat(lines[0]).contains(",org.test.BucketTimedFoo.doStuff;bucket=0-150,");
    assertThat(lines[0]).contains(",count=3,");
    assertThat(lines[0]).contains(",mean=120000,");
    assertThat(lines[0]).contains(",max=140000,");
    assertThat(lines[0]).contains(",total=360000");
  }


  private Counter createCounterMetric() {
    Counter counter = new DCounterMetric("org.test.CounterFoo.doStuff");
    counter.inc(10);
    return counter;
  }

  private GaugeDouble createGaugeMetric() {
    DoubleSupplier gauge = () -> 24d;
    return new DGaugeDouble("org.test.GaugeFoo.doStuff", gauge);
  }

  private Meter createValueMetric() {
    Meter metric = new DMeter("org.test.ValueFoo.doStuff");
    metric.addEvent(12);
    metric.addEvent(14);
    metric.addEvent(16);
    return metric;
  }

  private Timer createTimedMetric() {

    Timer metric = new DTimer("org.test.TimedFoo.doStuff");

    // add duration times in nanos
    long NANOS_TO_MICROS = 1000L;
    metric.addEventDuration(true, 100 * NANOS_TO_MICROS); // 100 micros
    metric.addEventDuration(true, 120 * NANOS_TO_MICROS); // 120 micros
    metric.addEventDuration(true, 140 * NANOS_TO_MICROS);
    metric.addEventDuration(false, 200 * NANOS_TO_MICROS);
    metric.addEventDuration(false, 220 * NANOS_TO_MICROS);
    return metric;
  }

  private Timer createBucketTimedMetricFull() {

    BucketTimedMetricFactory factory = new BucketTimedMetricFactory();
    Timer metric = factory.createMetric("org.test.BucketTimedFoo.doStuff", new int[]{150});

    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MILLIS); // 100 millis
    metric.addEventDuration(true, 120 * NANOS_TO_MILLIS); // 120 millis
    metric.addEventDuration(true, 140 * NANOS_TO_MILLIS);
    metric.addEventDuration(true, 200 * NANOS_TO_MILLIS);
    metric.addEventDuration(true, 220 * NANOS_TO_MILLIS);
    return metric;
  }

  /**
   * Create a BucketTimedMetric with some buckets completely empty.
   */
  private Timer createBucketTimedMetricPartial() {

    BucketTimedMetricFactory factory = new BucketTimedMetricFactory();
    Timer metric = factory.createMetric("org.test.BucketTimedFoo.doStuff", new int[]{150, 300});

    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MILLIS); // 100 millis
    metric.addEventDuration(true, 120 * NANOS_TO_MILLIS); // 120 millis
    metric.addEventDuration(true, 140 * NANOS_TO_MILLIS);
    // Not putting in values > 150 millis so last 2 buckets are empty
    return metric;
  }

}
