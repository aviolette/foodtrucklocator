#!/bin/bash

mkdir -p backup

backup_url='http://www.chicagofoodtruckfinder.com/remote_api'
kinds=( "Store" "Configuration" "application" "twitter_notification_account" "address_rule_script" "application")
for kind in "${kinds[@]}"
do
    rm backup/${kind}.out
    appcfg.py --oauth2 download_data --url=${backup_url} --filename=backup/${kind}.out --email=${EMAIL} --kind=${kind}
done
rm bulkloader*

