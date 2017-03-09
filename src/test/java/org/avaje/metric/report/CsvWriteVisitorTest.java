package org.avaje.metric.report;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.CounterMetric;
import org.avaje.metric.GaugeDouble;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.core.BucketTimedMetricFactory;
import org.avaje.metric.core.DefaultCounterMetric;
import org.avaje.metric.core.DefaultGaugeDoubleMetric;
import org.avaje.metric.core.DefaultTimedMetric;
import org.avaje.metric.core.DefaultValueMetric;
import org.testng.Assert;
import org.testng.annotations.Test;

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
    
    csvVisitor.visit(counter);
    String counterCsv = writer.toString();
                
    assertTrue(counterCsv.contains(",org.test.CounterFoo.doStuff,"));
    assertTrue(counterCsv.contains(",count=10,"));
    assertTrue(counterCsv.contains(",dur=0"));
  }


  private CsvWriteVisitor createVisitor(StringWriter writer) {
    return new CsvWriteVisitor(writer, "10:00:00", 2, ",", "\n", 0);
  }

  
  @Test
  public void testGaugeMetric() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);

    GaugeDoubleMetric metric = createGaugeMetric();
    
    csvVisitor.visit(metric);
    String csvContent = writer.toString();

    assertThat(csvContent).contains(",org.test.GaugeFoo.doStuff,");
    assertThat(csvContent).contains(",24.0");
  }

  @Test
  public void testValueMetric() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);
     
    ValueMetric metric = createValueMetric();
    
    csvVisitor.visit(metric);
    String csvContent = writer.toString();

    assertThat(csvContent).contains(",org.test.ValueFoo.doStuff,");
    assertThat(csvContent).contains(",count=3,");
    assertThat(csvContent).contains(",avg=14,");
    assertThat(csvContent).contains(",max=16,");
    assertThat(csvContent).contains(",sum=42,");
    assertThat(csvContent).contains(",dur=0");
  }

  
  @Test
  public void testTimedMetric() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);
    
    TimedMetric metric = createTimedMetric();
    
    csvVisitor.visit(metric);
    String csvContent = writer.toString();
    
    // values converted into microseconds
    assertThat(csvContent).contains(",org.test.TimedFoo.doStuff,");
    assertThat(csvContent).contains(",count=3,");
    assertThat(csvContent).contains(",avg=120,");
    assertThat(csvContent).contains(",max=140,");
    assertThat(csvContent).contains(",sum=360,");
    assertThat(csvContent).contains(",dur=0");

    assertThat(csvContent).contains(",err.count=2,");
    assertThat(csvContent).contains(",err.avg=210,");
    assertThat(csvContent).contains(",err.max=220,");
    assertThat(csvContent).contains(",err.sum=420,");
    assertThat(csvContent).contains(",err.dur=0");
    
  }


  /**
   * Test using a BucketTimedMetric with all buckets having values.
   */
  @Test
  public void testBucketTimedMetricFull() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);
    
    BucketTimedMetric metric = createBucketTimedMetricFull();
    
    csvVisitor.visit(metric);
    String csvContent = writer.toString();
    
    String[] lines = csvContent.split("\n");
    assertEquals(2, lines.length);

    assertThat(lines[0]).contains(",org.test.BucketTimedFoo.doStuff[0-150]");
    assertThat(lines[0]).contains(",count=3,");
    assertThat(lines[0]).contains(",avg=120000,");
    assertThat(lines[0]).contains(",max=140000,");
    assertThat(lines[0]).contains(",sum=360000,");
    assertThat(lines[0]).contains(",dur=0,");
    assertThat(lines[0]).contains(",err.count=0");


    assertThat(lines[1]).contains(",org.test.BucketTimedFoo.doStuff[150+]");
    assertThat(lines[1]).contains(",count=2,");
    assertThat(lines[1]).contains(",avg=210000,");
    assertThat(lines[1]).contains(",max=220000,");
    assertThat(lines[1]).contains(",sum=420000,");
    assertThat(lines[1]).contains(",dur=0,");
    assertThat(lines[1]).contains(",err.count=0");
  }

  /**
   * Test using a BucketTimedMetric with some buckets empty.
   */
  @Test
  public void testBucketTimedMetricPartial() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = createVisitor(writer);
      

    BucketTimedMetric metric = createBucketTimedMetricPartial();

    csvVisitor.visit(metric);
    String csvContent = writer.toString();

    String[] lines = csvContent.split("\n");
    assertEquals(3, lines.length);

    assertThat(lines[0]).contains(",org.test.BucketTimedFoo.doStuff[0-150],");
    assertThat(lines[0]).contains(",count=3,");
    assertThat(lines[0]).contains(",avg=120000,");
    assertThat(lines[0]).contains(",max=140000,");
    assertThat(lines[0]).contains(",sum=360000,");
    assertThat(lines[0]).contains(",dur=0,");
    assertThat(lines[0]).contains(",err.count=0");


    assertThat(lines[1]).contains(",org.test.BucketTimedFoo.doStuff[150-300],");
    assertTrue(lines[1].contains(",count=0,"));
    Assert.assertFalse(lines[1].contains(",avg="));
    Assert.assertFalse(lines[1].contains(",max="));
    Assert.assertFalse(lines[1].contains(",sum="));
    Assert.assertFalse(lines[1].contains(",dur="));
    assertTrue(lines[1].contains(",err.count=0"));

    assertThat(lines[2]).contains(",org.test.BucketTimedFoo.doStuff[300+],");
    assertTrue(lines[2].contains(",count=0,"));
    Assert.assertFalse(lines[2].contains(",avg="));
    Assert.assertFalse(lines[2].contains(",max="));
    Assert.assertFalse(lines[2].contains(",sum="));
    Assert.assertFalse(lines[2].contains(",dur="));
    assertTrue(lines[2].contains(",err.count=0"));
  }
  
  

  

  private CounterMetric createCounterMetric() {
    CounterMetric counter = new DefaultCounterMetric(MetricManager.name("org.test.CounterFoo.doStuff"));
    counter.markEvents(10);
    counter.collectStatistics(new ArrayList<>());
    return counter;
  }
  
  private GaugeDoubleMetric createGaugeMetric() {
    GaugeDouble gauge = () -> 24d;
    GaugeDoubleMetric metric = new DefaultGaugeDoubleMetric(MetricManager.name("org.test.GaugeFoo.doStuff"), gauge);
    metric.collectStatistics(new ArrayList<>());
    return metric;
  }
  
  private ValueMetric createValueMetric() {
    ValueMetric metric = new DefaultValueMetric(MetricManager.name("org.test.ValueFoo.doStuff"));
    metric.addEvent(12);
    metric.addEvent(14);
    metric.addEvent(16);
    metric.collectStatistics(new ArrayList<>());
    return metric;
  }
  
  private TimedMetric createTimedMetric() {
      
    TimedMetric metric = new DefaultTimedMetric(MetricManager.name("org.test.TimedFoo.doStuff"));
    
    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MICROS); // 100 micros
    metric.addEventDuration(true, 120 * NANOS_TO_MICROS); // 120 micros
    metric.addEventDuration(true, 140 * NANOS_TO_MICROS);
    metric.addEventDuration(false, 200 * NANOS_TO_MICROS);
    metric.addEventDuration(false, 220 * NANOS_TO_MICROS);
    
    metric.collectStatistics(new ArrayList<>());
    return metric;
  }
  
  private BucketTimedMetric createBucketTimedMetricFull() {
      
    BucketTimedMetricFactory factory = new BucketTimedMetricFactory();
    BucketTimedMetric metric = factory.createMetric(MetricManager.name("org.test.BucketTimedFoo.doStuff"), new int[]{150});
      
    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MILLIS); // 100 millis
    metric.addEventDuration(true, 120 * NANOS_TO_MILLIS); // 120 millis
    metric.addEventDuration(true, 140 * NANOS_TO_MILLIS);
    metric.addEventDuration(true, 200 * NANOS_TO_MILLIS);
    metric.addEventDuration(true, 220 * NANOS_TO_MILLIS);
    
    metric.collectStatistics(new ArrayList<>());
    return metric;
  }

  /**
   * Create a BucketTimedMetric with some buckets completely empty.
   */
  private BucketTimedMetric createBucketTimedMetricPartial() {
      
    BucketTimedMetricFactory factory = new BucketTimedMetricFactory();
    BucketTimedMetric metric = factory.createMetric(MetricManager.name("org.test.BucketTimedFoo.doStuff"), new int[]{150, 300});
        
    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MILLIS); // 100 millis
    metric.addEventDuration(true, 120 * NANOS_TO_MILLIS); // 120 millis
    metric.addEventDuration(true, 140 * NANOS_TO_MILLIS);
      
    // Not putting in values > 150 millis so last 2 buckets are empty
      
    metric.collectStatistics(new ArrayList<>());
    return metric;
  }

}
