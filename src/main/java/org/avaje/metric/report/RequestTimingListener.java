package org.avaje.metric.report;

import org.avaje.metric.RequestTiming;

/**
 * Listener that can be notified when a RequestTiming is reported.
 */
public interface RequestTimingListener {

  /**
   * Listener notified for each RequestTiming that has been reported.
   */
  void onRequestTiming(RequestTiming requestTiming);

}
