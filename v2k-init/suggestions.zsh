#!/usr/bin/env zsh

#Needs to be integrated with the suggestion system
function v2k-get-suggestion {
  echo $(${=valet2k_ng} logml suggest $1)
}

function v2k-suggestion {
  
  BUFFER=$(v2k-get-suggestion $1)
  zle end-of-line
}

function v2k-suggestion-1 {
  v2k-suggestion 1
}

function v2k-suggestion-2 {
  v2k-suggestion 2
}

function v2k-suggestion-3 {
  v2k-suggestion 3
}

function v2k-suggestion-4 {
  v2k-suggestion 4
}

function v2k-suggestion-5 {
  v2k-suggestion 5
}

function v2k-suggestion-6 {
  v2k-suggestion 6
}

function v2k-suggestion-7 {
  v2k-suggestion 7
}

function v2k-suggestion-8 {
  v2k-suggestion 8
}

function v2k-suggestion-9 {
  v2k-suggestion 9
}

function v2k-suggestion-0 {
  v2k-suggestion 0
}

zle -N v2k-suggestion-1
zle -N v2k-suggestion-2
zle -N v2k-suggestion-3
zle -N v2k-suggestion-4
zle -N v2k-suggestion-5
zle -N v2k-suggestion-6
zle -N v2k-suggestion-7
zle -N v2k-suggestion-8
zle -N v2k-suggestion-9
zle -N v2k-suggestion-0

bindkey -r '^F'
bindkey '^F1' v2k-suggestion-1
bindkey '^F2' v2k-suggestion-2
bindkey '^F3' v2k-suggestion-3
bindkey '^F4' v2k-suggestion-4
bindkey '^F5' v2k-suggestion-5
bindkey '^F6' v2k-suggestion-6
bindkey '^F7' v2k-suggestion-7
bindkey '^F8' v2k-suggestion-8
bindkey '^F9' v2k-suggestion-9
bindkey '^F0' v2k-suggestion-0