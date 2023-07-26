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
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed {

  /**
   * Set the prefix for metric names. Typically used at class level to define a common prefix.
   * <p>
   * By default <em>app</em> is the prefix used for timed metrics with <em>web.api</em> is the
   * default prefix for web controllers (Rest endpoint controllers).
   *
   * <pre>{@code
   *
   * @Timed(perfix="web.api")
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
}
