#!/usr/bin/env zsh


if [[ ! -p ~/.v2k_suggestion_pipe ]]; then
  mkfifo ~/.v2k_suggestion_pipe
fi

export valet2k_repo=$(dirname "$(readlink -e "$0")")
make -C $valet2k_repo/nailgun-git ng
export valet2k_ng=$valet2k_repo/nailgun-git/ng


#TODO: startup
#../nailgun/ng ng-alias | grep lognew > /dev/null || nohup mvn exec:java&

socket_name=v2kcom
# shorthand
export tm="tmux -L $socket_name"

echo "Entering Valet 2000"
if ${=tm} info &> /dev/null; then #running
  echo "jk, v2k already running"
else #not running
  ${valet2k_repo}/v2k-init/tmux-init.zsh
fi
