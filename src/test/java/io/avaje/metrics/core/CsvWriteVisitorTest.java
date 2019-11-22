package io.avaje.metrics.core;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.GaugeDouble;
import io.avaje.metrics.GaugeDoubleMetric;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.ValueMetric;
import io.avaje.metrics.report.CsvWriteVisitor;
import io.avaje.metrics.statistics.CounterStatistics;
import io.avaje.metrics.statistics.GaugeDoubleStatistics;
import io.avaje.metrics.statistics.MetricStatistics;
import io.avaje.metrics.statistics.TimedStatistics;
import io.avaje.metrics.statistics.ValueStatistics;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CsvWriteVisitorTest {

  private static long NANOS_TO_MICROS = 1000L;

  private static long NANOS_TO_MILLIS = 1000000L;


  @Test
  public void testCounter() throws IOException {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    CounterMetric counter = createCounterMetric();

    csvVisitor.visit((CounterStatistics) collect(counter));
    String counterCsv = writer.toString();

    assertTrue(counterCsv.contains(",org.test.CounterFoo.doStuff,"));
    assertTrue(counterCsv.contains(",count=10,"));
    assertTrue(counterCsv.contains(",dur=0"));
  }

  private MetricStatistics collect(Metric metric) {
    return collectAll(metric).get(0);
  }

  private List<MetricStatistics> collectAll(Metric metric) {
    HelperStatsCollector collector = new HelperStatsCollector();
    metric.collect(collector);
    return collector.getList();
  }

  private CsvWriteVisitor createVisitor(StringWriter writer) {
    return new CsvWriteVisitor(writer, "10:00:00", 2, ",", "\n", 0);
  }


  @Test
  public void testGaugeMetric() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    GaugeDoubleMetric metric = createGaugeMetric();

    csvVisitor.visit((GaugeDoubleStatistics) collect(metric));
    String csvContent = writer.toString();

    assertThat(csvContent).contains(",org.test.GaugeFoo.doStuff,");
    assertThat(csvContent).contains(",24.0");
  }

  @Test
  public void testValueMetric() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    ValueMetric metric = createValueMetric();

    csvVisitor.visit((ValueStatistics) collect(metric));
    String csvContent = writer.toString();

    assertThat(csvContent).contains(",org.test.ValueFoo.doStuff,");
    assertThat(csvContent).contains(",count=3,");
    assertThat(csvContent).contains(",avg=14,");
    assertThat(csvContent).contains(",max=16,");
    assertThat(csvContent).contains(",sum=42,");
    assertThat(csvContent).contains(",dur=0");
  }


  @Test
  public void testTimedMetric() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    TimedMetric metric = createTimedMetric();

    List<MetricStatistics> statistics = collectAll(metric);
    for (MetricStatistics statistic : statistics) {
      csvVisitor.visit((ValueStatistics)statistic);
    }
    String csvContent = writer.toString();
    String[] lines = csvContent.split("\n");
    assertEquals(2, lines.length);

    assertThat(lines[0]).contains(",org.test.TimedFoo.doStuff.error,");
    assertThat(lines[0]).contains(",count=2,");
    assertThat(lines[0]).contains(",avg=210,");
    assertThat(lines[0]).contains(",max=220,");
    assertThat(lines[0]).contains(",sum=420,");
    assertThat(lines[0]).contains(",dur=0");

    // values converted into microseconds
    assertThat(lines[1]).contains(",org.test.TimedFoo.doStuff,");
    assertThat(lines[1]).contains(",count=3,");
    assertThat(lines[1]).contains(",avg=120,");
    assertThat(lines[1]).contains(",max=140,");
    assertThat(lines[1]).contains(",sum=360,");
    assertThat(lines[1]).contains(",dur=0");
  }


  /**
   * Test using a BucketTimedMetric with all buckets having values.
   */
  @Test
  public void testBucketTimedMetricFull() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    TimedMetric metric = createBucketTimedMetricFull();

    metric.addEventDuration(false, 220 * NANOS_TO_MILLIS);
    metric.addEventDuration(false, 110 * NANOS_TO_MILLIS);
    metric.addEventDuration(false, 110 * NANOS_TO_MILLIS);
    metric.addEventDuration(false, 110 * NANOS_TO_MILLIS);

    List<MetricStatistics> stats = collectAll(metric);
    for (MetricStatistics stat : stats) {
      csvVisitor.visit((TimedStatistics) stat);
    }
    String csvContent = writer.toString();

    String[] lines = csvContent.split("\n");
    assertEquals(3, lines.length);

    assertThat(lines[0]).contains("org.test.BucketTimedFoo.doStuff.error,count=4,avg=137500,max=220000,sum=550000");

    assertThat(lines[1]).contains(",org.test.BucketTimedFoo.doStuff[0-150]");
    assertThat(lines[1]).contains(",count=3,");
    assertThat(lines[1]).contains(",avg=120000,");
    assertThat(lines[1]).contains(",max=140000,");
    assertThat(lines[1]).contains(",sum=360000,");
    assertThat(lines[1]).contains(",dur=0");

    assertThat(lines[2]).contains(",org.test.BucketTimedFoo.doStuff[150+]");
    assertThat(lines[2]).contains(",count=2,");
    assertThat(lines[2]).contains(",avg=210000,");
    assertThat(lines[2]).contains(",max=220000,");
    assertThat(lines[2]).contains(",sum=420000,");
    assertThat(lines[2]).contains(",dur=0");
  }

  /**
   * Test using a BucketTimedMetric with some buckets empty.
   */
  @Test
  public void testBucketTimedMetricPartial() {

    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);


    TimedMetric metric = createBucketTimedMetricPartial();

    List<MetricStatistics> statistics = collectAll(metric);
    for (MetricStatistics statistic : statistics) {
      csvVisitor.visit((TimedStatistics) statistic);
    }
    String csvContent = writer.toString();

    String[] lines = csvContent.split("\n");
    assertEquals(1, lines.length);

    assertThat(lines[0]).contains(",org.test.BucketTimedFoo.doStuff[0-150],");
    assertThat(lines[0]).contains(",count=3,");
    assertThat(lines[0]).contains(",avg=120000,");
    assertThat(lines[0]).contains(",max=140000,");
    assertThat(lines[0]).contains(",sum=360000,");
    assertThat(lines[0]).contains(",dur=0");
  }


  private CounterMetric createCounterMetric() {
    CounterMetric counter = new DefaultCounterMetric(MetricName.of("org.test.CounterFoo.doStuff"));
    counter.markEvents(10);
    return counter;
  }

  private GaugeDoubleMetric createGaugeMetric() {
    GaugeDouble gauge = () -> 24d;
    GaugeDoubleMetric metric = new DefaultGaugeDoubleMetric(MetricName.of("org.test.GaugeFoo.doStuff"), gauge);
    return metric;
  }

  private ValueMetric createValueMetric() {
    ValueMetric metric = new DefaultValueMetric(MetricName.of("org.test.ValueFoo.doStuff"));
    metric.addEvent(12);
    metric.addEvent(14);
    metric.addEvent(16);
    return metric;
  }

  private TimedMetric createTimedMetric() {

    TimedMetric metric = new DefaultTimedMetric(MetricName.of("org.test.TimedFoo.doStuff"));

    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MICROS); // 100 micros
    metric.addEventDuration(true, 120 * NANOS_TO_MICROS); // 120 micros
    metric.addEventDuration(true, 140 * NANOS_TO_MICROS);
    metric.addEventDuration(false, 200 * NANOS_TO_MICROS);
    metric.addEventDuration(false, 220 * NANOS_TO_MICROS);
    return metric;
  }

  private TimedMetric createBucketTimedMetricFull() {

    BucketTimedMetricFactory factory = new BucketTimedMetricFactory();
    TimedMetric metric = factory.createMetric(MetricName.of("org.test.BucketTimedFoo.doStuff"), new int[]{150});

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
  private TimedMetric createBucketTimedMetricPartial() {

    BucketTimedMetricFactory factory = new BucketTimedMetricFactory();
    TimedMetric metric = factory.createMetric(MetricName.of("org.test.BucketTimedFoo.doStuff"), new int[]{150, 300});

    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MILLIS); // 100 millis
    metric.addEventDuration(true, 120 * NANOS_TO_MILLIS); // 120 millis
    metric.addEventDuration(true, 140 * NANOS_TO_MILLIS);
    // Not putting in values > 150 millis so last 2 buckets are empty
    return metric;
  }

}
