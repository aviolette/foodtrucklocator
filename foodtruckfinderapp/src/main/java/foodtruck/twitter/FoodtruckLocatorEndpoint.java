// Copyright 2010 BrightTag, Inc. All rights reserved.
package foodtruck.twitter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author aviolette@gmail.com
 * @since 10/26/11
 */
@BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
public @interface FoodtruckLocatorEndpoint {
}
