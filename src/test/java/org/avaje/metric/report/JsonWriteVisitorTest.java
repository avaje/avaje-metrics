package org.avaje.metric.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonWriteVisitorTest {

  private static final long NANOS_TO_MICROS = 1000L;

  private static final long NANOS_TO_MILLIS = 1000000L;


  private JsonWriteVisitor newJsonMetricVisitor(Writer writer) {
    ReportMetrics m = new ReportMetrics(null, System.currentTimeMillis(), null);
    return new JsonWriteVisitor(writer, m);
  }

  @Test
  public void testCounter() throws IOException {

    StringWriter writer = new StringWriter();
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor(writer);

    CounterMetric counter = createCounterMetric();

    jsonVisitor.visit(counter);
    String counterJson = writer.toString();

    Assert.assertEquals("{\"type\":\"counter\",\"name\":\"org.test.CounterFoo.doStuff\",\"count\":10,\"dur\":0}", counterJson);
  }


  @Test
  public void testGaugeMetric() throws IOException {

    StringWriter writer = new StringWriter();
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor(writer);
    GaugeDoubleMetric metric = createGaugeMetric();

    jsonVisitor.visit(metric);
    String counterJson = writer.toString();

    Assert.assertEquals("{\"type\":\"gauge\",\"name\":\"org.test.GaugeFoo.doStuff\",\"value\":24.0}", counterJson);
  }

  @Test
  public void testValueMetric() throws IOException {

    StringWriter writer = new StringWriter();
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor(writer);

    ValueMetric metric = createValueMetric();

    jsonVisitor.visit(metric);
    String counterJson = writer.toString();

    Assert.assertEquals("{\"type\":\"value\",\"name\":\"org.test.ValueFoo.doStuff\",\"count\":3,\"avg\":14,\"max\":16,\"sum\":42,\"dur\":0}", counterJson);
  }


  @Test
  public void testTimedMetric() throws IOException {

    StringWriter writer = new StringWriter();
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor(writer);

    TimedMetric metric = createTimedMetric();

    jsonVisitor.visit(metric);
    String counterJson = writer.toString();

    // values converted into microseconds
    Assert.assertEquals("{\"type\":\"timed\",\"name\":\"org.test.TimedFoo.doStuff\",\"norm\":{\"count\":3,\"avg\":120,\"max\":140,\"sum\":360,\"dur\":0},\"error\":{\"count\":2,\"avg\":210,\"max\":220,\"sum\":420,\"dur\":0}}", counterJson);
  }


  /**
   * Test using a BucketTimedMetric with all buckets having values.
   */
  @Test
  public void testBucketTimedMetricFull() throws IOException {

    StringWriter writer = new StringWriter();
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor(writer);

    BucketTimedMetric metric = createBucketTimedMetricFull();

    jsonVisitor.visit(metric);
    String bucketJson = writer.toString();

    assertThat(bucketJson).contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff\",\"bucket\":\"0-150\",\"norm\":{\"count\":3,\"avg\":120000,\"max\":140000,\"sum\":360000,\"dur\":0},\"error\":{\"count\":0}},");
    assertThat(bucketJson).contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff\",\"bucket\":\"150+\",\"norm\":{\"count\":2,\"avg\":210000,\"max\":220000,\"sum\":420000,\"dur\":0},\"error\":{\"count\":0}}");
  }

  /**
   * Test using a BucketTimedMetric with some buckets empty.
   */
  @Test
  public void testBucketTimedMetricPartial() throws IOException {

    StringWriter writer = new StringWriter();
    JsonWriteVisitor jsonVisitor = newJsonMetricVisitor(writer);

    BucketTimedMetric metric = createBucketTimedMetricPartial();

    jsonVisitor.visit(metric);
    String bucketJson = writer.toString();

    assertThat(bucketJson).contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff\",\"bucket\":\"0-150\",\"norm\":{\"count\":3,\"avg\":120000,\"max\":140000,\"sum\":360000,\"dur\":0},\"error\":{\"count\":0}},");
    assertThat(bucketJson).contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff\",\"bucket\":\"150-300\",\"norm\":{\"count\":0},\"error\":{\"count\":0}},");
    assertThat(bucketJson).contains("{\"type\":\"timed\",\"name\":\"org.test.BucketTimedFoo.doStuff\",\"bucket\":\"300+\",\"norm\":{\"count\":0},\"error\":{\"count\":0}}");
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

    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setKey("key-val");
    headerInfo.setEnv("dev");
    headerInfo.setApp("app-val");
    headerInfo.setServer("server-val");

    ReportMetrics reportMetrics = new ReportMetrics(headerInfo, System.currentTimeMillis(), metrics);

    StringWriter writer = new StringWriter();
    JsonWriteVisitor jsonVisitor = new JsonWriteVisitor(writer, reportMetrics);


    jsonVisitor.write();
    String json = writer.toString();
    System.out.println("---");
    System.out.println(json);
    System.out.println("---");

    Assert.assertTrue(json.contains("\"env\":\"dev\""));
    Assert.assertTrue(json.contains("\"app\":\"app-val\""));
    Assert.assertTrue(json.contains("\"server\":\"server-val\""));

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode jsonObject = mapper.readValue(json, ObjectNode.class);

    Assert.assertEquals("dev", jsonObject.get("env").asText());
    Assert.assertEquals("app-val", jsonObject.get("app").asText());
    Assert.assertEquals("server-val", jsonObject.get("server").asText());

    JsonNode jsonNode = jsonObject.get("metrics");
    ArrayNode metricArray = (ArrayNode) jsonNode;
    assertThat(metricArray.size()).isEqualTo(9);

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
   *
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
