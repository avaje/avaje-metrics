package io.avaje.metrics;

import java.util.Collection;
import java.util.function.Function;

/**
 * A Metric that collects statistics on events.
 * <ul>
 * <li>Timer used for monitoring execution time</li>
 * <li>Counter is for counting discrete events like 'user logged in'</li>
 * <li>Meter is used when events have a value like bytes sent, lines read</li>
 * <li>Gauges measure the current value of a resource like 'used memory' or 'active threads'.</li>
 * </ul>
 */
public interface Metric {

  /**
   * Return the Id of the metric.
   */
  ID id();

  /**
   * Return the name of the metric.
   */
  String name();

  /**
   * Typically this is only called by the MetricManager and tells the metric to collect its underlying statistics for
   * reporting purposes and in addition resetting and internal counters it has.
   */
  void collect(Visitor collector);

  /**
   * Reset the statistics resetting any internal counters etc.
   * <p>
   * Typically, the MetricRegistry takes care of resetting the statistic/counters for the metrics when
   * it periodically collects and reports all the metrics, and you are not expected to use this method.
   */
  void reset();

  /**
   * Identifier of a Metric based on both the name and tags.
   */
  interface ID {

    /**
     * Create an Id given a name only.
     */
    static ID of(String name) {
      return new MId(name, Tags.EMPTY);
    }

    /**
     * Create an Id given name and tags.
     */
    static ID of(String name, Tags tags) {
      return new MId(name, tags);
    }

    /**
     * Return the metric name.
     */
    String name();

    /**
     * Return the tags.
     */
    Tags tags();

    /**
     * Return an Id appending the suffix to the name.
     */
    ID suffix(String suffix);

    /**
     * Return an Id with the given name..
     */
    ID withName(String otherName);

    /**
     * Return an Id with the given name..
     */
    ID withTags(Tags tags);

  }

  /**
   * Common for statistics of all metrics.
   */
  interface Statistics {

    /**
     * Return the metric id.
     */
    ID id();

    /**
     * Return the metric name.
     */
    String name();

    /**
     * Return the associated tags as an array.
     */
    String[] tags();

    /**
     * Visit the reporter for the given metric type.
     */
    void visit(Visitor reporter);

  }

  /**
   * Used for reporting to visit all the different types of metric statistics.
   */
  interface Visitor {

    /**
     * Return the naming convention to use when reporting metrics.
     */
    default Function<String, String> namingConvention() {
      return NamingMatch.INSTANCE;
    }

    /**
     * Visit Timer stats.
     */
    void visit(Timer.Stats timed);

    /**
     * Visit meter stats.
     */
    void visit(Meter.Stats meter);

    /**
     * Visit Counter stats.
     */
    void visit(Counter.Stats counter);

    /**
     * Visit GaugeDouble stats.
     */
    void visit(GaugeDouble.Stats gauge);

    /**
     * Visit GaugeLong stats.
     */
    void visit(GaugeLong.Stats gauge);

    /**
     * Visit all the metrics.
     */
    default void visitAll(Collection<Statistics> all) {
      for (Statistics stats : all) {
        stats.visit(this);
      }
    }
  }
}
