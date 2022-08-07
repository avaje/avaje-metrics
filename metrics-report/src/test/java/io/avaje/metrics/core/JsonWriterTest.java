package io.avaje.metrics.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.avaje.metrics.*;
import io.avaje.metrics.report.HeaderInfo;
import io.avaje.metrics.report.JsonWriteVisitor;
import io.avaje.metrics.report.JsonWriter;
import io.avaje.metrics.report.ReportMetrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonWriterTest {

  private static final long NANOS_TO_MICROS = 1000L;
  private static final long NANOS_TO_MILLIS = 1000000L;


  private JsonWriter newJsonMetricVisitor(Writer writer) {
    return new JsonWriter(writer, Collections.emptyList());
  }

  @Test
  void testCounter() {

    StringWriter writer = new StringWriter();
    JsonWriter jsonVisitor = newJsonMetricVisitor(writer);

    CounterMetric counter = createCounterMetric();
    jsonVisitor.visit((CounterMetric.Stats) collectOne(counter));

    String counterJson = writer.toString();

    assertEquals("{\"name\":\"org.test.CounterFoo.doStuff\",\"value\":10}", counterJson);
  }

  @Test
  void testGaugeMetric() {

    StringWriter writer = new StringWriter();
    JsonWriter jsonVisitor = newJsonMetricVisitor(writer).withType(true);
    GaugeDoubleMetric metric = createGaugeMetric();
    jsonVisitor.visit((GaugeDoubleMetric.Stats) collectOne(metric));
    String counterJson = writer.toString();

    assertEquals("{\"type\":\"dm\",\"name\":\"org.test.GaugeFoo.doStuff\",\"value\":24.0}", counterJson);
  }

  @Test
  void testValueMetric() {

    StringWriter writer = new StringWriter();
    JsonWriter jsonVisitor = newJsonMetricVisitor(writer).withType(true);

    ValueMetric metric = createValueMetric();
    jsonVisitor.visit((ValueMetric.Stats) collectOne(metric));
    String counterJson = writer.toString();

    assertEquals("{\"type\":\"vm\",\"name\":\"org.test.ValueFoo.doStuff\",\"count\":3,\"mean\":14,\"max\":16,\"total\":42}", counterJson);
  }

  @Test
  void testTimedMetric() {

    StringWriter writer = new StringWriter();
    JsonWriter jsonVisitor = newJsonMetricVisitor(writer);

    TimedMetric metric = createTimedMetric();
    visitAllTimed(metric, jsonVisitor);

    String counterJson = writer.toString();

    // values converted into microseconds
    String match = "{\"name\":\"org.test.TimedFoo.doStuff.error\",\"count\":2,\"mean\":210,\"max\":220,\"total\":420}{\"name\":\"org.test.TimedFoo.doStuff\",\"count\":3,\"mean\":120,\"max\":140,\"total\":360}";
    assertEquals(match, counterJson);
  }


  /**
   * Test using a BucketTimedMetric with all buckets having values.
   */
  @Test
  void testBucketTimedMetricFull() {

    StringWriter writer = new StringWriter();
    JsonWriter jsonVisitor = newJsonMetricVisitor(writer).withType(true);

    TimedMetric metric = createBucketTimedMetricFull();
    visitAllTimed(metric, jsonVisitor);
    String bucketJson = writer.toString();

    String match = "{\"type\":\"tm\",\"name\":\"org.test.BucketTimedFoo.doStuff;bucket=0-150\",\"count\":3,\"mean\":120000,\"max\":140000,\"total\":360000}{\"type\":\"tm\",\"name\":\"org.test.BucketTimedFoo.doStuff;bucket=150\",\"count\":2,\"mean\":210000,\"max\":220000,\"total\":420000}";
    assertThat(bucketJson).contains(match);
  }

  /**
   * Test using a BucketTimedMetric with some buckets empty.
   */
  @Test
  void testBucketTimedMetricPartial() {

    StringWriter writer = new StringWriter();
    JsonWriter jsonVisitor = newJsonMetricVisitor(writer);

    TimedMetric metric = createBucketTimedMetricPartial();
    visitAllTimed(metric, jsonVisitor);

    String bucketJson = writer.toString();

    String match = "{\"name\":\"org.test.BucketTimedFoo.doStuff;bucket=0-150\",\"count\":3,\"mean\":120000,\"max\":140000,\"total\":360000}";
    assertThat(bucketJson).contains(match);
  }

  private void visitAllTimed(Metric metric, JsonWriter jsonVisitor) {

    List<MetricStats> statistics = collectAll(metric);
    for (MetricStats statistic : statistics) {
      jsonVisitor.visit((TimedMetric.Stats) statistic);
    }
  }

  @Test
  void testMetricList() throws IOException {

    List<Metric> metrics = new ArrayList<>();
    metrics.add(createValueMetric());
    metrics.add(createGaugeMetric());
    metrics.add(createTimedMetric());
    metrics.add(createBucketTimedMetricFull());
    metrics.add(createBucketTimedMetricPartial());
    metrics.add(createCounterMetric());

    List<MetricStats> statistics = collect(metrics);

    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setKey("key-val");
    headerInfo.setEnv("dev");
    headerInfo.setApp("app-val");
    headerInfo.setServer("server-val");

    ReportMetrics reportMetrics = new ReportMetrics(headerInfo, System.currentTimeMillis(), statistics, 60);

    StringWriter writer = new StringWriter();
    JsonWriteVisitor jsonVisitor = new JsonWriteVisitor(writer, reportMetrics);


    jsonVisitor.write();
    String json = writer.toString();
    System.out.println("---");
    System.out.println(json);
    System.out.println("---");

    assertTrue(json.contains("\"env\":\"dev\""));
    assertTrue(json.contains("\"app\":\"app-val\""));
    assertTrue(json.contains("\"server\":\"server-val\""));

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode jsonObject = mapper.readValue(json, ObjectNode.class);

    assertEquals("dev", jsonObject.get("env").asText());
    assertEquals("app-val", jsonObject.get("app").asText());
    assertEquals("server-val", jsonObject.get("server").asText());

    JsonNode jsonNode = jsonObject.get("metrics");
    ArrayNode metricArray = (ArrayNode) jsonNode;
    assertThat(metricArray.size()).isEqualTo(8);

  }

  private MetricStats collectOne(Metric metric) {
    HelperStatsCollector collector = new HelperStatsCollector();
    metric.collect(collector);
    return collector.getList().get(0);
  }

  private List<MetricStats> collect(List<Metric> metrics) {

    HelperStatsCollector collector = new HelperStatsCollector();
    for (Metric metric : metrics) {
      metric.collect(collector);
    }
    return collector.getList();
  }

  private List<MetricStats> collectAll(Metric... metrics) {

    HelperStatsCollector collector = new HelperStatsCollector();
    for (Metric metric : metrics) {
      metric.collect(collector);
    }
    return collector.getList();
  }

  private CounterMetric createCounterMetric() {
    CounterMetric counter = new DCounterMetric("org.test.CounterFoo.doStuff");
    counter.inc(10);
    return counter;
  }

  private GaugeDoubleMetric createGaugeMetric() {
    DoubleSupplier gauge = () -> 24d;
    return new DGaugeDoubleMetric("org.test.GaugeFoo.doStuff", gauge);
  }

  private ValueMetric createValueMetric() {
    ValueMetric metric = new DValueMetric("org.test.ValueFoo.doStuff");
    metric.addEvent(12);
    metric.addEvent(14);
    metric.addEvent(16);
    return metric;
  }

  private TimedMetric createTimedMetric() {

    TimedMetric metric = new DTimedMetric("org.test.TimedFoo.doStuff");

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
    TimedMetric metric = factory.createMetric("org.test.BucketTimedFoo.doStuff", new int[]{150});

    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MILLIS); // 100 millis
    metric.addEventDuration(true, 120 * NANOS_TO_MILLIS); // 120 millis
    metric.addEventDuration(true, 140 * NANOS_TO_MILLIS);
    metric.addEventDuration(true, 200 * NANOS_TO_MILLIS);
    metric.addEventDuration(true, 220 * NANOS_TO_MILLIS);
    return metric;
  }

  /**
   * Create a BucketTimedMetric with some buckets completely empty
   */
  private TimedMetric createBucketTimedMetricPartial() {

    BucketTimedMetricFactory factory = new BucketTimedMetricFactory();
    TimedMetric metric = factory.createMetric("org.test.BucketTimedFoo.doStuff", new int[]{150, 300});

    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MILLIS); // 100 millis
    metric.addEventDuration(true, 120 * NANOS_TO_MILLIS); // 120 millis
    metric.addEventDuration(true, 140 * NANOS_TO_MILLIS);

    // Not puting in values > 150 millis so last 2 buckets are empty
    return metric;
  }

}
