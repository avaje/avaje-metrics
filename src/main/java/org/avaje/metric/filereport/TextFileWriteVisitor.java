package org.avaje.metric.filereport;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.GaugeDoubleGroup;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.GaugeLongGroup;
import org.avaje.metric.GaugeLongMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.ValueStatistics;

/**
 * A visitor that is aimed to write a space formatted file.
 * <p>
 * This format is aimed at writing to the local file system to provide simple low tech reporting of
 * the collected metrics.
 */
public class TextFileWriteVisitor implements MetricVisitor {

  protected final SimpleDateFormat timeFormat;

  protected final long collectTime;

  protected final int decimalPlaces;
  
  protected final int columnWidth;
  
  protected final String delimitPrefix;
  
  protected final String delimitSuffix;

  protected final Writer writer;

  protected boolean errors;

  public TextFileWriteVisitor(Writer writer) {
    this(writer, "HH:mm:ss");
  }

  public TextFileWriteVisitor(Writer writer, String timeNowFormat) {
    this(writer, timeNowFormat, 16, 2, "", ", ");
  }
  
  public TextFileWriteVisitor(Writer writer, String timeNowFormat, int columnWidth, int decimalPlaces, String delimitPrefix, String delimitSuffix) {
    this.collectTime = System.currentTimeMillis();
    this.writer = writer;
    this.timeFormat = new SimpleDateFormat(timeNowFormat);
    this.decimalPlaces = decimalPlaces;
    this.columnWidth = columnWidth;
    this.delimitPrefix = "";
    this.delimitSuffix = ", ";
    initialiseNewLine();
  }

  protected void initialiseNewLine() {
    try {
      writer.write(getNowString());
      writer.write(delimitSuffix);
      writer.write("-\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeMetricName(Metric metric) throws IOException {
    writer.write(getNowString());
    writer.write(delimitSuffix);
    writeWithPadding(metric.getName().getSimpleName(), 30);
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
      write("count", counterStatistics.getCount());
      write("dur", getDuration(counterStatistics.getStartTime()));
      writeMetricEnd(metric);
      
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visit(GaugeDoubleGroup gaugeMetricGroup) {

    try {
      GaugeDoubleMetric[] gaugeMetrics = gaugeMetricGroup.getGaugeMetrics();
      writeMetricName(gaugeMetricGroup);
      for (GaugeDoubleMetric m : gaugeMetrics) {
        write(m.getName().getName(), formattedValue(m.getValue()));
      }
      writeMetricEnd(gaugeMetricGroup);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void visit(GaugeDoubleMetric metric) {
    try {
      writeMetricName(metric);
      write("value", formattedValue(metric.getValue()));
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String formattedValue(double value) {
    return NumFormat.dp(decimalPlaces, value);
  }
  
  @Override
  public void visit(GaugeLongMetric metric) {
    try {
      writeMetricName(metric);
      write("value", metric.getValue());
      writeMetricEnd(metric);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void visit(GaugeLongGroup gaugeMetricGroup) {

    try {
      GaugeLongMetric[] gaugeMetrics = gaugeMetricGroup.getGaugeMetrics();
      writeMetricName(gaugeMetricGroup);
      for (GaugeLongMetric m : gaugeMetrics) {
        write(m.getName().getName(), m.getValue());
      }
      writeMetricEnd(gaugeMetricGroup);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected void writeSummary(String prefix, ValueStatistics valueStats) {
    
    try {

      long count = valueStats.getCount();
      write(prefix, "count", count);
      if (count == 0) {
        return;
      }
      write(prefix, "avg", valueStats.getMean());
      write(prefix, "max", valueStats.getMax());
      write(prefix, "sum", valueStats.getTotal());
      write(prefix, "dur", getDuration(valueStats.getStartTime()));
     
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void write(String prefix, String name, long value) throws IOException {
    writePrefix(prefix);
    write(name, value);
  }
  
  protected void writePrefix(String prefix) {
    try {
      if (errors) {
        writer.write("err.");
      }
      if (!prefix.isEmpty()) {
        writer.write(prefix);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void write(String name, long value) throws IOException {
    write(name, String.valueOf(value));
  }
  
  protected void write(String name, String value) throws IOException {

    writer.write(name);
    writer.write("=");
    writeWithPadding(value, columnWidth - name.length());
  }  
  
  protected void writeWithPadding(String text, int padTo) throws IOException {
    writer.write(text);
    writer.write(delimitSuffix);
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
    return Math.round((collectTime - startTime)/1000d);
  }

}
