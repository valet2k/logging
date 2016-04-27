#!/usr/bin/env zsh

autoload add-zsh-hook
add-zsh-hook -d precmd log
add-zsh-hook -d precmd v2k-log
function v2k-log {
  #get detailed env and send last history line as args
  typeset | $valet2k_ng lognew $(fc -ln -1)
}
add-zsh-hook precmd v2k-log
