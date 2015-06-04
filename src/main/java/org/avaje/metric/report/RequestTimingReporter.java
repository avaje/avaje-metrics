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
}
