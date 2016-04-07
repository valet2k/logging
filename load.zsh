#!/usr/bin/env zsh
local dir=$(dirname $0)
local olddir=$PWD

cd $dir # logging directory
source derby.zsh
derby bg

export valet2k_ng=$PWD/nailgun/ng

cd assistant
../nailgun/ng ng-alias | grep lognew > /dev/null || nohup mvn exec:java&

cd $dir
source addhook.zsh

cd $olddir
