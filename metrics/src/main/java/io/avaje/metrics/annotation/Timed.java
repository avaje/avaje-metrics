package io.avaje.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put onto a Class or methods that we want timed execution statistics collected.
 * <p>
 * When put on a Class the default is that all public methods on that Class are timed.
 * <p>
 * When put on a method we want to override some of metric naming or only collect timed
 * execution on very few methods on the class.
 * <p>
 * Tags specified at class level are inherited by timed methods. Tags specified at method level
 * append to the class-level tags.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed {

  /**
   * Controls whether timed enhancement should also create spans.
   */
  enum SpanMode {
    /**
     * Use the surrounding default.
     */
    DEFAULT,
    /**
     * Create child spans under an existing recording span.
     */
    CHILD,
    /**
     * Create a root span when no recording span exists.
     */
    ROOT,
    /**
     * Do not create spans for timed methods.
     */
    OFF
  }

  /**
   * Set the prefix for metric names. Typically used at class level to define a common prefix.
   * <p>
   * By default <em>app</em> is the prefix used for timed metrics with <em>web.api</em> is the
   * default prefix for web controllers (Rest endpoint controllers).
   *
   * <pre>{@code
   *
   * @Timed(prefix="web.api")
   * public class AdminResource
   *   ...
   *
   * }</pre>
   */
  String prefix() default "";

  /**
   * Set the metric name.
   * <p>
   * When on a class this override the default metric name which is otherwise
   * the short name of the class.
   * <p>
   * On methods we use this when the method name is not appropriate or when
   * there is method overloading and the otherwise generated unique name is unclear.
   */
  String name() default "";

  /**
   * Set tags for the timer using {@code key:value} values as used by {@code Tags.of(...)}.
   * <p>
   * Class-level tags apply to each timed method and method-level tags append to them.
   *
   * <pre>{@code
   *
   * @Timed(tags = {"component:billing", "marker:blue"})
   * public class BillingService {
   *
   *   @Timed(tags = "operation:sync")
   *   public void syncInvoices() {
   *     ...
   *   }
   * }
   *
   * }</pre>
   */
  String[] tags() default {};

  /**
   * Define buckets as a list of millisecond times.
   * <p>
   * For example with values of 100, 200, 300 there a with 4 bucket ranges of:
   * <pre>
   *      0 to 100 milliseconds
   *    100 to 200 milliseconds
   *    200 to 300 milliseconds
   *    300+       milliseconds
   * </pre>
   * <p>
   * Defining buckets means a bucket timer will be used instead of a standard timer.
   */
  int[] buckets() default {};

  /**
   * Specify whether timed enhancement should also create spans.
   * <p>
   * {@link SpanMode#DEFAULT} inherits the class-level or agent-level setting.
   */
  SpanMode span() default SpanMode.DEFAULT;
}
