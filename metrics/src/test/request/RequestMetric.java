package io.avaje.metrics.core;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;

import java.util.Map;
import java.util.function.Supplier;

final class RequestMetric implements TimedMetric {

  private final Map<String, String> attributes;
  private final MetricName name;

  RequestMetric(MetricName name) {
    this.name = name;
    this.attributes = null;
  }

  RequestMetric(MetricName name, Map<String, String> attributes) {
    this.name = name;
    this.attributes = attributes;
  }

  @Override
  public void time(Runnable event) {

  }

  @Override
  public <T> T time(Supplier<T> event) {
    return null;
  }

  @Override
  public TimedEvent startEvent() {
    return null;
  }

  @Override
  public void addEventSince(boolean success, long startNanos) {

  }

  @Override
  public void add(long startNanos) {

  }

  @Override
  public void add(long startNanos, boolean requestTiming) {

  }

  @Override
  public void addErr(long startNanos) {

  }

  @Override
  public void addErr(long startNanos, boolean requestTiming) {

  }

  @Override
  public void addEventDuration(boolean success, long durationNanos) {

  }

  @Override
  public boolean isBucket() {
    return false;
  }

  @Override
  public String bucketRange() {
    return null;
  }

  @Override
  public boolean isRequestTiming() {
    return false;
  }

  @Override
  public void setRequestTiming(int collectionCount) {

  }

  @Override
  public int getRequestTiming() {
    return 0;
  }

  @Override
  public void decrementRequestTiming() {

  }

  public Map<String, String> attributes() {
    return attributes;
  }

  @Override
  public MetricName name() {
    return null;
  }

  @Override
  public void collect(MetricStatsVisitor collector) {

  }

  @Override
  public void reset() {

  }
}
