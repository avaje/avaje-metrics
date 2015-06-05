package org.avaje.metric.report;

import org.avaje.metric.RequestTiming;

import java.util.List;

/**
 * Reports the request timings that have been collected.
 */
public interface RequestTimingReporter {

  /**
   * Report the request timing events.
   */
  void report(List<RequestTiming> requestTimings);

  /**
   * Perform periodic cleanup of any resources (e.g. only keep x days of metrics files).
   * <p>
   * By default this will be called approximately every 8 hours and is intended to be used to cleanup old files
   * created by the likes of FileReporter (only keep x days of metrics files).
   */
  void cleanup();
}
