#!/bin/sh
set -v
tmp=$(mktemp)
echo $tmp
cd $(dirname $0)
echo "$@" > ijtest.sql.50
for each in $(ls ijtest.sql.* | sort -g -k 3 -t .); do
  echo -e -- -- $each section >> $tmp
  cat $each >> $tmp
done;
derby_path=db-derby-10.12.1.1-lib/lib/derbyrun.jar
ij="java -jar $derby_path ij "
#cygpath && $ij "$(cygpath -w $tmp)" || $ij $tmp

worl=$(which cygcheck) 

#$ij "$(cygpath -w $tmp)"

if [ $worl = "/usr/bin/cygcheck" ]; then
	$ij "$(cygpath -w $tmp)"
else
	$ij $tmp
fi

#java -jar $iderby_path ij "$(cygpath -w $tmp)"
# /usr/bin/cygcheck
