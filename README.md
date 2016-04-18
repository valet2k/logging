# logging
[![Build Status](https://travis-ci.org/valet2k/logging.svg?branch=master)](https://travis-ci.org/valet2k/logging)

* load.zsh - entry point for adding to shell - source in zshrc
  TODO: actually start assistant from jar

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
* latest build can be retrieved from [s3](https://s3.amazonaws.com/valet2k/builds/assistant-latest.jar)
* checksum also available at [s3](https://s3.amazonaws.com/valet2k/builds/assistant-latest.jar.sha512)

User installation:
* clone this repo
* download latest build jar into assistant project folder under repo root
* launch in assistant folder (not more than one at a time)
* source load.zsh to add rpc and hooks

Developlment:
* Can use any ide with maven integration, or use maven directly
* Need nailgun's snapshot version built and installed locally

With maven:
* can get maven from [maven site](https://maven.apache.org/download.cgi)
* put maven on path (exercise for reader)
* have `JAVA_HOME` set to a jdk home (exercise for reader)
* `mvn clean install` in the nailgun-git folder
* can launch assistant with `mvn compile exec:java` in assistant folder

With IDE:
* launch install maven target for `nailgun` at least once
* use run from ide or exec:java target for assistant from ide

package task also needs jar-timestamp-normalize-maven-plugin from
[a fork](github.com/automaticgiant/jar-timestamp-normalize-maven-plugin) until pr is
merged upstream

