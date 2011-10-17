package foodtruck.geolocation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Represents a Yahoo resource.
 * @author aviolette@gmail.com
 * @since 10/16/11
 */
@BindingAnnotation @Target( {FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
public @interface YahooEndPoint {
}
