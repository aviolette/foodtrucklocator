# Introduction

The Food Truck Locator is an App Engine app that shows a map with the location of food trucks in an area.  Food trucks that are displayed on this map are done so by aggregating data from google calendar feeds and information from twitter.  In addition to the information displayed on the map, a set of web services is provided to build out third party applications.  Finally, the food truck locator publishes information about food trucks to configurable tweet bots that are associated with a location in the area.

# Initial Configuration

Download the latest Appengine SDK for Java, https://developers.google.com/appengine/downloads

Edit your .bashrc.  Define APPENGINE_HOME.  This should point to the directory where you unpacked the appengine SDK. Define
APPENGINE_VERSION as well.  This should match the version of appengine you downloaded (e.g. 1.9.4).

> source ~/.bashrc

Run this once.  This will install the maven dependencies that aren't in a public repository:

> ./setup.sh

## Twitter Configuration

This cofiguration is needed to read and post to twitter

```
> mkdir -p main/src/main/resources
> cd main/src/main/resources
> emacs twitter4j.properties
```

Add these properties to twitter4j.properties

```
debug=true
oauth.consumerKey=....
oauth.consumerSecret=....
oauth.accessToken=...
oauth.accessTokenSecret=...
http.retryCount=1
http.enableSSL=true
```

# Building

> mvn package

# Running (in Development)

To run your app on port 8080, run this command in the root directory of your code:

> $APPENGINE_HOME/bin/dev_appserver.sh target/foodtrucklocator-1.0-SNAPSHOT

