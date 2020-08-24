package io.avaje.metrics;

import java.util.List;

/**
 * Holds the details for a request including it's timing entries.
 */
public interface RequestTiming {

  /**
   * Set the external request id (typically obtaining from MDC logging context).
   * <p>
   * This is an optional but useful to be able to relate the request timing output
   * to a specific application request.
   * </p>
   */
  void setExternalRequestId(String externalRequestId);

  /**
   * Return the external request id if it had been set.
   * <p>
   * This is an optional but useful to be able to relate the request timing output
   * to a specific application request.
   * </p>
   */
  String getExternalRequestId();

  /**
   * Return the time the request was reported.
   */
  long getReportTime();

  /**
   * Return the entries for the request.
   */
  List<RequestTimingEntry> getEntries();

}
