#!/usr/bin/env zsh

#Needs to be integrated with the suggestion system
function v2k-create-suggestions {
  echo "$(clear)Suggestion last updated: $(date +"%T")" > ~/.v2k_suggestion_pipe
}

#Widget not working, may make this its own function and bind tab to it
function expand-or-complete {
  v2k-create-suggestions &!
  zle .expand-or-complete
}

zle -N expand-or-complete

function self-insert {
  v2k-create-suggestions &!
  zle .self-insert
}

zle -N self-insert

function backward-delete-char {
  v2k-create-suggestions &!
  zle .backward-delete-char
}

zle -N backward-delete-char

function delete-char {
  v2k-create-suggestions &!
  zle .delete-char
}

zle -N delete-char

function magic-space {
  v2k-create-suggestions &!
  zle .magic-space
}

zle -N magic-space
