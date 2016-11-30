package foodtruck.monitoring;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * @author aviolette@gmail.com
 * @since 7/4/12
 */
@BindingAnnotation @Target({METHOD}) @Retention(RUNTIME)
public @interface Monitored {
}
