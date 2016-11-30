package foodtruck.appengine.dao.appengine;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author aviolette
 * @since 2/28/14
 */
@BindingAnnotation
@Target({PARAMETER, METHOD}) @Retention(RUNTIME)
public @interface FifteenMinuteRollup {
}
