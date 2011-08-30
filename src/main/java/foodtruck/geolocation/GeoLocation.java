// Copyright 2010 BrightTag, Inc. All rights reserved.
package foodtruck.geolocation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that represents a webresource used for geolocation
 * @author aviolette@gmail.com
 * @since 8/30/11
 */
@BindingAnnotation @Target( {FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
public @interface GeoLocation {
}
