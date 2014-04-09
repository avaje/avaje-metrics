package org.avaje.metric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker that timed execution statistics should NOT be collected on this class.
 * <p>
 * Note that timed execution can be automatically added to JAX-RS endpoints and
 * Spring beans and this annotation can be used to exclude specific beans as
 * desired.
 * </p>
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotTimed {

}
