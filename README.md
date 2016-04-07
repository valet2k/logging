# logging

* derby.zsh - bootstraps/controls a derby network server
* load.zsh - entry point for adding to shell
* addhook.zsh - should add precmd hook for logging

The assistant is a maven project that connects to the derby server(might move it
into same process) and specifies table config and provides nails to interact
with the database and a Spark data frame to pull data from.

nailgun is a commandline rpc interface to a long-running jvm. The assistant uses
it to call in. The client must be built and if the repo is cloned to this folder
and then built, it will be exposed as `$valet2k_ng` after `load.zsh` is sourced.

Nails:
* logshow [n] - show n lines of history
* lognew [command line] - add log entry with command line being args
* logrm n - remove row n from log

TODO:
* need shell aliases/functions to shorten nail calls?
