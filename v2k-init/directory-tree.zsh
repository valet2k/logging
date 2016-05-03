#!/usr/bin/env zsh

[[ -z $chpwd_functions ]] && chpwd_functions=()
directory_tree() {
    clear > ~/.v2k_print_pipe
	echo "$(pwd):\n$(tree -L 3 --filelimit 10)" > ~/.v2k_print_pipe
	#echo "$(pwd):\n$(tree --du -shaC | grep -Ev '(  *[^ ]* ){3}\[')" > ~/.v2k_print_pipe
    #ls --color=always > ~/.v2k_print_pipe
}
chpwd_functions+=directory_tree

function accept-line() {
	#echo "${BUFFER}" > ~/.v2k_print_pipe
	zle .accept-line
}

zle -N accept-line
