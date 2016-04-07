#!/usr/bin/env zsh
local dir=$(dirname $0)
local olddir=$PWD

cd $dir # logging directory
source derby.zsh
derby&

export valet2k_ng=$PWD/nailgun/ng

cd assistant
# this test doesn't work - not sure why yet
pgrep -f com.github.valet2k || mvn exec:java&

cd $dir
source addhook.zsh

cd $olddir
