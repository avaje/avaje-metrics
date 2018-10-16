package org.avaje.metric.core.log4j;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.avaje.metric.CounterMetric;
import org.avaje.metric.MetricManager;

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
    super("metrics", null, null);
    this.errorMetric = MetricManager.getCounterMetric(errorName);
    this.warnMetric = MetricManager.getCounterMetric(warnName);
  }

  @Override
  public void append(LogEvent event) {
    switch (event.getLevel().getStandardLevel()) {
      case WARN:
        warnMetric.markEvent();
        break;
      case ERROR:
        errorMetric.markEvent();
        break;
      case FATAL:
        errorMetric.markEvent();
        break;
      default:
        break;
    }
  }
}
