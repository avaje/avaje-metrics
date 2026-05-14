package io.avaje.metrics.core;

import io.avaje.metrics.Timer;
import io.avaje.metrics.spi.SpiTimedSpanFactory;
import org.jspecify.annotations.Nullable;

interface TraceableTimer {

  /**
   * Return this timer with tracing enabled if supported.
   */
  Timer withTracing(@Nullable SpiTimedSpanFactory timedSpanFactory);
}
