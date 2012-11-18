# Introduction

<< put something here>>

# Initial Configuration

Download the Appengine SDK for Java, version 1.7.3: http://googleappengine.googlecode.com/files/appengine-java-sdk-1.7.3.zip

Define APPENGINE_HOME in your bashrc.  This should point to the directory where you unpacked the appengine SDK

Run this once.  This will install the maven dependencies that aren't in a public repository:

> ./setup.sh

## Twitter

The foodtrucklocator uses twitter to gather information about the current state of trucks.  In order to use this feature, you have to register an application here: https://dev.twitter.com/apps.  You will get authentication credentials that you can use to connect to the twitter API.  

You will need to create a twitter4j.properties file as defined here: http://twitter4j.org/en/configuration.html and put it in src/main/java/resources.

## Yahoo

Because throttled Google geolocation services are not reliable on AppEngine because of a shared IP pool, it is recommended that you get a Yahoo application key that will allow you to do geolocation lookups on Yahoo Placefinder in addition to Google.  

# Building

> mvn package

# Running (in Development)

$APPENGINE_HOME/bin/dev_appserver.sh target

# Configuring the web application

1. Add all the trucks
2. Configure the address location <<add more information about this>>
3. Setup the geolocation
4. Set the map center

# Deploying to App Engine

<< put some stuff here >>

