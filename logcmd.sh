#!/bin/sh
./ijtest.sh "insert into t values ($(date +%s%N),'$*','$(pwd)');"
