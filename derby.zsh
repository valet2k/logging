#!/usr/bin/env zsh
set -o pipefail

cd $(dirname $0)
local archive_prefix="db-derby-10.12.1.1-lib"
local lib_url=http://www-us.apache.org/dist/db/derby/db-derby-10.12.1.1/db-derby-10.12.1.1-lib.zip
#ls "$archive_prefix".zip

test -f "$archive_prefix".zip || curl -O $lib_url
local derby_run_path="$archive_prefix/lib/derbyrun.jar"

test -f $derby_run_path || unzip "$archive_prefix".zip
local derby_run="$(readlink -f $derby_run_path)"

derby(){
if test "$1" = "--help" ; then
  echo Usage:
  echo -e "\t$0 \t\tto download(if needed) and start"
  echo -e "\t$0 stop \tto stop"
  exit
fi

if test "$1" = "stop"; then
  echo "trying to stop"
  java -jar $derby_run server ping && java -jar $derby_run server shutdown
else
  echo "starting if not already"
  java -jar $derby_run server ping || java -jar $derby_run server start &
fi
}

echo "derby handler installed"

