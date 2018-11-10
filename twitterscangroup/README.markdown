# Introduction

This is a java service that fetches tweets from a list and puts them in a mongo database.  The last tweet retrieved is saved in a configuration as the starting point for the next query.

# Setup

> mkdir -p src/main/resources

Create a twitter4j.properties file as detailed here: http://twitter4j.org/en/configuration.html.  Put that file in src/main/resources

# Building

> mvn clean install assembly:assembly

# Running

> java -cp target/twittergroupscan-1.0-SNAPSHOT-jar-with-dependencies.jar tgc.poll.Main mongoUrl listId

Where mongo URL is something like this: 127.0.0.1/twitter

and listId is the numeric list identifier of a twitter list
