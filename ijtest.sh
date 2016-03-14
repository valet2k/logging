#!/bin/sh
set -v
tmp=$(mktemp)
cd $(dirname $0)
echo "$@" > ijtest.sql.50
for each in $(ls ijtest.sql.* | sort -g -k 3 -t .); do
  echo -e -- -- $each section >> $tmp
  cat $each >> $tmp
done;
java -jar db-derby-10.12.1.1-lib/lib/derbyrun.jar ij $tmp
