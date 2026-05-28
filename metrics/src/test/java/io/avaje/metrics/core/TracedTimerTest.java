package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiSpan;
import io.avaje.metrics.spi.SpiTimedSpanFactory;
import io.avaje.metrics.spi.SpiTimedSpanFactory.SpanMode;
import org.junit.jupiter.api.Test;

import static io.avaje.metrics.spi.SpiTimedSpanFactory.SpanMode.CHILD;
import static io.avaje.metrics.spi.SpiTimedSpanFactory.SpanMode.ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TracedTimerTest {

  @Test
  void tracedTimer_startEvent_success() {
    MetricRegistry registry = Metrics.createRegistry();
    Timer timer = registry.timerBuilder("app.service.method").buildTraced();

    Timer.Event event = timer.startEvent();
    event.end();

    var stats = (Timer.Stats) registry.collectMetrics().get(0);
    assertThat(stats.name()).isEqualTo("app.service.method");
    assertThat(stats.count()).isEqualTo(1);
  }

  @Test
  void withTracing_returnsImmutableTracedTimer() {
    Timer plainTimer = new DTimer(Metric.ID.of("app.service.method"));
    TestTimedSpanFactory testTimedSpanFactory = new TestTimedSpanFactory();
    Timer tracedTimer = ((TraceableTimer) plainTimer).withTracing(testTimedSpanFactory, CHILD);

    tracedTimer.time(() -> "ok");
    tracedTimer.time(() -> "ok");

    assertThat(tracedTimer).isNotSameAs(plainTimer);
    assertThat(testTimedSpanFactory.prepareCount).isEqualTo(1);
    assertThat(testTimedSpanFactory.startCount).isEqualTo(2);
    MetricRegistry registry = Metrics.createRegistry();
    registry.register(tracedTimer);
    var stats = (Timer.Stats) registry.collectMetrics().get(0);
    assertThat(stats.name()).isEqualTo("app.service.method");
    assertThat(stats.count()).isEqualTo(2);
  }

  @Test
  void tracedBucketTimer_startEvent_recordsMetrics() {
    MetricRegistry registry = Metrics.createRegistry();
    Timer timer = registry.timerBuilder("app.service.bucketed").bucketRanges(100, 200).buildTraced();

    Timer.Event event = timer.startEvent();
    event.end();

    var stats = (Timer.Stats) registry.collectMetrics().get(0);
    assertThat(stats.name()).isEqualTo("app.service.bucketed");
    assertThat(stats.count()).isEqualTo(1);
  }

  @Test
  void tracedTimer_startEvent_errorRecordsThrowable() {
    TestTimedSpanFactory testTimedSpanFactory = new TestTimedSpanFactory();
    Timer tracedTimer = ((TraceableTimer) new DTimer(Metric.ID.of("app.service.method"))).withTracing(testTimedSpanFactory, CHILD);
    var error = new IllegalStateException("boom");

    Timer.Event event = tracedTimer.startEvent();
    event.endWithError(error);

    assertThat(testTimedSpanFactory.lastSpan.error).isSameAs(error);
  }

  @Test
  void tracedTimer_time_errorRecordsThrowable() {
    TestTimedSpanFactory testTimedSpanFactory = new TestTimedSpanFactory();
    Timer tracedTimer = ((TraceableTimer) new DTimer(Metric.ID.of("app.service.method"))).withTracing(testTimedSpanFactory, CHILD);
    var error = new IllegalStateException("boom");

    assertThatThrownBy(() -> tracedTimer.time(() -> {
      throw error;
    })).isSameAs(error);

    assertThat(testTimedSpanFactory.lastSpan.error).isSameAs(error);
  }

  @Test
  void tracedBucketTimer_errorRecordsThrowable() {
    TestTimedSpanFactory testTimedSpanFactory = new TestTimedSpanFactory();
    Timer bucketTimer = new BucketTimerFactory().createMetric(Metric.ID.of("app.service.bucketed"), "", new int[]{100, 200});
    Timer tracedBucketTimer = ((TraceableTimer) bucketTimer).withTracing(testTimedSpanFactory, CHILD);
    var error = new IllegalArgumentException("bad");

    Timer.Event event = tracedBucketTimer.startEvent();
    event.endWithError(error);

    assertThat(testTimedSpanFactory.lastSpan.error).isSameAs(error);
  }

  @Test
  void withTracing_passesRootSpanMode() {
    TestTimedSpanFactory testTimedSpanFactory = new TestTimedSpanFactory();
    Timer tracedTimer = ((TraceableTimer) new DTimer(Metric.ID.of("app.service.method"))).withTracing(testTimedSpanFactory, ROOT);

    tracedTimer.time(() -> "ok");

    assertThat(testTimedSpanFactory.lastSpanMode).isEqualTo(ROOT);
  }

  private static final class TestTimedSpanFactory implements SpiTimedSpanFactory {

    private int prepareCount;
    private int startCount;
    private TestTimedSpan lastSpan;
    private SpanMode lastSpanMode;

    @Override
    public Prepared prepare(Metric.ID id, String bucketRange) {
      return prepare(id, bucketRange, CHILD);
    }

    @Override
    public Prepared prepare(Metric.ID id, String bucketRange, SpanMode spanMode) {
      prepareCount++;
      lastSpanMode = spanMode;
      return () -> {
        startCount++;
        lastSpan = new TestTimedSpan();
        return lastSpan;
      };
    }
  }

  private static final class TestTimedSpan implements SpiSpan {

    private Throwable error;

    @Override
    public void end() {
    }

    @Override
    public void endWithError() {
    }

    @Override
    public void endWithError(Throwable error) {
      this.error = error;
    }
  }
}
