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
   * <p>
   * Collection of summary values over 1 minute intervals up to 6 minutes
   * provides us with a reasonable measure of the min and max range of values
   * over the last 5 minutes.
   * </p>
   */
  public interface Summary {

    /**
     * Return the time to now in seconds this summary has been collected for.
     */
    public long getSinceSeconds();

    /**
     * Return the start time for this summary.
     */
    public long getStartTime();
    
    public double getEventRate(TimeUnit rateUnit);

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
     * Return the minium value.
     */
    public double getMin();

    /**
     * Return the mean value.
     */
    public double getMean();
  }

  /**
   * Moving summary of min/max range of values over short 1-2 minute period and
   * longer 5-6 minute period.
   * <p>
   * This provides something similar to percentiles.
   * </p>
   */
  public interface MovingSummary {

    /**
     * Return the moving summary over the last 1-2 minutes.
     */
    public Summary getOneMinuteSummary();

    /**
     * Return the moving summary over the last 5-6 minutes.
     */
    public Summary getFiveMinuteSummary();
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
