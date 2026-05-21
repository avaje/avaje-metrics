package io.avaje.metrics.statsd;

final class MetricNames {

  private final String count;
  private final String total;
  private final String mean;
  private final String max;

  private MetricNames(String count, String total, String mean, String max) {
    this.count = count;
    this.total = total;
    this.mean = mean;
    this.max = max;
  }

  static MetricNames of(String name) {
    return new MetricNames(name + ".count", name + ".total", name + ".mean", name + ".max");
  }

  String count() {
    return count;
  }

  String total() {
    return total;
  }

  String mean() {
    return mean;
  }

  String max() {
    return max;
  }
}
