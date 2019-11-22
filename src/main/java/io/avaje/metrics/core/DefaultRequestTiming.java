package io.avaje.metrics.core;

import io.avaje.metrics.RequestTiming;
import io.avaje.metrics.RequestTimingEntry;

import java.util.List;

/**
 * Holds the pre request timing entries and the time it was reported.
 */
class DefaultRequestTiming implements RequestTiming {

  final List<RequestTimingEntry> entries;

  final long reportTime;

  String externalRequestId;

  DefaultRequestTiming(List<RequestTimingEntry> entries, long reportTime) {
    this.entries = entries;
    this.reportTime = reportTime;
  }

  public List<RequestTimingEntry> getEntries() {
    return entries;
  }

  public long getReportTime() {
    return reportTime;
  }

  public String getExternalRequestId() {
    return externalRequestId;
  }

  @Override
  public void setExternalRequestId(String externalRequestId) {
    this.externalRequestId = externalRequestId;
  }
}
