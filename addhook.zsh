#!/usr/bin/env zsh

autoload add-zsh-hook
add-zsh-hook -d precmd log
function log(){
  typeset | $valet2k_ng lognew $(fc -ln -1)
}
add-zsh-hook precmd log
