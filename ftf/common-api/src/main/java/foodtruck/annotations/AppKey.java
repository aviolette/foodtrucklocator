package foodtruck.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author aviolette
 * @since 12/14/16
 */

@Target({PARAMETER}) @Retention(RUNTIME)
public @interface AppKey {
}
