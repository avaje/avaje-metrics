package org.avaje.metric.report;

import java.io.IOException;
import java.io.StringWriter;

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
import org.junit.Assert;
import org.junit.Test;

public class CsvWriteVisitorTest {

  private static long NANOS_TO_MICROS = 1000L;
  
  private static long NANOS_TO_MILLIS = 1000000L;

    
  
  @Test
  public void testCounter() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = new CsvWriteVisitor(writer);
    
    CounterMetric counter = createCounterMetric();
    
    csvVisitor.visit(counter);
    String counterCsv = writer.toString();
                
    Assert.assertTrue(counterCsv.contains("org.test.CounterFoo.doStuff,"));
    Assert.assertTrue(counterCsv.contains("count=10,"));
    Assert.assertTrue(counterCsv.contains("dur=0,"));
  }

  
  @Test
  public void testGaugeMetric() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = new CsvWriteVisitor(writer);

    GaugeDoubleMetric metric = createGaugeMetric();
    
    csvVisitor.visit(metric);
    String csvContent = writer.toString();
    
    Assert.assertTrue(csvContent.contains("org.test.GaugeFoo.doStuff,"));
    Assert.assertTrue(csvContent.contains("value=24.0,"));    
  }

  @Test
  public void testValueMetric() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = new CsvWriteVisitor(writer);
     
    ValueMetric metric = createValueMetric();
    
    csvVisitor.visit(metric);
    String csvContent = writer.toString();
    
    Assert.assertTrue(csvContent.contains("org.test.ValueFoo.doStuff,"));
    Assert.assertTrue(csvContent.contains("count=3,"));  
    Assert.assertTrue(csvContent.contains("avg=14,"));
    Assert.assertTrue(csvContent.contains("max=16,"));
    Assert.assertTrue(csvContent.contains("sum=42,"));
    Assert.assertTrue(csvContent.contains("dur=0,"));
  }

  
  @Test
  public void testTimedMetric() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = new CsvWriteVisitor(writer);
    
    TimedMetric metric = createTimedMetric();
    
    csvVisitor.visit(metric);
    String csvContent = writer.toString();
    
    
    
    // values converted into microseconds
    Assert.assertTrue(csvContent.contains("org.test.TimedFoo.doStuff,"));
    Assert.assertTrue(csvContent.contains("count=3,"));  
    Assert.assertTrue(csvContent.contains("avg=120,"));
    Assert.assertTrue(csvContent.contains("max=140,"));
    Assert.assertTrue(csvContent.contains("sum=360,"));
    Assert.assertTrue(csvContent.contains("dur=0,"));
    
    Assert.assertTrue(csvContent.contains("err.count=2,"));  
    Assert.assertTrue(csvContent.contains("err.avg=210,"));
    Assert.assertTrue(csvContent.contains("err.max=220,"));
    Assert.assertTrue(csvContent.contains("err.sum=420,"));
    Assert.assertTrue(csvContent.contains("err.dur=0,"));
    
  }


  /**
   * Test using a BucketTimedMetric with all buckets having values.
   */
  @Test
  public void testBucketTimedMetricFull() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = new CsvWriteVisitor(writer);
    
    BucketTimedMetric metric = createBucketTimedMetricFull();
    
    csvVisitor.visit(metric);
    String csvContent = writer.toString();
    
    String[] lines = csvContent.split("\n");
    Assert.assertEquals(2, lines.length);

    // 11:53:58, org.test.BucketTimedFoo.doStuff-0-150, count=3,           avg=120000,        max=140000,        sum=360000,        dur=0,             err.count=0,
    // 11:53:58, org.test.BucketTimedFoo.doStuff-150+, count=2,           avg=210000,        max=220000,        sum=420000,        dur=0,             err.count=0,

    Assert.assertTrue(lines[0].contains("org.test.BucketTimedFoo.doStuff-0-150,"));
    Assert.assertTrue(lines[0].contains("count=3,"));  
    Assert.assertTrue(lines[0].contains("avg=120000,"));
    Assert.assertTrue(lines[0].contains("max=140000,"));
    Assert.assertTrue(lines[0].contains("sum=360000,"));
    Assert.assertTrue(lines[0].contains("dur=0,"));
    Assert.assertTrue(lines[0].contains("err.count=0,"));


    Assert.assertTrue(lines[1].contains("org.test.BucketTimedFoo.doStuff-150+,"));
    Assert.assertTrue(lines[1].contains("count=2,"));
    Assert.assertTrue(lines[1].contains("avg=210000,"));
    Assert.assertTrue(lines[1].contains("max=220000,"));
    Assert.assertTrue(lines[1].contains("sum=420000,"));
    Assert.assertTrue(lines[1].contains("dur=0,"));
    Assert.assertTrue(lines[1].contains("err.count=0,"));

  }

  /**
   * Test using a BucketTimedMetric with some buckets empty.
   */
  @Test
  public void testBucketTimedMetricPartial() throws IOException {
    
    StringWriter writer = new StringWriter();
    CsvWriteVisitor csvVisitor = new CsvWriteVisitor(writer);
      

    BucketTimedMetric metric = createBucketTimedMetricPartial();

    csvVisitor.visit(metric);
    String csvContent = writer.toString();

    String[] lines = csvContent.split("\n");
    Assert.assertEquals(3, lines.length);

    // 11:57:58, org.test.BucketTimedFoo.doStuff-0-150, count=3,           avg=120000,        max=140000,        sum=360000,        dur=0,             err.count=0,
    // 11:57:58, org.test.BucketTimedFoo.doStuff-150-300, count=0,           err.count=0,
    // 11:57:58, org.test.BucketTimedFoo.doStuff-300+, count=0,           err.count=0,

    Assert.assertTrue(lines[0].contains("org.test.BucketTimedFoo.doStuff-0-150,"));
    Assert.assertTrue(lines[0].contains("count=3,"));
    Assert.assertTrue(lines[0].contains("avg=120000,"));
    Assert.assertTrue(lines[0].contains("max=140000,"));
    Assert.assertTrue(lines[0].contains("sum=360000,"));
    Assert.assertTrue(lines[0].contains("dur=0,"));
    Assert.assertTrue(lines[0].contains("err.count=0,"));


    Assert.assertTrue(lines[1].contains("org.test.BucketTimedFoo.doStuff-150-300,"));
    Assert.assertTrue(lines[1].contains("count=0,"));
    Assert.assertFalse(lines[1].contains("avg="));
    Assert.assertFalse(lines[1].contains("max="));
    Assert.assertFalse(lines[1].contains("sum="));
    Assert.assertFalse(lines[1].contains("dur="));
    Assert.assertTrue(lines[1].contains("err.count=0,"));

    Assert.assertTrue(lines[2].contains("org.test.BucketTimedFoo.doStuff-300+,"));
    Assert.assertTrue(lines[2].contains("count=0,"));
    Assert.assertFalse(lines[2].contains("avg="));
    Assert.assertFalse(lines[2].contains("max="));
    Assert.assertFalse(lines[2].contains("sum="));
    Assert.assertFalse(lines[2].contains("dur="));
    Assert.assertTrue(lines[2].contains("err.count=0,"));
  }
  
  

  

  private CounterMetric createCounterMetric() {
    CounterMetric counter = new DefaultCounterMetric(MetricManager.name("org.test.CounterFoo.doStuff"));
    counter.markEvents(10);
    counter.collectStatistics();
    return counter;
  }
  
  private GaugeDoubleMetric createGaugeMetric() {
    GaugeDouble gauge = new GaugeDouble() {
      @Override
      public double getValue() {
        return 24d;
      }
    };
    GaugeDoubleMetric metric = new DefaultGaugeDoubleMetric(MetricManager.name("org.test.GaugeFoo.doStuff"), gauge);
    metric.collectStatistics();
    return metric;
  }
  
  private ValueMetric createValueMetric() {
    ValueMetric metric = new DefaultValueMetric(MetricManager.name("org.test.ValueFoo.doStuff"));
    metric.addEvent(12);
    metric.addEvent(14);
    metric.addEvent(16);
    metric.collectStatistics();
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
    
    metric.collectStatistics();
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
    
    metric.collectStatistics();
    return metric;
  }

  /**
   * Create a BucketTimedMetric with some buckets completely empty
   * @return
   */
  private BucketTimedMetric createBucketTimedMetricPartial() {
      
    BucketTimedMetricFactory factory = new BucketTimedMetricFactory();
    BucketTimedMetric metric = factory.createMetric(MetricManager.name("org.test.BucketTimedFoo.doStuff"), new int[]{150, 300});
        
    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MILLIS); // 100 millis
    metric.addEventDuration(true, 120 * NANOS_TO_MILLIS); // 120 millis
    metric.addEventDuration(true, 140 * NANOS_TO_MILLIS);
      
    // Not puting in values > 150 millis so last 2 buckets are empty
      
    metric.collectStatistics();
    return metric;
  }

}
