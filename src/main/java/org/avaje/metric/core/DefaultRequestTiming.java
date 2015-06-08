package org.avaje.metric.core;

import org.avaje.metric.RequestTiming;
import org.avaje.metric.RequestTimingEntry;

import java.util.List;

/**
 * Holds the pre request timing entries and the time it was reported.
 */
public class DefaultRequestTiming implements RequestTiming {

  final List<RequestTimingEntry> entries;

  final long reportTime;

  String externalRequestId;

  public DefaultRequestTiming(List<RequestTimingEntry> entries, long reportTime) {
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
