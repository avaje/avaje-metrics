package io.avaje.metrics;

/**
 * Builder used to configure and register a counter.
 *
 * <p>Example:
 *
 * <pre>{@code
 * registry.counterBuilder("app.rows")
 *   .tags(Tags.of("pod:blue"))
 *   .unit("row")
 *   .build();
 * }</pre>
 */
public interface CounterBuilder {

  /**
   * Set the tags to use for the counter.
   */
  CounterBuilder tags(Tags tags);

  /**
   * Set the unit to use for the counter.
   */
  CounterBuilder unit(String unit);

  /**
   * Create and register the counter.
   */
  Counter build();
}
