package org.avaje.metric;


/**
 * Builder to get metrics using extra parameters such as rateUnit and clock.
 */
public interface MetricBuilder {

  /**
   * Set the metric name.
   */
  public MetricBuilder setName(MetricName name);

  /**
   * Set the metric name.
   */
  public MetricBuilder setName(String group, String type, String name);

  /**
   * Set the metric name.
   */
  public MetricBuilder setName(Class<?> cls, String name);

  /**
   * Set the clock to use. The clock defaults to {@link Clock#defaultClock()}.
   */
  public MetricBuilder setClock(Clock clock);
  
  /**
   * Return the CounterMetric using the builder parameters.
   */
  public CounterMetric getCounterMetric();

  /**
   * Return the LoadMetric using the builder parameters.
   */
  public LoadMetric getLoadMetric();

  /**
   * Return the TimedMetric using the builder parameters.
   */
  public TimedMetric getTimedMetric();

  /**
   * Return the ValueMetric using the builder parameters.
   */
  public ValueMetric getValueMetric();

}