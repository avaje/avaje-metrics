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
    this.listeners = (listeners != null) ? listeners : Collections.emptyList();
  }

  @Override
  public void report(List<RequestTiming> requestTimings) {
    if (reporter != null) {
      reporter.report(requestTimings);
    }
    if (!listeners.isEmpty()) {
      // notify all the listeners
      for (RequestTiming requestTiming : requestTimings) {
        for (RequestTimingListener listener : listeners) {
          listener.onRequestTiming(requestTiming);
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
