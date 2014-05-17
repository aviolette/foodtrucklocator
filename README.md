# Introduction

The Food Truck Locator is an App Engine app that shows a map with the location of food trucks in an area.  Food trucks that are displayed on this map are done so by aggregating data from google calendar feeds and information from twitter.  In addition to the information displayed on the map, a set of web services is provided to build out third party applications.  Finally, the food truck locator publishes information about food trucks to configurable tweet bots that are associated with a location in the area.

# Initial Configuration

Download the latest Appengine SDK for Java, https://developers.google.com/appengine/downloads

Edit your .bashrc.  Define APPENGINE_HOME.  This should point to the directory where you unpacked the appengine SDK. Define
APPENGINE_VERSION as well.  This should match the version of appengine you downloaded (e.g. 1.9.4).

> source ~/.bashrc

Run this once.  This will install the maven dependencies that aren't in a public repository:

> ./setup.sh

# Building

> mvn package

# Running (in Development)

To run your app on port 8080, run this command in the root directory of your code:

> $APPENGINE_HOME/bin/dev_appserver.sh target

# Configuring the web application

1) Go to http://localhost:/admin/configuration.  When prompted, sign in as administrator.

At minimum you will need to define the Map Center, Front Door App Key, and you will need to enable Google Geolocation and Yahoo Placefinder (no need to specify all the auth stuff for yahoo).

Here is a detailed description of all the parameters:

* Map Center - This is the address with which the map will center by default (for example Clark and Monroe, Chicago, IL)
* Enable food truck request support - Add a button on the truck page that will allow people to request food trucks at their events
* Enable Schedule Caching - If enabled, the schedule will be cached in memcached for 5 minutes.  So changes that you make will not be reflected for up to 5 minutes
* Enable ability to take trucks off the road automatically - If enabled certain key words specified in tweets will cause them their schedules to be removed from the site for the day (for example, 'off the road' is one of them).
* Front Door App Key - This is the application key for the main app.
* Email of sender - All system notifications will be sent from this email address
* List of notification receivers - The list of addresses that will receive notifications
* Google Calendar URL - This is the calendar URL which will be searched for food truck events
* Enable Google Geolocation - If enabled, Google Geolocation will be used for location lookups
* Enable Yahoo Placefinder - If enabled, Yahoo Placefinder will be used for location lookups
* Primary Twitter List Id - The

2) Add all the trucks

Go to http://localhost:8080/admin/trucks

# Deploying to App Engine

Put Some Stuff Here

* Setting up cron jobs
* Setting up security
* Deployment



## Twitter

The foodtrucklocator uses twitter to gather information about the current state of trucks.  In order to use this feature, you have to register an application here: https://dev.twitter.com/apps.  You will get authentication credentials that you can use to connect to the twitter API.  

You will need to create a twitter4j.properties file as defined here: http://twitter4j.org/en/configuration.html and put it in src/main/java/resources.

You will need to create a twitter list that contains all the food trucks you wish to track.  All twitter lists have a unique ID.  Get this ID (you can use the twitter developer tools to get it).  You will need this below.

## Yahoo

Because throttled Google geolocation services are not reliable on AppEngine because of a shared IP pool, it is recommended that you get a Yahoo application key that will allow you to do geolocation lookups on Yahoo Placefinder in addition to Google.  

## Google Calendar

The foodtrucklocator uses a PUBLIC google calendar to keep the food truck schedules in.  You will need to create a public google calendar (If you are using a Google apps account make sure that your calendar settings allow calendar events to be exposed to the public).  Go into the calendar settings, click the orange XML button, a URL will popup, grab that link.  The last part of the url will end in 'basic'.  Copy down that URL, and change the 'basic' to 'full'.  This will be used below.

## Google AppEngine (for deployment)

Create an AppEngine application.  This will be used in the deployment step below.  You will also need to setup cron jobs to sync the google calendar data into the local schedule cache.



