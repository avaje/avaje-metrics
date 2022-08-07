package io.avaje.metrics.core.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.avaje.metrics.Counter;
import io.avaje.metrics.MetricManager;

/**
 * Logback appender that counts errors and warnings.
 */
public final class LogbackMetricAppender extends AppenderBase<ILoggingEvent> {

  private final Counter errorMetric;

  private final Counter warnMetric;

  public LogbackMetricAppender() {
    this("app.log.error", "app.log.warn");
  }

  public LogbackMetricAppender(String errorName, String warnName) {
    this.errorMetric = MetricManager.counter(errorName);
    this.warnMetric = MetricManager.counter(warnName);
  }

  /**
   * Increment the warning or error counters.
   */
  @Override
  protected void append(ILoggingEvent event) {
    switch (event.getLevel().toInt()) {
      case Level.WARN_INT:
        warnMetric.inc();
        break;
      case Level.ERROR_INT:
        errorMetric.inc();
        break;
      default:
        // not interested in any other logging events
        break;
    }
  }

}
