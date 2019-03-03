#!/bin/sh
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd $DIR

node extractLinks.js > /tmp/royalpalms.json
gsutil cp /tmp/royalpalms.json gs://cftf_schedule

