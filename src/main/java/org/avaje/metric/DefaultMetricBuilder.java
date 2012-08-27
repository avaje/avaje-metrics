package org.avaje.metric;


/**
 * Default implementation of the MetricBuilder.
 */
class DefaultMetricBuilder implements MetricBuilder {

  private MetricName name;
  
  private Clock clock = Clock.defaultClock();

  @Override
  public MetricBuilder setName(MetricName name) {
    this.name = name;
    return this;
  }
  
  @Override
  public MetricBuilder setName(String group, String type, String name) {
    this.name = new MetricName(group, type, name);
    return this;
  }

  @Override
  public MetricBuilder setName(Class<?> klass, String name) {
    this.name = new MetricName(klass, name);
    return this;
  }
  
  @Override
  public MetricBuilder setClock(Clock clock) {
    this.clock = clock;
    return this;
  }

  
  @Override
  public CounterMetric getCounterMetric() {
    nameRequired();
    return MetricManager.getCounterMetric(name);
  }
  
  @Override
  public LoadMetric getLoadMetric() {
    nameRequired();
    return MetricManager.getLoadMetric(name);
  }
  
  @Override
  public TimedMetric getTimedMetric() {
    nameRequired();
    return MetricManager.getTimedMetric(name, clock);
  }

  @Override
  public ValueMetric getValueMetric() {
    nameRequired();
    return MetricManager.getValueMetric(name);
  }
  
  private void nameRequired() {
    if (name == null) {
      throw new IllegalArgumentException("name is required");
    }
  }
  
}
