package org.avaje.metric.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.Stats;

/**
 * A random sample of a stream of {@code long}s. Uses Vitter's Algorithm R to produce a
 * statistically representative sample.
 *
 * @see <a href="http://www.cs.umd.edu/~samir/498/vitter.pdf">Random Sampling with a Reservoir</a>
 */
public class SampleUniform implements Sample {
    private static final int BITS_PER_LONG = 63;
    private final AtomicLong count = new AtomicLong();
    private final AtomicLongArray values;

    /**
     * Creates a new {@link SampleUniform}.
     *
     * @param reservoirSize the number of samples to keep in the sampling reservoir
     */
    public SampleUniform(int reservoirSize) {
        this.values = new AtomicLongArray(reservoirSize);
        clear();
    }

    @Override
    public void clear() {
        for (int i = 0; i < values.length(); i++) {
            values.set(i, 0);
        }
        count.set(0);
    }

    @Override
    public int size() {
        final long c = count.get();
        if (c > values.length()) {
            return values.length();
        }
        return (int) c;
    }

    
    
    @Override
    public void update(MetricValueEvent event) {
      update(event.getValue());
    }

    //@Override
    public void update(long value) {
        final long c = count.incrementAndGet();
        if (c <= values.length()) {
            values.set((int) c - 1, value);
        } else {
            final long r = nextLong(c);
            if (r < values.length()) {
                values.set((int) r, value);
            }
        }
    }

    /**
     * Get a pseudo-random long uniformly between 0 and n-1. Stolen from
     * {@link java.util.Random#nextInt()}.
     *
     * @param n the bound
     * @return a value select randomly from the range {@code [0..n)}.
     */
    private static long nextLong(long n) {
        long bits, val;
        do {
            bits = ThreadLocalRandom.current().nextLong() & (~(1L << BITS_PER_LONG));
            val = bits % n;
        } while (bits - val + (n - 1) < 0L);
        return val;
    }

    @Override
    public Stats.Percentiles getSnapshot() {
        final int s = size();
        final List<Long> copy = new ArrayList<Long>(s);
        for (int i = 0; i < s; i++) {
            copy.add(values.get(i));
        }
        return new PercentilesSnapshot(copy);
    }
}
