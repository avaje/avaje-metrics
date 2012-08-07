package org.avaje.metric;

import java.util.concurrent.TimeUnit;

/**
 * Returns common TimeUnits in abbreviated form.
 */
public class TimeUnitAbbreviation {

  /**
   * Return the abbreviation for common time units (nanoseconds to days).
   */
  public static String toAbbr(TimeUnit timeUnit) {
    switch (timeUnit) {
    case NANOSECONDS: return "ns";
    case MICROSECONDS: return "us";
    case MILLISECONDS: return "ms";
    case SECONDS: return "sec";
    case MINUTES: return "min";
    case HOURS: return "hr";
    case DAYS: return "day";
    default:
      return timeUnit.name().toLowerCase();
    }
  }
  
}
