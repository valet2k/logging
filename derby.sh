#!/bin/sh
set -eo pipefail

if test "$1" = "--help" ; then
  echo Usage:
  echo -e "\t$0 \t\tto download(if needed) and start"
  echo -e "\t$0 stop \tto stop"
  exit
fi

cd $(dirname $0)
echo "cd $(pwd)"
archive_prefix="db-derby-10.12.1.1-lib"
lib_url=http://www-us.apache.org/dist/db/derby/db-derby-10.12.1.1/db-derby-10.12.1.1-lib.zip
ls "$archive_prefix".zip
test -f "$archive_prefix".zip || curl -O $lib_url
derbyrun_path="$archive_prefix/lib/derbyrun.jar"
test -f $derbyrun_path || unzip "$archive_prefix".zip
derby_run="java -jar $derbyrun_path"
if test "$1" = "stop" ; then
  $derby_run server ping && $derby_run server shutdown
else
  $derby_run server ping || $derby_run server start &
fi
