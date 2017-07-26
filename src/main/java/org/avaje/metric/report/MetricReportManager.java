package org.avaje.metric.report;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.RequestTiming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Writes the collected metrics to registered reporters.
 * <p>
 * Typically you configure the frequency in seconds in which statistics are collected and reported
 * as well as a base directory where the metric files go. By default the base directory will be read
 * from a system property 'metric.directory' and otherwise defaults to the current directory.
 * </p>
 */
public class MetricReportManager {

  private static final Logger logger = LoggerFactory.getLogger(MetricReportManager.class);

  private static final int EIGHT_HOURS = 60 * 60 * 8;

  private static NameComp NAME_COMPARATOR = new NameComp();

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
   * Optional reporter for request timings.
   */
  protected final RequestTimingReporter requestTimingReporter;

  /**
   * The headerInfo which identifies the application, environment and server etc that these metrics
   * are collected for.
   */
  protected final HeaderInfo headerInfo;

  /**
   * Create using the MetricReportConfig bean.
   */
  public MetricReportManager(MetricReportConfig config) {

    this.executor = defaultExecutor(config.getExecutor());
    this.requestTimingReporter = defaultReqReporter(config);
    this.localReporter = defaultReporter(config);
    this.remoteReporter = config.getRemoteReporter();
    this.freqInSeconds = config.getFreqInSeconds();
    this.headerInfo = config.getHeaderInfo();

    if (freqInSeconds > 0) {
      // Register the metrics collection task to run periodically
      executor.scheduleAtFixedRate(new WriteTask(), freqInSeconds, freqInSeconds, TimeUnit.SECONDS);
    }

    if (config.isRequestTiming()) {
      int requestFreqInSecs = defaultRequestFreqInSecs(config);
      executor.scheduleAtFixedRate(new WriteRequestTimings(), requestFreqInSecs, requestFreqInSecs, TimeUnit.SECONDS);
    }
  }

  /**
   * Helper method that provides a default RequestTimingReporter if not specified.
   */
  protected static RequestTimingReporter defaultReqReporter(MetricReportConfig config) {

    if (config.getRequestTimingReporter() != null) {
      return config.getRequestTimingReporter();
    }
    // just use the default implementation based on config
    RequestTimingReporter fileReporter = new RequestFileReporter(config);
    return new BaseRequestTimingReporter(fileReporter, config.getRequestTimingListeners());
  }

  /**
   * Helper method that provides a default RequestTimingReporter if not specified.
   */
  protected static MetricReporter defaultReporter(MetricReportConfig config) {
    if (config.getLocalReporter() != null) {
      return config.getLocalReporter();
    }
    return new FileReporter(config.getDirectory(), config.getMetricsFileName(), new CsvReportWriter(config.getThresholdMean()));
  }

  /**
   * Helper method that provides a default ScheduledExecutorService if not specified.
   */
  protected static ScheduledExecutorService defaultExecutor(ScheduledExecutorService executor) {
    return (executor != null) ? executor : Executors.newScheduledThreadPool(1, new BasicThreadFactory());
  }

  /**
   * Helper method to default the freqInSeconds used for request collection.
   */
  protected static int defaultRequestFreqInSecs(MetricReportConfig config) {
    int freqInSeconds = config.getRequestsFreqInSeconds();
    return freqInSeconds > 1 ? freqInSeconds : 3;
  }

  public void shutdown() {
    if (executor != null) {
      executor.shutdown();
    }
  }

  /**
   * Periodic task that reads the collected request timings and sends them to the
   * appropriate reporter.
   */
  protected class WriteRequestTimings implements Runnable {
    public void run() {
      reportRequestTimings();
    }
  }

  /**
   * Reads the collected request timings and sends them to the reporter.
   */
  private void reportRequestTimings() {

    try {
      // read and remove any collected request timings
      List<RequestTiming> requestTimings = MetricManager.collectRequestTimings();
      if (!requestTimings.isEmpty() && requestTimingReporter != null) {
        // write the request timings out to file log typically
        requestTimingReporter.report(requestTimings);
      }
    } catch (Exception e) {
      logger.error("Error reporting request timing", e);
    }
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

      } catch (Exception e) {
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
    if (requestTimingReporter != null) {
      requestTimingReporter.cleanup();
    }
  }

  /**
   * Report all the metrics.
   * <p/>
   * This typically means appending the metrics to a file or sending over a network.
   */
  protected void reportMetrics() throws IOException {

    long startNanos = System.nanoTime();
    long collectionTime = System.currentTimeMillis();
    
    // collect all the 'non-empty' metrics 
    List<Metric> metrics = collectMetrics();

    long collectNanos = System.nanoTime() - startNanos;

    // report metrics locally and remotely as necessary
    ReportMetrics reportMetrics = new ReportMetrics(headerInfo, collectionTime, metrics);
    report(reportMetrics, localReporter);
    report(reportMetrics, remoteReporter);

    long reportNanos = System.nanoTime() - startNanos - collectNanos;

    if (logger.isDebugEnabled()) {
      logger.debug("reported [{}] metrics - collectMicros:{} reportMicros:{}", metrics.size(), asMicros(collectNanos), asMicros(reportNanos));
    }
  }

  private static long asMicros(long collectNanos) {
    return TimeUnit.MICROSECONDS.convert(collectNanos, TimeUnit.NANOSECONDS);
  }

  /**
   * Collect all the non-empty metrics and return them for reporting.
   */
  protected static List<Metric> collectMetrics() {

    List<Metric> metrics = sort(MetricManager.collectNonEmptyJvmMetrics());
    List<Metric> otherMetrics = sort(MetricManager.collectNonEmptyMetrics());
    metrics.addAll(otherMetrics);
    return metrics;
  }

  /**
   * Sort the metrics into name order.
   */
  protected static List<Metric> sort(Collection<Metric> metrics) {

    ArrayList<Metric> sortedList = new ArrayList<>(metrics);
    Collections.sort(sortedList, NAME_COMPARATOR);
    return sortedList;
  }

  /**
   * Visit the metrics sorted by name.
   */
  protected static void report(ReportMetrics reportMetrics, MetricReporter reporter) {

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

  /**
   * The default thread factory
   */
  protected static class BasicThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    BasicThreadFactory() {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      namePrefix = "metric-";
    }

    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(),0);
      if (t.isDaemon())
        t.setDaemon(false);
      if (t.getPriority() != Thread.NORM_PRIORITY)
        t.setPriority(Thread.NORM_PRIORITY);
      return t;
    }
  }
}
