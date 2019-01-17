#!/bin/sh
cd /Users/andrewviolette/dev/foodtrucklocator/lambda/royal-palms

node extractLinks.js > /tmp/royalpalms.json
gsutil cp /tmp/royalpalms.json gs://cftf_schedule

