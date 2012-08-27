package org.avaje.metric;

import java.util.concurrent.TimeUnit;

/**
 * Moving averages based on various time intervals from 10 seconds (for
 * immediate activity) through to 15 minutes.
 * <p>
 * This is similar to how load is reported on unix.
 * </p>
 * <p>
 * References: An exponentially-weighted moving average.
 * 
 * @see <a href="http://www.teamquest.com/pdfs/whitepaper/ldavg1.pdf">UNIX Load
 *      Average Part 1: How It Works</a>
 * @see <a href="http://www.teamquest.com/pdfs/whitepaper/ldavg2.pdf">UNIX Load
 *      Average Part 2: Not Your Average Average</a> </p>
 */
public interface MovingAverageStatistics {

  /**
   * Return the Timeunit used to scale the rates (per hour, minute, second etc).
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
