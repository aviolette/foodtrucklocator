#!/bin/sh

APPENGINE_VERSION=1.8.0

mvn install:install-file -Dfile=vendor/google/guice-2.0.jar \
    -DgroupId=com.google.code.guice \
    -DartifactId=guice \
    -Dversion=2.0.1 \
    -Dpackaging=jar 

mvn install:install-file -Dfile=vendor/google/guice-servlet-2.0.jar  \
    -DgroupId=com.google.code.guice \
    -DartifactId=guice-servlet \
    -Dversion=2.0.1 \
    -Dpackaging=jar 

mvn install:install-file -Dfile=vendor/google/gdata-client-1.0.jar \
    -DgroupId=com.google.gdata \
    -DartifactId=gdata-client \
    -Dversion=1.0 \
    -Dpackaging=jar 

mvn install:install-file -Dfile=vendor/google/gdata-core-1.0.jar \
    -DgroupId=com.google.gdata \
    -DartifactId=gdata-core \
    -Dversion=1.0 \
    -Dpackaging=jar 

mvn install:install-file -Dfile=vendor/google/gdata-calendar-2.0.jar \
    -DgroupId=com.google.gdata \
    -DartifactId=gdata-calendar \
    -Dversion=2.0 \
    -Dpackaging=jar 

mvn install:install-file -Dfile=$APPENGINE_HOME/lib/shared/appengine-local-runtime-shared.jar \
    -DgroupId=com.google \
    -DartifactId=appengine-local-runtime-shared \
    -Dversion=$APPENGINE_VERSION \
    -Dpackaging=jar 

mvn install:install-file -Dfile=$APPENGINE_HOME/lib/user/appengine-api-1.0-sdk-$APPENGINE_VERSION.jar \
    -DgroupId=com.google \
    -DartifactId=appengine-api-1.0-sdk \
    -Dpackaging=jar \
    -Dversion=$APPENGINE_VERSION \
    