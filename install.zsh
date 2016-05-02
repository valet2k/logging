#!/usr/bin/env zsh

if [[ $ZSH_EVAL_CONTEXT =~ :file$ ]]; then
  echo 'You must execute install.zsh, not source it.'
  return 1
fi

if [[ -z ${valet2k_repo+x} ]]; then
  valet2k_repo=$(dirname "$(readlink -e "$0")")
  echo "valet2k_repo set to '${valet2k_repo}'"
else
  echo "valet2k_repo previously set to '${valet2k_repo}'"
fi

if [[ $(git --git-dir ${valet2k_repo}/.git rev-parse --abbrev-ref HEAD) = 'master' ]]; then
  echo 'Updating by git...'
  git --git-dir ${valet2k_repo}/.git pull
else
  echo 'Either git is not installed or the "logging" repository is not on the master branch.'
  exit 2
fi


cd ${valet2k_repo}/assistant

# Download the jar if necessary
s3='https://s3.amazonaws.com/valet2k/builds/'
jarname='assistant-latest.jar'

if curl -sS "$s3$jarname.sha512" | sha512sum -c &> /dev/null; then
  echo 'Current assistant-latest.jar is up to date'
else
  echo 'Downloading newest assistant-latest.jar'
  curl -O "$s3$jarname"
fi

# Start nailgun
(java -jar assistant-latest.jar &) &> /dev/null
