#!/usr/bin/env zsh

if [[ ! -p ~/.v2k_suggestion_pipe ]]; then
  mkfifo ~/.v2k_suggestion_pipe
fi


#TODO: startup
#../nailgun/ng ng-alias | grep lognew > /dev/null || nohup mvn exec:java&

export valet2k_repo="$(dirname "$0:a")"
make -C $valet2k_repo/nailgun-git ng > /dev/null
export valet2k_ng=$valet2k_repo/nailgun-git/ng
export valet2k_init=$valet2k_repo/v2k-init
source ${valet2k_init}/logging.zsh


# Cleanly exit tmux
function quit()
{       echo "Shutting down Valet..."
	pkill tmux
	pkill zsh
	#$valet2k_ng ng-stop
}

export valet2k_session=$($valet2k_ng uuid)
echo $valet2k_session | grep 'Connection refused' && export valet2k_session=$(cd $valet2k_repo/; java -jar assistant/assistant-latest.jar com.github.valet2k.UUID)

if [[ "$valet2k_in_tmux" = true ]]; then
  source ${valet2k_init}/suggestions.zsh
  source ${valet2k_init}/create-suggestions.zsh
  source ${valet2k_init}/directory-tree.zsh
else
  ${valet2k_repo}/install.zsh
  export valet2k_tmux="tmux -L v2kcom"

  if [[ "$valet2k_auto_tmux" = true ]]; then #set valet2k_auto_tmux in .zshrc
    ${valet2k_init}/tmux-init.zsh
  else
    echo 'To start valet2k_tmux, execute "${valet2k_init}/tmux-init.zsh".'
    echo 'For now only logging is enabled.'
  fi
fi
