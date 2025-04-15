package io.avaje.metrics.core;

import io.avaje.metrics.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Writes the metric information as JSON to a buffer for sending.
 */
final class JsonWriter implements Metric.Visitor {

  private static final DecimalFormat formatDecimal = new DecimalFormat("0.0#");

  private final Appendable buffer;

  private final List<Metric.Statistics> metrics;

  static void writeTo(Appendable writer, List<Metric.Statistics> metrics) {
    new JsonWriter(writer, metrics).write();
  }

  private JsonWriter(Appendable writer, List<Metric.Statistics> metrics) {
    this.buffer = writer;
    this.metrics = metrics;
  }

  private void write() {
    try {
      for (int i = 0; i < metrics.size(); i++) {
        if (i == 0) {
          buffer.append("  ");
        } else {
          buffer.append(" ,");
        }
        metrics.get(i).visit(this);
        buffer.append('\n');
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Error writing JSON", e);
    }
  }

  private void writeMetricStart(Metric.Statistics metric) throws IOException {
    writeMetricStart(metric.name());
  }

  private void writeMetricStart(String name) throws IOException {
    buffer.append('{');
    writeKey("name");
    writeValue(name);
    buffer.append(',');
  }

  private void writeMetricEnd() throws IOException {
    buffer.append('}');
  }

  @Override
  public void visit(Timer.Stats metric) {
    try {
      writeMetricStart(metric.name());
      writeSummary(metric);
      writeMetricEnd();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void visit(Meter.Stats metric) {
    try {
      writeMetricStart(metric);
      writeSummary(metric);
      writeMetricEnd();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void visit(Counter.Stats metric) {
    try {
      writeMetricStart(metric);
      writeKeyNumber("value", metric.count());
      writeMetricEnd();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void visit(GaugeDouble.Stats metric) {
    try {
      writeMetricStart(metric);
      writeKeyNumber("value", format(metric.value()));
      writeMetricEnd();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void visit(GaugeLong.Stats metric) {
    try {
      writeMetricStart(metric);
      writeKeyNumber("value", metric.value());
      writeMetricEnd();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void writeSummary(Meter.Stats valueStats) throws IOException {
    // valueStats == null when BucketTimedMetric and the bucket is empty
    long count = valueStats.count();
    writeKeyNumber("count", count);
    if (count != 0) {
      buffer.append(",");
      writeKeyNumber("mean", valueStats.mean());
      buffer.append(",");
      writeKeyNumber("max", valueStats.max());
      buffer.append(",");
      writeKeyNumber("total", valueStats.total());
    }
  }

  private String format(double value) {
    return formatDecimal.format(value);
  }

  private void writeKeyNumber(String key, long numberValue) throws IOException {
    writeKeyNumber(key, String.valueOf(numberValue));
  }

  private void writeKeyNumber(String key, String numberValue) throws IOException {
    writeKey(key);
    writeNumberValue(numberValue);
  }

  private void writeKey(String key) throws IOException {
    buffer.append('"').append(key).append('"').append(':');
  }

  private void writeValue(String val) throws IOException {
    buffer.append('"').append(val).append('"');
  }

  private void writeNumberValue(String val) throws IOException {
    buffer.append(val);
  }

}
