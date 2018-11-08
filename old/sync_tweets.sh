#!/bin/sh
while [ true ]; do
    curl http://localhost:8080/cron/tweets
    sleep 1000;
done