package io.avaje.metrics;

import java.util.List;

import io.avaje.metrics.core.JsonWriter;

/**
 * Utility to write the metric information as JSON to a buffer.
 */
public final class MetricsJsonWriter {

  private MetricsJsonWriter() {}

  /**
   * Write metrics using the original JSON representation.
   */
  public static void append(Appendable out, List<Metric.Statistics> metrics) {
    JsonWriter.writeTo(out, metrics);
  }

  /**
   * Write metrics using the version 2 JSON representation.
   */
  public static void appendV2(Appendable out, List<Metric.Statistics> metrics) {
    JsonWriter.writeToV2(out, metrics);
  }
}
