package org.avaje.metric.filereport;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.GaugeMetric;
import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.Metric;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.ValueStatistics;
import org.avaje.metric.report.MetricVisitor;

public class TextFileWriteVisitor implements MetricVisitor {

  protected final SimpleDateFormat timeFormat;

  protected final int decimalPlaces;
  
  protected final int gaugePadding;

  protected final Writer writer;

  protected boolean errors;

  public TextFileWriteVisitor(Writer writer) {
    this(writer, "HH:mm:ss");
  }

  public TextFileWriteVisitor(Writer writer, String timeNowFormat) {
    this.writer = writer;
    this.timeFormat = new SimpleDateFormat(timeNowFormat);
    this.decimalPlaces = 2;
    this.gaugePadding = decimalPlaces+4;
    
    initialiseNewLine();
  }

  protected void initialiseNewLine() {
    try {
      writer.write(getNowString());
      writer.write(" -\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeMetricName(Metric metric) throws IOException {
    writer.write(getNowString());
    writer.write(" ");
    writeWithPadding(metric.getName().getSimpleName(), 30);
    writer.write(" ");
  }

  protected void writeMetricEnd(Metric metric) throws IOException {
    writer.write("\n");
  }
  
  @Override
  public void visit(TimedMetric metric) {
   
    try {
      writeMetricName(metric);
      writeSummary("", metric.getCollectedSuccessStatistics());
      writeSummary("err.", metric.getCollectedErrorStatistics());
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(ValueMetric metric) {
    try {
      writeMetricName(metric);
      writeSummary("", metric.getCollectedStatistics());
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(CounterMetric metric) {

    try {
      writeMetricName(metric);
      CounterStatistics counterStatistics = metric.getCollectedStatistics();
      writer.write("count=");
      writer.write(String.valueOf(counterStatistics.getCount()));
      writer.write(" ");
      writer.write("dur=");
      writer.write(String.valueOf(getDuration(counterStatistics.getStartTime())));
      writer.write(" ");
      writeMetricEnd(metric);
      
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeMetricGroup gaugeMetricGroup) {

    try {
      GaugeMetric[] gaugeMetrics = gaugeMetricGroup.getGaugeMetrics();
      writeMetricName(gaugeMetricGroup);
      for (GaugeMetric m : gaugeMetrics) {
        writer.write(m.getName().getName());
        writer.write("=");
        writeWithPadding(m.getFormattedValue(decimalPlaces), gaugePadding);
        writer.write(" ");
      }
      writeMetricEnd(gaugeMetricGroup);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void visit(GaugeMetric metric) {
    try {
      writeMetricName(metric);
      writer.write("value=");
      writer.write(metric.getFormattedValue(decimalPlaces));
      writer.write(" ");
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeSummary(String prefix, ValueStatistics valueStats) {
    try {

      long count = valueStats.getCount();
      writePrefix(prefix);
      writer.write("count=");
      writer.write(String.valueOf(count));
      writer.write(" ");
      if (count == 0) {
        return;
      }

      writePrefix(prefix);
      writer.write("avg=");
      writer.write(String.valueOf(valueStats.getMean()));
      writer.write(" ");
      writePrefix(prefix);
      writer.write("max=");
      writer.write(String.valueOf(valueStats.getMax()));
      writer.write(" ");
      writePrefix(prefix);
      writer.write("sum=");
      writer.write(String.valueOf(valueStats.getTotal()));
      writer.write(" ");
      writePrefix(prefix);
      writer.write("dur=");
      writer.write(String.valueOf(getDuration(valueStats.getStartTime())));
      writer.write(" ");
     
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writePrefix(String prefix) {
    try {
      if (errors) {
        writer.write("err.");
      }
      if (!prefix.isEmpty()) {
        writer.write(prefix);
        // writer.write(".");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeWithPadding(String text, int padTo) throws IOException {
    writer.write(text);
    writePadding(text, padTo);
  }

  protected void writePadding(String text, int padTo) throws IOException {
    int extra = padTo - text.length();
    if (extra > 0) {
      for (int i = 0; i < extra; i++) {
        writer.write(" ");
      }
    }
  }

  protected String getNowString() {
    return timeFormat.format(new Date());
  }

  protected long getDuration(long startTime) {
    return Math.round((System.currentTimeMillis() - startTime)/1000d);
  }

}
