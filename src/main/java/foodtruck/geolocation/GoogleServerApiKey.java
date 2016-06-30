package foodtruck.geolocation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author aviolette
 * @since 5/19/16
 */
@BindingAnnotation
@Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
@interface GoogleServerApiKey {
}
