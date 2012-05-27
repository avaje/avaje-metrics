package org.avaje.metric.stats;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.Clock;
import org.avaje.metric.MetricRateStatistics;

/**
 * A meter metric which measures mean throughput and one-, five-, and
 * fifteen-minute exponentially-weighted moving average throughputs.
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
public class RateMeter implements MetricRateStatistics {

  private final EWMA m1Rate = EWMA.oneMinuteEWMA();
  private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
  private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

  private final AtomicLong count = new AtomicLong();
  private final long startTimeNanos;
  private final TimeUnit rateUnit;
  private final String eventType;
  private final Clock clock;

  /**
     */
  public RateMeter(String eventType, TimeUnit rateUnit, Clock clock) {
    this.eventType = eventType;
    this.rateUnit = rateUnit;
    this.clock = clock;
    this.startTimeNanos = this.clock.getTickNanos();
  }

  public String toString() {
    return "count:" + getCount() + " 1min:" + getOneMinuteRate() + " 5min:" + getFiveMinuteRate() + " 15min:" + getFifteenMinuteRate()
        + " mean:" + getMeanRate() + " unit:" + getRateUnit();
  }

  
  @Override
  public TimeUnit getRateUnit() {
    return rateUnit;
  }

  public String getEventType() {
    return eventType;
  }

  public void clear() {
    count.set(0);
    m1Rate.clear();
    m5Rate.clear();
    m15Rate.clear();
  }
  
  /**
   * Mark the occurrence of a given number of events. Call this every 5 seconds
   * or so.
   * 
   * @param n
   *          the number of events
   */
  public void update(long n) {
    count.addAndGet(n);
    m1Rate.update(n);
    m5Rate.update(n);
    m15Rate.update(n);
  }

  public void updateAndTick(long n) {
    count.addAndGet(n);
    m1Rate.updateAndTick(n);
    m5Rate.updateAndTick(n);
    m15Rate.updateAndTick(n);
  }

  public void tick() {
    m1Rate.tick();
    m5Rate.tick();
    m15Rate.tick();
  }

  @Override
  public long getCount() {
    return count.get();
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

  // @Override
  // public <T> void processWith(MetricProcessor<T> processor, MetricName name,
  // T context) throws Exception {
  // processor.processMeter(name, this, context);
  // }

  private double convertNsRate(double ratePerNs) {
    return ratePerNs * (double) rateUnit.toNanos(1);
  }
}
