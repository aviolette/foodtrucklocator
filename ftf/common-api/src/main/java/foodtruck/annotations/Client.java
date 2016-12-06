package foodtruck.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author aviolette
 * @since 12/5/16
 */
@BindingAnnotation
@Target({METHOD}) @Retention(RUNTIME)
public @interface Client {
}
