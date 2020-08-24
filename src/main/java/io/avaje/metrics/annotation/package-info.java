/**
 * Annotations such as {@link io.avaje.metrics.annotation.Timed}.
 * <p>
 * These annotations can be placed on classes and methods to control metrics collection.
 * <p>
 * Note that enhancement can be used to automatically add timing metrics collections on
 * classes annotated with Timed - but additionally will also by default enhance classes
 * annotated with <code>@Singleton</code>, JAX_RS annotations like <code>@Path</code> and
 * Spring sterotype annotations such as <code>@Component</code>, <code>@Service</code> and <code>@Repository</code> etc.
 */
package io.avaje.metrics.annotation;