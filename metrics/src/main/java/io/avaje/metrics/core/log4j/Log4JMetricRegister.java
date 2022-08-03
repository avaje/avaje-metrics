package io.avaje.metrics.core.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

/**
 * Register the standard Log4JMetricAppender.
 */
public class Log4JMetricRegister {

  /**
   * Register with the given name for the error and warning metrics.
   */
  public static void registerWith(String errorName, String warnName) {

    Log4JMetricAppender appender = new Log4JMetricAppender(errorName, warnName);
    appender.start();

    LoggerContext context = (LoggerContext) LogManager.getContext(false);
    Configuration config = context.getConfiguration();
    config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).addAppender(appender, Level.WARN, null);
    context.updateLoggers(config);
  }
}
