package io.avaje.metrics.core.log4j;

import io.avaje.metrics.CounterMetric;
import io.avaje.metrics.MetricManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

/**
 * Log4J metric appender. Counts error and warning log messages.
 */
public class Log4JMetricAppender extends AbstractAppender {

  private final CounterMetric errorMetric;

  private final CounterMetric warnMetric;

  public Log4JMetricAppender() {
    this("app.log.error", "app.log.warn");
  }

  public Log4JMetricAppender(String errorName, String warnName) {
    super("metrics", null, null, true, Property.EMPTY_ARRAY);
    this.errorMetric = MetricManager.counter(errorName);
    this.warnMetric = MetricManager.counter(warnName);
  }

  @Override
  public void append(LogEvent event) {
    switch (event.getLevel().getStandardLevel()) {
      case WARN:
        warnMetric.inc();
        break;
      case ERROR:
      case FATAL:
        errorMetric.inc();
        break;
      default:
        break;
    }
  }
}
