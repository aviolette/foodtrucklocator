#!/bin/sh

node extractLinks.js > /tmp/calendar.json
gsutil cp /tmp/calendar.json gs://chicagopizzaboss
