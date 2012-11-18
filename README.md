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

You will need to create a twitter list that contains all the food trucks you wish to track.  All twitter lists have a unique ID.  Get this ID (you can use the twitter developer tools to get it).  You will need this below.

## Yahoo

Because throttled Google geolocation services are not reliable on AppEngine because of a shared IP pool, it is recommended that you get a Yahoo application key that will allow you to do geolocation lookups on Yahoo Placefinder in addition to Google.  

## Google Calendar

The foodtrucklocator uses a PUBLIC google calendar to keep the food truck schedules in.  You will need to create a public google calendar (If you are using a Google apps account make sure that make sure that your calendar settings allow calendar events to be exposed to the public).  Go into the calendar settings, click the orange XML button, a URL will popup, grab that link.  The last part of the url will end in 'basic'.  Copy down that URL, and change the 'basic' to 'full'.  This will be used below.

## Google AppEngine (for deployment)

Create an AppEngine application.  This will be used in the deployment step below.  You will also need to setup cron jobs to sync the google calendar data into the local schedule cache.

# Building

> mvn package

# Running (in Development)

To run your app on port 8080, run this command in the root directory of your code:

> $APPENGINE_HOME/bin/dev_appserver.sh target

# Configuring the web application

1. Add all the trucks
2. Configure the address location <<add more information about this>>
3. Setup the geolocation
4. Set the map center

# Deploying to App Engine

<< put some stuff here >>

