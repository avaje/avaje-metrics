package org.avaje.metric;

import java.util.concurrent.TimeUnit;

public interface Stats {

  public interface Summary {

    public long getSinceSeconds();

    public long getStartTime();

    public long getCount();

    public double getSum();

    public double getMax();

    public double getMin();

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

    public TimeUnit getRateUnit();

    public long getCount();

    public double getFifteenMinuteRate();

    public double getFiveMinuteRate();

    public double getOneMinuteRate();

    public double getTenSecondRate();

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
