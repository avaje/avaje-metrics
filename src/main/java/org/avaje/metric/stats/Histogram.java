package org.avaje.metric.stats;

import java.util.List;

import org.avaje.metric.MetricValueEvent;
import org.avaje.metric.Stats;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately computing running
 *      variance</a>
 */
public class Histogram {
  
    private static final int DEFAULT_SAMPLE_SIZE = 1028;
    private static final double DEFAULT_ALPHA = 0.015;

    /**
     * The type of sampling the histogram should be performing.
     */
    enum SampleType {
        /**
         * Uses a uniform sample of 1028 elements, which offers a 99.9% confidence level with a 5%
         * margin of error assuming a normal distribution.
         */
        UNIFORM {
            @Override
            public Sample newSample() {
                return new SampleUniform(DEFAULT_SAMPLE_SIZE);
            }
        },

        /**
         * Uses an exponentially decaying sample of 1028 elements, which offers a 99.9% confidence
         * level with a 5% margin of error assuming a normal distribution, and an alpha factor of
         * 0.015, which heavily biases the sample to the past 5 minutes of measurements.
         */
        BIASED {
            @Override
            public Sample newSample() {
                return new SampleExponentiallyDecaying(DEFAULT_SAMPLE_SIZE, DEFAULT_ALPHA);
            }
        };

        public abstract Sample newSample();
    }

    private final Sample sample;


    /**
     * Creates a new {@link Histogram} with the given sample type.
     *
     * @param type the type of sample to use
     */
    Histogram(SampleType type) {
        this(type.newSample());
    }
    
    public Histogram() {
      this(SampleType.BIASED);
    }

    /**
     * Creates a new {@link Histogram} with the given sample.
     *
     * @param sample the sample to create a histogram from
     */
    Histogram(Sample sample) {
        this.sample = sample;
        clear();
    }

    /**
     * Clears all recorded values.
     */
    public void clear() {
        sample.clear();
    }

    public void update(List<? extends MetricValueEvent> events) {
      for (int i = 0; i < events.size(); i++) {
        update(events.get(i));
      }
    }
    
    private void update(MetricValueEvent event) {
        
      sample.update(event);
    }

    public Stats.Percentiles getSnapshot() {
        return sample.getSnapshot();
    }


}
