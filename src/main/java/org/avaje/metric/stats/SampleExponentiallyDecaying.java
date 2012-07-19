package org.avaje.metric.stats;

import static java.lang.Math.exp;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.avaje.metric.Clock;
import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.Stats;

/**
 * An exponentially-decaying random sample of {@code long}s. Uses Cormode et
 * al's forward-decaying priority reservoir sampling method to produce a
 * statistically representative sample, exponentially biased towards newer
 * entries.
 * 
 * @see <a href=
 *      "http://www.research.att.com/people/Cormode_Graham/library/publications/CormodeShkapenyukSrivastavaXu09.pdf"
 *      > Cormode et al. Forward Decay: A Practical Time Decay Model for
 *      Streaming Systems. ICDE '09: Proceedings of the 2009 IEEE International
 *      Conference on Data Engineering (2009)</a>
 */
public class SampleExponentiallyDecaying implements Sample {
  private static final long RESCALE_THRESHOLD = TimeUnit.HOURS.toNanos(1);
  private final ConcurrentSkipListMap<Double, Long> values;
  private final ReentrantReadWriteLock lock;
  private final double alpha;
  private final int reservoirSize;
  private final AtomicLong count = new AtomicLong(0);
  private volatile long startTime;
  private final AtomicLong nextScaleTime = new AtomicLong(0);
  private final Clock clock;

  /**
   * Creates a new {@link SampleExponentiallyDecaying}.
   * 
   * @param reservoirSize
   *          the number of samples to keep in the sampling reservoir
   * @param alpha
   *          the exponential decay factor; the higher this is, the more biased
   *          the sample will be towards newer values
   */
  public SampleExponentiallyDecaying(int reservoirSize, double alpha) {
    this(reservoirSize, alpha, Clock.defaultClock());
  }

  /**
   * Creates a new {@link SampleExponentiallyDecaying}.
   * 
   * @param reservoirSize
   *          the number of samples to keep in the sampling reservoir
   * @param alpha
   *          the exponential decay factor; the higher this is, the more biased
   *          the sample will be towards newer values
   */
  public SampleExponentiallyDecaying(int reservoirSize, double alpha, Clock clock) {
    this.values = new ConcurrentSkipListMap<Double, Long>();
    this.lock = new ReentrantReadWriteLock();
    this.alpha = alpha;
    this.reservoirSize = reservoirSize;
    this.clock = clock;
    clear();
  }

  @Override
  public void clear() {
    lockForRescale();
    try {
      values.clear();
      count.set(0);
      this.startTime = currentTimeInSeconds();
      nextScaleTime.set(clock.getTickNanos() + RESCALE_THRESHOLD);
    } finally {
      unlockForRescale();
    }
  }

  @Override
  public int size() {
    return (int) min(reservoirSize, count.get());
  }

  // @Override
  // public void update(long value) {
  // update(value, currentTimeInSeconds());
  // }

  @Override
  public void update(MetricValueEvent event) {

    update(event.getValue(), TimeUnit.MILLISECONDS.toSeconds(event.getEventTime()));
  }

  /**
   * Adds an old value with a fixed timestamp to the sample.
   * 
   * @param value
   *          the value to be added
   * @param currentTimeSecs
   *          the epoch timestamp of {@code value} in seconds
   */
  public void update(long value, long currentTimeSecs) {

    rescaleIfNeeded();

    lockForRegularUsage();
    try {
      final double priority = weight(currentTimeSecs - startTime)
          / ThreadLocalRandom.current().nextDouble();
      final long newCount = count.incrementAndGet();
      if (newCount <= reservoirSize) {
        values.put(priority, value);
      } else {
        Double first = values.firstKey();
        if (first < priority) {
          if (values.putIfAbsent(priority, value) == null) {
            // ensure we always remove an item
            while (values.remove(first) == null) {
              first = values.firstKey();
            }
          }
        }
      }
    } finally {
      unlockForRegularUsage();
    }

  }

  private void rescaleIfNeeded() {
    final long now = clock.getTickNanos();
    final long next = nextScaleTime.get();
    if (now >= next) {
      rescale(now, next);
    }
  }

  @Override
  public Stats.Percentiles getSnapshot() {
    lockForRegularUsage();
    try {
      return new PercentilesSnapshot(values.values());
    } finally {
      unlockForRegularUsage();
    }
  }

  private long currentTimeInSeconds() {
    return TimeUnit.MILLISECONDS.toSeconds(clock.getTimeMillis());
  }

  private double weight(long t) {
    return exp(alpha * t);
  }

  /*
   * "A common feature of the above techniques—indeed, the key technique that
   * allows us to track the decayed weights efficiently—is that they maintain
   * counts and other quantities based on g(ti − L), and only scale by g(t − L)
   * at query time. But while g(ti −L)/g(t−L) is guaranteed to lie between zero
   * and one, the intermediate values of g(ti − L) could become very large. For
   * polynomial functions, these values should not grow too large, and should be
   * effectively represented in practice by floating point values without loss
   * of precision. For exponential functions, these values could grow quite
   * large as new values of (ti − L) become large, and potentially exceed the
   * capacity of common floating point types. However, since the values stored
   * by the algorithms are linear combinations of g values (scaled sums), they
   * can be rescaled relative to a new landmark. That is, by the analysis of
   * exponential decay in Section III-A, the choice of L does not affect the
   * final result. We can therefore multiply each value based on L by a factor
   * of exp(−α(L′ − L)), and obtain the correct value as if we had instead
   * computed relative to a new landmark L′ (and then use this new L′ at query
   * time). This can be done with a linear pass over whatever data structure is
   * being used."
   */
  private void rescale(long now, long next) {
    if (nextScaleTime.compareAndSet(next, now + RESCALE_THRESHOLD)) {
      lockForRescale();
      try {
        final long oldStartTime = startTime;
        this.startTime = currentTimeInSeconds();
        final ArrayList<Double> keys = new ArrayList<Double>(values.keySet());
        for (Double key : keys) {
          final Long value = values.remove(key);
          values.put(key * exp(-alpha * (startTime - oldStartTime)), value);
        }

        // make sure the counter is in sync with the number of stored samples.
        count.set(values.size());
      } finally {
        unlockForRescale();
      }
    }
  }

  private void unlockForRescale() {
    lock.writeLock().unlock();
  }

  private void lockForRescale() {
    lock.writeLock().lock();
  }

  private void lockForRegularUsage() {
    lock.readLock().lock();
  }

  private void unlockForRegularUsage() {
    lock.readLock().unlock();
  }
}
