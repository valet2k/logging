#!/usr/bin/env zsh

if [[ $ZSH_EVAL_CONTEXT =~ :file$ ]]; then
  echo 'You must execute tmux-init, not source it.'
  return 1
fi

if ${=valet2k_tmux} info &> /dev/null; then #running
  echo "v2k already running"
else #not running
  export valet2k_in_tmux=true
  echo "Entering Valet 2000..."
  ${=valet2k_tmux} new-session -d -s user #"zsh zsh_hook.sh)\'; zsh -i"
  #$tm new-window -d -t user -n user
  ${=valet2k_tmux} split-window -dl 11 -t user:0.0 "while true; do cat ~/.v2k_suggestion_pipe; done"
  ${=valet2k_tmux} attach -t user
fi
