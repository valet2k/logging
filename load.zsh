#!/usr/bin/env zsh


if [[ ! -p ~/.v2k_suggestion_pipe ]]; then
    mkfifo ~/.v2k_suggestion_pipe
fi

export valet2k_repo=$(dirname "$(readlink -e "$0")")
make -C $valet2k_repo/nailgun-git ng
export valet2k_ng=$valet2k_repo/nailgun-git/ng
echo "Entering Valet 2000"

socket_name=v2kcom
# shorthand
tm="tmux -L $socket_name"

${=tm} new-session -d -s user #"zsh zsh_hook.sh)\'; zsh -i"
#$tm new-window -d -t user -n user
${=tm} split-window -dl 11 -t user:0.0 "while true; do cat ~/.v2k_suggestion_pipe; done"
${=tm} send-keys -t user:0.0 "source ${valet2k_repo}/v2k-init/suggestions.zsh;
source ${valet2k_repo}/v2k-init/create-suggestions.zsh;
source ${valet2k_repo}/v2k-init/logging.zsh" C-m
${=tm} attach -t user
