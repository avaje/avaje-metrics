package org.avaje.metric.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.CounterMetric;
import org.avaje.metric.GaugeDouble;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.Metric;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonWriteVisitorTest {

  private static long NANOS_TO_MICROS = 1000L;
  
  private static long NANOS_TO_MILLIS = 1000000L;

    
  private JsonWriteVisitor newJsonMetricVisitor() {
    return new JsonWriteVisitor(System.currentTimeMillis());
  }
  
  @Test
  public void testCounter() throws IOException {
    
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor();
    
    CounterMetric counter = createCounterMetric();
    
    jsonVisitor.visit(counter);
    String counterJson = jsonVisitor.getBufferValue();
    
    Assert.assertEquals("{\"type\":\"counter\",\"name\":\"org.test.CounterFoo.doStuff\",\"count\":10,\"dur\":0}", counterJson);
  }

  
  @Test
  public void testGaugeMetric() throws IOException {
    
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor();
    GaugeDoubleMetric metric = createGaugeMetric();
    
    jsonVisitor.visit(metric);
    String counterJson = jsonVisitor.getBufferValue();
    
    Assert.assertEquals("{\"type\":\"gauge\",\"name\":\"org.test.GaugeFoo.doStuff\",\"value\":24.0}", counterJson);
  }

  @Test
  public void testValueMetric() throws IOException {
    
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor();
    
    ValueMetric metric = createValueMetric();
    
    jsonVisitor.visit(metric);
    String counterJson = jsonVisitor.getBufferValue();
    
    Assert.assertEquals("{\"type\":\"value\",\"name\":\"org.test.ValueFoo.doStuff\",\"n\":{\"count\":3,\"avg\":14,\"max\":16,\"sum\":42,\"dur\":0}}", counterJson);
  }

  
  @Test
  public void testTimedMetric() throws IOException {
    
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor();
    
    TimedMetric metric = createTimedMetric();
    
    jsonVisitor.visit(metric);
    String counterJson = jsonVisitor.getBufferValue();
    
    // values converted into microseconds
    Assert.assertEquals("{\"type\":\"timed\",\"name\":\"org.test.TimedFoo.doStuff\",\"n\":{\"count\":3,\"avg\":120,\"max\":140,\"sum\":360,\"dur\":0},\"e\":{\"count\":2,\"avg\":210,\"max\":220,\"sum\":420,\"dur\":0}}", counterJson);
  }


  /**
   * Test using a BucketTimedMetric with all buckets having values.
   */
  @Test
  public void testBucketTimedMetricFull() throws IOException {
    
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor();
    
    BucketTimedMetric metric = createBucketTimedMetricFull();
    
    jsonVisitor.visit(metric);
    String bucketJson = jsonVisitor.getBufferValue();

    String[] lines = bucketJson.split("\n");
    Assert.assertEquals(3, lines.length);

    Assert.assertTrue(lines[0].contains("{\"type\":\"bucketTimed\",\"name\":\"org.test.BucketTimedFoo.doStuff\",\"bucketRanges\":\"150\",\"buckets\":["));
    Assert.assertTrue(lines[1].contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff-0-150\",\"n\":{\"count\":3,\"avg\":120000,\"max\":140000,\"sum\":360000,\"dur\":0},\"e\":{\"count\":0}},"));
    Assert.assertTrue(lines[2].contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff-150+\",\"n\":{\"count\":2,\"avg\":210000,\"max\":220000,\"sum\":420000,\"dur\":0},\"e\":{\"count\":0}}]}"));
  }

  /**
   * Test using a BucketTimedMetric with some buckets empty.
   */
  @Test
  public void testBucketTimedMetricPartial() throws IOException {
    
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor();
    
    BucketTimedMetric metric = createBucketTimedMetricPartial();
    
    jsonVisitor.visit(metric);
    String bucketJson = jsonVisitor.getBufferValue();

    String[] lines = bucketJson.split("\n");
    Assert.assertEquals(4, lines.length);

    Assert.assertTrue(lines[0].contains("{\"type\":\"bucketTimed\",\"name\":\"org.test.BucketTimedFoo.doStuff\",\"bucketRanges\":\"150,300\",\"buckets\":["));
    Assert.assertTrue(lines[1].contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff-0-150\",\"n\":{\"count\":3,\"avg\":120000,\"max\":140000,\"sum\":360000,\"dur\":0},\"e\":{\"count\":0}},"));
    Assert.assertTrue(lines[2].contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff-150-300\",\"n\":{\"count\":0},\"e\":{\"count\":0}},"));
    Assert.assertTrue(lines[3].contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff-300+\",\"n\":{\"count\":0},\"e\":{\"count\":0}}]}"));

  }
  
  
  
  @Test
  public void testMetricList() throws IOException {
    
    List<Metric> metrics = new ArrayList<Metric>();
    metrics.add(createValueMetric());
    metrics.add(createGaugeMetric());
    metrics.add(createTimedMetric());
    metrics.add(createBucketTimedMetricFull());
    metrics.add(createBucketTimedMetricPartial());
    metrics.add(createCounterMetric());



    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor();

    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setKey("key-val");
    headerInfo.setEnv("dev");
    headerInfo.setApp("app-val");
    headerInfo.setServer("server-val");
    
    String json = jsonVisitor.buildJson(headerInfo, metrics);
    System.out.println("---");
    System.out.println(json);
    System.out.println("---");

    Assert.assertTrue(json.contains("\"env\":\"dev\""));
    Assert.assertTrue(json.contains("\"app\":\"app-val\""));
    Assert.assertTrue(json.contains("\"server\":\"server-val\""));
    
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode jsonObject = mapper.readValue(json, ObjectNode.class);
    
    Assert.assertEquals("dev",jsonObject.get("env").asText());
    Assert.assertEquals("app-val",jsonObject.get("app").asText());
    Assert.assertEquals("server-val",jsonObject.get("server").asText());
    
    JsonNode jsonNode = jsonObject.get("metrics");
    ArrayNode metricArray = (ArrayNode)jsonNode;
    Assert.assertEquals(6, metricArray.size());

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
