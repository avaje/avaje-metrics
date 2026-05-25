package io.avaje.metrics.prometheus;

import io.avaje.metrics.Meter;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Tags;
import io.avaje.metrics.Timer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrometheusMetricsTest {

  @Test
  void counter_isCumulativeAcrossScrapes() {
    var registry = Metrics.createRegistry();
    var counter = registry.counterBuilder("app.requests")
      .tags(Tags.of(null, "env:prod", "route:/customers/{id}", "ignored"))
      .build();
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .build();

    counter.inc(5);
    var first = prometheus.scrape();

    counter.inc(3);
    var second = prometheus.scrape();

    assertThat(first).contains(
      "# TYPE app_requests_total counter\n",
      "app_requests_total{env=\"prod\",route=\"/customers/{id}\"} 5\n");
    assertThat(second).contains("app_requests_total{env=\"prod\",route=\"/customers/{id}\"} 8\n");
  }

  @Test
  void gauges_escapeLabelsAndSanitizeNames() {
    var registry = Metrics.createRegistry();
    registry.gauge("9.jvm.memory-used")
      .tags(Tags.of("path:/a\"b\\c\nx", "status-code:200"))
      .ofDoubles(() -> 42.5D);
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .build();

    var scrape = prometheus.scrape();

    assertThat(scrape).contains(
      "# TYPE m_9_jvm_memory_used gauge\n",
      "m_9_jvm_memory_used{path=\"/a\\\"b\\\\c\\nx\",status_code=\"200\"} 42.5\n");
  }

  @Test
  void timer_writesSummaryUsingSeconds() {
    var registry = Metrics.createRegistry();
    Timer timer = registry.timer("app.service.method");
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .build();

    timer.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(5));
    timer.addEventDuration(false, TimeUnit.MILLISECONDS.toNanos(2));

    var first = prometheus.scrape();
    var second = prometheus.scrape();

    assertThat(first).contains(
      "# TYPE app_service_method_seconds summary\n",
      "app_service_method_seconds_count 1\n",
      "app_service_method_seconds_sum 0.005\n",
      "# TYPE app_service_method_error_seconds summary\n",
      "app_service_method_error_seconds_count 1\n",
      "app_service_method_error_seconds_sum 0.002\n");
    assertThat(first).doesNotContain("_max");
    assertThat(second).contains(
      "app_service_method_seconds_count 1\n",
      "app_service_method_seconds_sum 0.005\n",
      "app_service_method_error_seconds_count 1\n",
      "app_service_method_error_seconds_sum 0.002\n");
  }

  @Test
  void timer_canIncludeWindowedMaxGauge() {
    var registry = Metrics.createRegistry();
    Timer timer = registry.timer("app.service.max");
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .includeMax(true)
      .build();

    timer.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(5));
    var first = prometheus.scrape();
    var second = prometheus.scrape();

    assertThat(first).contains(
      "# TYPE app_service_max_seconds_max gauge\n",
      "app_service_max_seconds_max 0.005\n");
    assertThat(second).contains("app_service_max_seconds_max 0.0\n");
  }

  @Test
  void timerThreshold_appliesToCumulativeTotal() {
    var registry = Metrics.createRegistry();
    Timer timer = registry.timer("app.fast.method");
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .timedThresholdMicros(10_000)
      .build();

    timer.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(5));
    assertThat(prometheus.scrape()).isEmpty();

    timer.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(6));
    assertThat(prometheus.scrape()).contains(
      "app_fast_method_seconds_count 2\n",
      "app_fast_method_seconds_sum 0.011\n");
  }

  @Test
  void meter_writesCumulativeCountAndTotal() {
    var registry = Metrics.createRegistry();
    Meter meter = registry.meter("app.bytes.sent");
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .includeMax(true)
      .build();

    meter.addEvent(1024);
    meter.addEvent(2048);
    var scrape = prometheus.scrape();

    assertThat(scrape).contains(
      "# TYPE app_bytes_sent_count_total counter\n",
      "app_bytes_sent_count_total 2\n",
      "# TYPE app_bytes_sent_total counter\n",
      "app_bytes_sent_total 3072\n",
      "# TYPE app_bytes_sent_max gauge\n",
      "app_bytes_sent_max 2048\n");
  }

  @Test
  void meter_omitsMaxByDefault() {
    var registry = Metrics.createRegistry();
    Meter meter = registry.meter("app.bytes.sent.default");
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .build();

    meter.addEvent(1024);

    assertThat(prometheus.scrape())
      .contains("app_bytes_sent_default_total 1024\n")
      .doesNotContain("app_bytes_sent_default_max");
  }

  @Test
  void bucketTimer_writesHistogramBuckets() {
    var registry = Metrics.createRegistry();
    Timer timer = registry.timerBuilder("http.server.request")
      .tags(Tags.of("method:GET"))
      .bucketRanges(100, 200)
      .build();
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .build();

    timer.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(50));
    timer.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(150));
    timer.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(250));

    var scrape = prometheus.scrape();

    assertThat(scrape).contains(
      "# TYPE http_server_request_seconds histogram\n",
      "http_server_request_seconds_bucket{method=\"GET\",le=\"0.1\"} 1\n",
      "http_server_request_seconds_bucket{method=\"GET\",le=\"0.2\"} 2\n",
      "http_server_request_seconds_bucket{method=\"GET\",le=\"+Inf\"} 3\n",
      "http_server_request_seconds_count{method=\"GET\"} 3\n",
      "http_server_request_seconds_sum{method=\"GET\"} 0.45\n");
  }

  @Test
  void bucketTimer_prefixesTagWhenLeLabelExists() {
    var registry = Metrics.createRegistry();
    Timer timer = registry.timerBuilder("http.server.request")
      .tags(Tags.of("le:custom", "method:GET"))
      .bucketRanges(100)
      .build();
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .build();

    timer.addEventDuration(true, TimeUnit.MILLISECONDS.toNanos(50));

    assertThat(prometheus.scrape())
      .contains("http_server_request_seconds_bucket{tag_le=\"custom\",method=\"GET\",le=\"0.1\"} 1\n");
  }

  @Test
  void write_wrapsIoException() {
    var registry = Metrics.createRegistry();
    registry.counter("app.requests").inc();
    var prometheus = PrometheusMetrics.builder()
      .registry(registry)
      .build();

    assertThatThrownBy(() -> prometheus.write(new FailingAppendable()))
      .isInstanceOf(java.io.UncheckedIOException.class)
      .hasMessageContaining("Error writing Prometheus metrics");
  }

  @Test
  void exposesContentType() {
    assertThat(PrometheusMetrics.CONTENT_TYPE)
      .isEqualTo("text/plain; version=0.0.4; charset=utf-8");
  }

  private static final class FailingAppendable implements Appendable {

    @Override
    public Appendable append(CharSequence csq) throws IOException {
      throw new IOException("nope");
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
      throw new IOException("nope");
    }

    @Override
    public Appendable append(char c) throws IOException {
      throw new IOException("nope");
    }
  }
}
