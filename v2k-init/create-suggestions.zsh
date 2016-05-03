#!/usr/bin/env zsh

#Needs to be integrated with the suggestion system
function v2k-create-suggestions {
#  ${=valet2k_ng} logml predict "${BUFFER}"
  echo "$(clear)$(date +"%T") w/ buffer ${BUFFER}:\n$($valet2k_ng logml suggest -p ''$BUFFER)" > "$HOME/.v2k_suggestion_pipe"
}

#Widget not working, may make this its own function and bind tab to it
#function expand-or-complete {
#  v2k-create-suggestions &!
#  zle .expand-or-complete
#}

#zle -N expand-or-complete

function self-insert {
  zle .self-insert
  v2k-create-suggestions &!
}

zle -N self-insert

function backward-delete-char {
  zle .backward-delete-char
  v2k-create-suggestions &!
}

zle -N backward-delete-char

function delete-char {
  zle .delete-char
  v2k-create-suggestions &!
}

zle -N delete-char

function magic-space {
  zle .magic-space
  v2k-create-suggestions &!
}

zle -N magic-space
