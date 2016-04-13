#!/usr/bin/env zsh

valet2k_repo=$(dirname $(readlink -e $0))
lastpwd=$PWD

make -C $valet2k_repo/nailgun-git ng
export valet2k_ng=$valet2k_repo/nailgun-git/ng

autoload add-zsh-hook
add-zsh-hook -d precmd log
function log(){
  #get detailed env and send last history line as args
  #TODO: change
  typeset | $valet2k_ng lognew $(fc -ln -1)
}
add-zsh-hook precmd log

#TODO: startup
#cd assistant
#../nailgun/ng ng-alias | grep lognew > /dev/null || nohup mvn exec:java&

