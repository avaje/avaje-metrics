package org.avaje.metric.stats;

import static java.lang.Math.exp;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An exponentially-weighted moving average.
 * 
 * @see <a href="http://www.teamquest.com/pdfs/whitepaper/ldavg1.pdf">UNIX Load
 *      Average Part 1: How It Works</a>
 * @see <a href="http://www.teamquest.com/pdfs/whitepaper/ldavg2.pdf">UNIX Load
 *      Average Part 2: Not Your Average Average</a>
 */
class EWMA {

  private static final double MILLIS_PER_SEC = 1000d;
  private static final double SECONDS_PER_MINUTE = 60.0;
  private static final int ONE_MINUTE = 1;
  private static final int FIVE_MINUTES = 5;
  private static final int FIFTEEN_MINUTES = 15;

  private final double windowDuration;
  
  private final AtomicLong uncountedEvents = new AtomicLong();
  
  private volatile double rate = 0.0;

  private long lastUpdateTimeNanos;

  /**
   * Return a new EWMA based on a 10 second moving average.
   */
  public static EWMA sec10EWMA() {
    return new EWMA(0.161616d);
  }
  
  /**
   * Return a new EWMA which is equivalent to the UNIX one minute load average.
   */
  public static EWMA oneMinuteEWMA() {
    return new EWMA(ONE_MINUTE);
  }

  /**
   * Return a new EWMA which is equivalent to the UNIX five minute load average.
   */
  public static EWMA fiveMinuteEWMA() {
    return new EWMA(FIVE_MINUTES);
  }

  /**
   * Return a new EWMA which is equivalent to the UNIX fifteen minute load.
   */
  public static EWMA fifteenMinuteEWMA() {
    return new EWMA(FIFTEEN_MINUTES);
  }

  /**
   * Return a new EWMA with smoothing based on the window minutes.
   * 
   * @param minutes
   *          the number of minutes over which to smooth the average
   */
  public EWMA(double minutes) {
    this.windowDuration = MILLIS_PER_SEC * SECONDS_PER_MINUTE * (minutes * ONE_MINUTE);
  }

  public void clear() {
    uncountedEvents.set(0);
    rate = 0.0d;
  }

  /**
   * Update the moving average with a new value.
   * 
   * @param n
   *          the new value
   */
  public void update(long n) {
    uncountedEvents.addAndGet(n);
  }

  public void updateAndTick(long n) {
    uncountedEvents.addAndGet(n);
    tick();
  }

  /**
   * Call this relatively often (every 2 to 10 seconds) to keep accurate.
   */
  public void tick() {

    final long eventCount = uncountedEvents.getAndSet(0);

    final long nowNanos = System.nanoTime();
    final long intervalNanos = nowNanos - lastUpdateTimeNanos;
    lastUpdateTimeNanos = nowNanos;

    final long intervalMillis = TimeUnit.NANOSECONDS.toMillis(intervalNanos);
    final double instantRate = (eventCount * MILLIS_PER_SEC) / intervalMillis;

    double alpha2 = 1 - exp(-intervalMillis / windowDuration);
    rate += (alpha2 * (instantRate - rate));
  }

  /**
   * Returns the rate in the given units of time.
   */
  public double getRate(TimeUnit rateUnit) {
    return rate * (double) rateUnit.toSeconds(1);
  }

}
