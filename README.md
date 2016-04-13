# logging
[![Build Status](https://travis-ci.org/valet2k/logging.svg?branch=master)](https://travis-ci.org/valet2k/logging)

* load.zsh - entry point for adding to shell - source in zshrc

The assistant is a maven project that connects to a built-in derby server and
specifies table config and provides nails to interact with the database and a
Spark data frame to pull data from. It's maven project folder is `assistant`.

nailgun is a commandline rpc interface to a long-running jvm. The assistant uses
it to call in. The client should be handled by running make on a git-subtree
inclusion of the nailgun repo and will be exposed as `$valet2k_ng` after
`load.zsh` is sourced.
[See here](http://blogs.atlassian.com/2013/05/alternatives-to-git-submodule-git-subtree/)
for more info.

Nails:
* logshow [n] - show n lines of history
* lognew [command line] - add log entry with command line being args
* logrm n - remove row n from log

Travis:
* travis-ci.org builds the project automagically, and deploys artifacts to s3,
  if it builds successfully
* builds can be retrieved from `https://s3.amazonaws.com/valet2k/builds/assistant-latest.jar`

Developlment:
* Can use any ide with maven integration
* Need nailgun's snapshot version built and installed locally
  (`mvn clean install` in the repo (need `mvn` from installed maven on path))

