package org.avaje.metric;

/**
 * Statistics collected on metrics.
 * <p>
 * The statistics collected are intended to provide a good representation of the
 * metrics current activity (10 seconds) as well as relatively recent activity
 * (1 minute and 5 minute ). This enables the user to collect statistics
 * passively (every 1 minute or every 5 minutes) or view the immediate activity
 * (last 10 seconds).
 * </p>
 */
public interface MetricStatistics {

  /**
   * Return the one minute and five minute moving summary statistics (minimum
   * and maximum values over the last 1-2 minutes and 5-6 minutes).
   */
  public ValueStatistics getValueStatistics(boolean reset);

  public ValueStatistics getValueStatistics();
}