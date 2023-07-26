package io.avaje.metrics.report;

import io.avaje.metrics.MetricSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A bean used to configure the MetricReporter.
 */
public class MetricReportConfig {

  private HeaderInfo headerInfo;

  private int freqInSeconds;

  private int requestsFreqInSeconds;

  private String directory;

  private String metricsFileName;

  private String requestsFileName;

  private MetricReporter reporter;

//  private boolean requestTiming;
//
//  private RequestTimingReporter requestTimingReporter;

  private int requestTimingThreshold;

  private long thresholdMean;

  private ScheduledExecutorService executor;

//  private List<RequestTimingListener> requestTimingListeners = new ArrayList<>();

  private List<MetricReportAggregator> aggregators = new ArrayList<>();

  private List<MetricSupplier> suppliers = new ArrayList<>();

  /**
   * Add additional metrics supplier that can supply metrics to the reporter.
   */
  public void addSupplier(MetricSupplier supplier) {
    suppliers.add(supplier);
  }

  /**
   * Return additional suppliers.
   */
  public List<MetricSupplier> getSuppliers() {
    return suppliers;
  }

  /**
   * Add a aggregator for timed metrics starting with the given prefix.
   *
   * @param prefix The prefix to match timed metrics on
   * @param name   The name of the aggregation
   */
  public void addAggregator(String prefix, String name) {
    aggregators.add(new AggregatorTimed(prefix, name));
  }

  /**
   * Add a aggregator for timed metrics starting with the given prefix.
   * <p>
   * The name of the resulting aggreation is the prefix + ".Agg"
   * </p>
   *
   * @param prefix The prefix to match timed metrics on
   */
  public void addAggregator(String prefix) {
    addAggregator(prefix, prefix + ".Agg");
  }

  /**
   * Return the aggregators.
   */
  public List<MetricReportAggregator> getAggregators() {
    return aggregators;
  }

  /**
   * Return the HeaderInfo which identifies this application instance.
   * <p/>
   * This is the server environment information that is common to all the metrics
   * collected on this running instance and can be provided when reporting the
   * metrics to a repository.
   */
  public HeaderInfo getHeaderInfo() {
    return headerInfo;
  }

  /**
   * Set the HeaderInfo which identifies this application instance.
   * <p/>
   * This is the server environment information that is common to all the metrics
   * collected on this running instance and can be provided when reporting the
   * metrics to a repository.
   */
  public void setHeaderInfo(HeaderInfo headerInfo) {
    this.headerInfo = headerInfo;
  }

  /**
   * Return the directory to output metrics files to.
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * Set the directory to output metrics files to.
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * Return the file name to output metrics files to.
   */
  public String getMetricsFileName() {
    return metricsFileName;
  }

  /**
   * Set the file name to output metrics files to.
   */
  public void setMetricsFileName(String metricsFileName) {
    this.metricsFileName = metricsFileName;
  }

  /**
   * Return the file name used to output request timings.
   */
  public String getRequestsFileName() {
    return requestsFileName;
  }

  /**
   * Set the file name used to output request timings.
   */
  public void setRequestsFileName(String requestsFileName) {
    this.requestsFileName = requestsFileName;
  }

  /**
   * Return the threshold for request timing reporting. This is a percentage of the total
   * request time and if an entry is less than the threshold it is omitted from the output.
   * <p>
   * This could typically be set to 1 percent (or even up to 5 percent) in order to reduce
   * the output for request timing and highlight the more costly methods.
   * </p>
   */
  public int getRequestTimingThreshold() {
    return requestTimingThreshold;
  }

  /**
   * Set a threshold for request timing reporting. This is a percentage of the total
   * request time and if an entry is less than the threshold it is omitted from the output.
   * <p>
   * This could typically be set to 1 percent (or even up to 5 percent) in order to reduce
   * the output for request timing and highlight the more costly methods.
   * </p>
   *
   * @param requestTimingThreshold a percentage value (from 0 to 99) but typically between 0 and 5.
   */
  public void setRequestTimingThreshold(int requestTimingThreshold) {
    this.requestTimingThreshold = requestTimingThreshold;
  }

  /**
   * Get the executor to use. This is optional, if not specified one is created.
   */
  public ScheduledExecutorService getExecutor() {
    return executor;
  }

  /**
   * Set the executor to use. This is optional, if not specified one is created.
   */
  public void setExecutor(ScheduledExecutorService executor) {
    this.executor = executor;
  }

  /**
   * Return the frequency metrics are collected in seconds.
   */
  public int getFreqInSeconds() {
    return freqInSeconds;
  }

  /**
   * Set the frequency metrics are collected in seconds.
   */
  public void setFreqInSeconds(int freqInSeconds) {
    this.freqInSeconds = freqInSeconds;
  }

  /**
   * Return the frequency that any request timings are logged.
   * This defaults to 3 seconds if not specified.
   */
  public int getRequestsFreqInSeconds() {
    return requestsFreqInSeconds;
  }

  /**
   * Set the frequency that any request timings are logged.
   * This defaults to 3 seconds if not specified.
   */
  public void setRequestsFreqInSeconds(int requestsFreqInSeconds) {
    this.requestsFreqInSeconds = requestsFreqInSeconds;
  }

  /**
   * Return a threshold mean in microseconds that can be used to suppress
   * reporting on uninteresting metrics.
   */
  public long getThresholdMean() {
    return thresholdMean;
  }

  /**
   * Set a threshold mean in microseconds that can be used to suppress
   * reporting on uninteresting metrics.
   */
  public void setThresholdMean(long thresholdMean) {
    this.thresholdMean = thresholdMean;
  }

  /**
   * Return the local reporter to use. This is typically a reporter that writes metrics out to a local file system.
   */
  public MetricReporter getReporter() {
    return reporter;
  }

  /**
   * Set the local reporter to use. This is typically a reporter that writes metrics out to a local file system.
   */
  public void setReporter(MetricReporter reporter) {
    this.reporter = reporter;
  }

//  /**
//   * Return the reporter used to report the request timings.
//   */
//  public RequestTimingReporter getRequestTimingReporter() {
//    return requestTimingReporter;
//  }
//
//  /**
//   * Set the reporter used to report the request timings.
//   */
//  public void setRequestTimingReporter(RequestTimingReporter requestTimingReporter) {
//    this.requestTimingReporter = requestTimingReporter;
//  }
//
//  /**
//   * Add a RequestTimingListener.
//   */
//  public void addRequestTimingListener(RequestTimingListener listener) {
//    this.requestTimingListeners.add(listener);
//  }
//
//  /**
//   * Return the list of RequestTimingListener's.
//   */
//  public List<RequestTimingListener> getRequestTimingListeners() {
//    return requestTimingListeners;
//  }
//
//  /**
//   * Set the list of RequestTimingListener's.
//   */
//  public void setRequestTimingListeners(List<RequestTimingListener> requestTimingListeners) {
//    this.requestTimingListeners = requestTimingListeners;
//  }
//
//  /**
//   * Return true if request timing is on.
//   */
//  public boolean isRequestTiming() {
//    return requestTiming;
//  }
//
//  /**
//   * Set to true to collect and report request timing.
//   */
//  public void setRequestTiming(boolean requestTiming) {
//    this.requestTiming = requestTiming;
//  }
}
