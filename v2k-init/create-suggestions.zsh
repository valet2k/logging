#!/usr/bin/env zsh

#Needs to be integrated with the suggestion system
function v2k-create-suggestions {
  echo "$(clear)Suggestion last updated: $(date +"%T")" > ~/.v2k_suggestion_pipe
}

function expand-or-complete {
  v2k-create-suggestions &!
  zle .self-insert
}

zle -N expand-or-complete

function self-insert {
  v2k-create-suggestions &!
  zle .self-insert
}

zle -N self-insert
