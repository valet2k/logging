#!/usr/bin/env zsh
echo "new-session"
${=tm} new-session -d -s user #"zsh zsh_hook.sh)\'; zsh -i"
#$tm new-window -d -t user -n user
echo "split-window"
${=tm} split-window -dl 11 -t user:0.0 "while true; do cat ~/.v2k_suggestion_pipe; done"
echo "send-keys"
${=tm} send-keys -t user:0.0 "source ${valet2k_repo}/v2k-init/suggestions.zsh;
source ${valet2k_repo}/v2k-init/logging.zsh;
source ${valet2k_repo}/v2k-init/create-suggestions.zsh" C-m
echo "attach"
${=tm} attach -t user
