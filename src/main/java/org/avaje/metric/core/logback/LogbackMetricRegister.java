package org.avaje.metric.core.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

/**
 * Used to register a Logback appender for error and warning level log metrics (counters).
 */
public class LogbackMetricRegister {

  public static void registerWith(String errorName, String warnName) {

    LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

    LogbackMetricAppender appender = new LogbackMetricAppender(errorName, warnName);
    appender.setContext(root.getLoggerContext());
    appender.start();

    root.addAppender(appender);
  }

}
