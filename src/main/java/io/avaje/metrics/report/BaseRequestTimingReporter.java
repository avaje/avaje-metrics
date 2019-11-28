package io.avaje.metrics.report;

import io.avaje.metrics.RequestTiming;

import java.util.Collections;
import java.util.List;

/**
 * Combines a RequestTimingReporter with a list of RequestTimingListener.
 */
public class BaseRequestTimingReporter implements RequestTimingReporter {

  final RequestTimingReporter reporter;

  final List<RequestTimingListener> listeners;

  public BaseRequestTimingReporter(RequestTimingReporter reporter, List<RequestTimingListener> listeners) {
    this.reporter = reporter;
    this.listeners = (listeners != null) ? listeners : Collections.EMPTY_LIST;
  }

  @Override
  public void report(List<RequestTiming> requestTimings) {
    if (reporter != null) {
      reporter.report(requestTimings);
    }
    if (!listeners.isEmpty()) {
      // notify all the listeners
      for (int t = 0; t < requestTimings.size(); t++) {
        RequestTiming requestTiming = requestTimings.get(t);
        for (int l = 0; l < listeners.size(); l++) {
          listeners.get(l).onRequestTiming(requestTiming);
        }
      }
    }
  }

  @Override
  public void cleanup() {
    if (reporter != null) {
      reporter.cleanup();
    }
  }
}
