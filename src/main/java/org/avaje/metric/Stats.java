package org.avaje.metric;

import java.util.concurrent.TimeUnit;

/**
 * Statistics calculated for the Metrics.
 */
public interface Stats {

  /**
   * Summary of values.
   * <p>
   * We are mostly interested in the min and max values which provide the range
   * of values collected (as an alternative to calculating percentiles).
   * </p>
   */
  public interface Summary {

    /**
     * Return the time to now in seconds this summary has been collected for.
     */
    public long getDuration();

    /**
     * Return the start time for this summary.
     */
    public long getStartTime();

    /**
     * Return the rate at which events are occurring (relative to the rateUnit
     * specified for the metric).
     */
    public double getEventRate();

    /**
     * Return the rate at which load is occurring (relative to the rateUnit
     * specified for the metric).
     */
    public double getLoadRate();

    /**
     * Return the total count.
     */
    public long getCount();

    /**
     * Return the total sum value.
     */
    public double getSum();

    /**
     * Return the maximum value.
     */
    public double getMax();

    /**
     * Return the minimum value.
     */
    public double getMin();

    /**
     * Return the mean value.
     */
    public double getMean();
  }

  /**
   * Moving averages based on various time intervals from 10 seconds (for
   * immediate activity) through to 15 minutes.
   * <p>
   * This is similar to how load is reported on unix.
   * </p>
   * <p>
   * References: An exponentially-weighted moving average.
   * 
   * @see <a href="http://www.teamquest.com/pdfs/whitepaper/ldavg1.pdf">UNIX
   *      Load Average Part 1: How It Works</a>
   * @see <a href="http://www.teamquest.com/pdfs/whitepaper/ldavg2.pdf">UNIX
   *      Load Average Part 2: Not Your Average Average</a> </p>
   */
  public interface MovingAverages {

    /**
     * Return the Timeunit used to scale the rates (per hour, minute, second
     * etc).
     */
    public TimeUnit getRateUnit();

    /**
     * Return the count of events.
     */
    public long getCount();

    /**
     * Return the fifteen minute moving average rate.
     */
    public double getFifteenMinuteRate();

    /**
     * Return the five minute moving average rate.
     */
    public double getFiveMinuteRate();

    /**
     * Return the one minute moving average rate.
     */
    public double getOneMinuteRate();

    /**
     * Return the ten second moving average rate.
     */
    public double getTenSecondRate();

    /**
     * Return the mean rate.
     */
    public double getMeanRate();

  }

  /**
   * Statistics from a Histogram statistical collection.
   */
  public interface Percentiles {

    /**
     * Returns the median value in the distribution.
     */
    public double getMedian();

    /**
     * Returns the value at the 75th percentile in the distribution.
     */
    public double get75thPercentile();

    /**
     * Returns the value at the 95th percentile in the distribution.
     */
    public double get95thPercentile();

    /**
     * Returns the value at the 99th percentile in the distribution.
     */
    public double get99thPercentile();

    /**
     * Returns the value at the 99.9th percentile in the distribution.
     */
    public double get999thPercentile();

  }

}
