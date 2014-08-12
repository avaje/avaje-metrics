package org.avaje.metric.report;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Writes the collected metrics to registered reporters.
 * <p>
 * Typically you configure the frequency in seconds in which statistics are collected and reported
 * as well as a base directory where the metric files go. By default the base directory will be read
 * from a system property 'metric.directory' and otherwise defaults to the current directory.
 * </p>
 */
public class MetricReportManager {

  private static final Logger logger = LoggerFactory.getLogger(MetricReportManager.class.getName());

  private static final int EIGHT_HOURS = 60 * 60 * 8;

  /**
   * Timer used to periodically execute the metrics collection and reporting.
   */
  protected final ScheduledExecutorService executor;

  /**
   * Frequency in seconds of which the reporting will execute.
   */
  protected final int freqInSeconds;

  /**
   * Optional first reporter.
   */
  protected final MetricReporter localReporter;

  /**
   * Optional second reporter.
   */
  protected final MetricReporter remoteReporter;

  /**
   * The headerInfo which identifies the application, environment and server etc that these metrics
   * are collected for.
   */
  protected HeaderInfo headerInfo;

  /**
   * Create specifying the reporting frequency and a reporter.
   * <p/>
   * This will create a Timer to execute the reporting periodically.
   */
  public MetricReportManager(int freqInSeconds, MetricReporter reporter) {
    this(Executors.newSingleThreadScheduledExecutor(), freqInSeconds, reporter, null);
  }

  /**
   * Create specifying a second reporter.
   * <p/>
   * Having 2 reporters can be useful if you want to store to a local file system and report the
   * metrics to a central repository.
   */
  public MetricReportManager(int freqInSeconds, MetricReporter localReporter, MetricReporter remoteReporter) {
    this(Executors.newSingleThreadScheduledExecutor(), freqInSeconds, localReporter, remoteReporter);
  }

  /**
   * Create specifying the ScheduledExecutorService to use.
   */
  public MetricReportManager(ScheduledExecutorService executor, int freqInSeconds, MetricReporter reporter) {
    this(executor, freqInSeconds, reporter, null);
  }

  /**
   * Create specifying a ScheduledExecutorService, reporting frequency and 2 reporters.
   * <p/>
   * Having 2 reporters can be useful if you want to store to a local file system and report the
   * metrics to a central repository.
   */
  public MetricReportManager(ScheduledExecutorService executor, int freqInSeconds, MetricReporter localReporter, MetricReporter remoteReporter) {

    this.executor = executor;
    this.localReporter = localReporter;
    this.remoteReporter = remoteReporter;
    this.freqInSeconds = freqInSeconds;

    if (freqInSeconds > 0) {
      // Register the metrics collection task to run periodically
      this.executor.scheduleAtFixedRate(new WriteTask(), freqInSeconds, freqInSeconds, TimeUnit.SECONDS);
    }

  }

  /**
   * Set the associated HeaderInfo.
   * <p/>
   * This is the server environment information that is common to all the metrics
   * collected on this running instance.
   */
  public void setHeaderInfo(HeaderInfo headerInfo) {
    this.headerInfo = headerInfo;
  }


  /**
   * Periodic task that collects and reports the metrics.
   */
  protected class WriteTask implements Runnable {

    int cleanupCounter;

    public void run() {
      try {
        cleanupCounter++;
        reportMetrics();

        if (cleanupCounter * freqInSeconds > EIGHT_HOURS) {
          // cleanup old metric files about every 8 hours
          cleanupCounter = 0;
          periodicCleanUp();
        }

      } catch (IOException e) {
        logger.error("Error writing metrics", e);
      }
    }
  }

  /**
   * Perform periodic (defaults to every 8 hours) cleanup.
   * <p/>
   * This is used by file reporters to limit the number of metrics files held.
   */
  protected void periodicCleanUp() {
    if (localReporter != null) {
      localReporter.cleanup();
    }
    if (remoteReporter != null) {
      remoteReporter.cleanup();
    }
  }

  /**
   * Report all the metrics.
   * <p/>
   * This typically means appending the metrics to a file or sending over a network.
   */
  protected void reportMetrics() throws IOException {

    long collectionTime = System.currentTimeMillis();
    List<Metric> metrics = collectMetrics();

    logger.trace("reporting [{}] metrics", metrics.size());

    ReportMetrics reportMetrics = new ReportMetrics(headerInfo, collectionTime, metrics);

    report(reportMetrics, localReporter);
    report(reportMetrics, remoteReporter);
  }

  /**
   * Collect all the non-empty metrics and return them for reporting.
   */
  protected List<Metric> collectMetrics() {

    List<Metric> metrics = sort(MetricManager.getJvmMetrics());
    List<Metric> otherMetrics = sort(MetricManager.collectNonEmptyMetrics());
    metrics.addAll(otherMetrics);
    return metrics;
  }

  /**
   * Sort the metrics into name order.
   */
  protected List<Metric> sort(Collection<Metric> metrics) {

    ArrayList<Metric> ar = new ArrayList<Metric>(metrics);
    Collections.sort(ar, new NameComp());
    return ar;
  }

  /**
   * Visit the metrics sorted by name.
   */
  protected void report(ReportMetrics reportMetrics, MetricReporter reporter) {

    if (reporter != null) {
      try {
        reporter.report(reportMetrics);
      } catch (Exception e) {
        logger.error("Error trying to report metrics", e);
      }
    }
  }

  /**
   * Compare Metrics by name for sorting purposes.
   */
  protected static class NameComp implements Comparator<Metric> {

    @Override
    public int compare(Metric o1, Metric o2) {
      return o1.getName().compareTo(o2.getName());
    }

  }

}
