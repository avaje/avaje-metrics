package org.avaje.metric;

/**
 * Statistics collected on metrics.
 * <p>
 * The statistics collected are intended to provide a good representation of the
 * metrics current recent activity (10 seconds, 1 minute) as well as longer term
 * activity (5 minute, 15 minute). This enables the user to collect statistics
 * passively (every 1 minute or every 5 minutes) or view the immediate activity
 * (last 10 seconds).
 * </p>
 */
public interface MetricStatistics {

  /**
   * Return the moving averages for the rate events are occurring.
   */
  public Stats.MovingAverages getEventRate();

  /**
   * Return the moving averages for the rate that load (execution time, bytes,
   * rows etc) is occurring.
   */
  public Stats.MovingAverages getWorkRate();

  /**
   * Return the moving 5 minute summary statistics (minimum value over the last 5 minutes, maximum value of the last 5 minutes).
   */
  public Stats.Summary getSummary();

}