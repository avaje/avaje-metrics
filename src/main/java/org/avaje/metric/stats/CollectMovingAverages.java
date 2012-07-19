package org.avaje.metric.stats;

import static org.avaje.metric.NumFormat.onedp;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.Clock;
import org.avaje.metric.Stats;

/**
 * Measures average throughput with one minute, five minute, and fifteen minute
 * exponentially weighted moving average.
 * <p>
 * This is typically moving averages of "Events per Minute" or
 * "Total execution time per minute" or "Total bytes per minute" where the
 * rateUnit can be Minutes or Seconds or Hours etc depending on what is being
 * measured.
 * </p>
 * 
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
public class CollectMovingAverages implements Stats.MovingAverages {

  private final EWMA m1Rate = EWMA.oneMinuteEWMA();
  private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
  private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();
  private final EWMA s10Rate = EWMA.sec10EWMA();

  private final AtomicLong count = new AtomicLong();
  private final long startTimeNanos;
  private final TimeUnit rateUnit;
  private final Clock clock;

  private final String rateDescriptionSuffix;

  public CollectMovingAverages(String rateDescription, TimeUnit rateUnit, Clock clock) {

    this.rateUnit = rateUnit;
    this.clock = clock;
    this.startTimeNanos = this.clock.getTickNanos();
    this.rateDescriptionSuffix = rateDescription + "/"
        + rateUnit.name().toLowerCase().substring(0, rateUnit.name().length() - 1);
  }

  public String toString() {
    return "count:" + getCount() + " 1min:" + getOneMinuteDisplay() + " mean:"
        + onedp(getMeanRate());
  }

  @Override
  public TimeUnit getRateUnit() {
    return rateUnit;
  }

  public String getOneMinuteDisplay() {
    return getDescription(getOneMinuteRate());
  }

  public String getTenSecondRateDisplay() {
    return onedp(getTenSecondRate());
  }

  public String getDescription(double value) {
    return onedp(value) + " " + rateDescriptionSuffix;
  }

  public void clear() {
    count.set(0);
    s10Rate.clear();
    m1Rate.clear();
    m5Rate.clear();
    m15Rate.clear();
  }

  public void update(long n) {
    count.addAndGet(n);
    s10Rate.update(n);
    m1Rate.update(n);
    m5Rate.update(n);
    m15Rate.update(n);
  }

  public void updateAndTick(long n) {
    count.addAndGet(n);
    s10Rate.updateAndTick(n);
    m1Rate.updateAndTick(n);
    m5Rate.updateAndTick(n);
    m15Rate.updateAndTick(n);
  }

  public void tick() {
    s10Rate.tick();
    m1Rate.tick();
    m5Rate.tick();
    m15Rate.tick();
  }

  @Override
  public long getCount() {
    return count.get();
  }

  @Override
  public double getTenSecondRate() {
    return s10Rate.getRate(rateUnit);
  }

  @Override
  public double getFifteenMinuteRate() {
    return m15Rate.getRate(rateUnit);
  }

  @Override
  public double getFiveMinuteRate() {
    return m5Rate.getRate(rateUnit);
  }

  @Override
  public double getOneMinuteRate() {
    return m1Rate.getRate(rateUnit);
  }

  @Override
  public double getMeanRate() {
    if (getCount() == 0) {
      return 0.0;
    } else {
      final long elapsed = (clock.getTickNanos() - startTimeNanos);
      return convertNsRate(getCount() / (double) elapsed);
    }
  }

  private double convertNsRate(double ratePerNs) {
    return ratePerNs * (double) rateUnit.toNanos(1);
  }
}
