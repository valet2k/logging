# logging
[![Build Status](https://travis-ci.org/valet2k/logging.svg?branch=master)](https://travis-ci.org/valet2k/logging)

##Entry Point
* load.zsh - entry point for adding to shell - source in zshrc
  TODO: actually start assistant from jar

##assistant

The assistant is a maven project that connects to a built-in derby server and
specifies table config and provides nails to interact with the database and a
Spark data frame to pull data from. It's maven project folder is `assistant`.

##RPC
nailgun is a commandline rpc interface to a long-running jvm. The assistant uses
it to call in. The client should be handled by running make on a git-subtree
inclusion of the nailgun repo and will be exposed as `$valet2k_ng` after
`load.zsh` is sourced.

[(git-subtree)](http://blogs.atlassian.com/2013/05/alternatives-to-git-submodule-git-subtree/)
for more info.

###Nails:
* logshow [n] - show n lines of history
* lognew [command line] - add log entry with command line being args
* logrm n - remove row n from log

##User installation:
* clone this repo
* download latest build jar into assistant project folder under repo root
* launch in assistant folder (not more than one at a time)
* source load.zsh to add rpc and hooks

##Developlment:
* Can use any ide with maven integration, or use maven directly
* Need nailgun's snapshot version built and installed locally

###Continuous Integration:
* travis-ci.org builds the project on push to any branch, and deploys artifacts
  (from master builds) to s3, if it builds successfully
* latest build can be retrieved from
  [https://s3.amazonaws.com/valet2k/builds/assistant-latest.jar](https://s3.amazonaws.com/valet2k/builds/assistant-latest.jar)
* checksum also available at
  [https://s3.amazonaws.com/valet2k/builds/assistant-latest.jar.sha512](https://s3.amazonaws.com/valet2k/builds/assistant-latest.jar.sha512)
* "e2e" test is run during build, [test.zsh](./test.zsh)
* travis configuration is [.travis.yml](./.travis.yml)
* runs [before_deploy.sh](./before_deploy.sh) if build is successful from master
  to prepare artifacts for publishing to s3 (example of checksum update check)

###With maven:
* can get maven from [maven site](https://maven.apache.org/download.cgi)
* put maven on path (exercise for reader)
* have `JAVA_HOME` set to a jdk home (exercise for reader)
* `mvn clean install` in the nailgun-git folder
* can launch assistant with `mvn compile exec:java` in assistant folder

###With IDE:
* launch install maven target for `nailgun` at least once
* use run from ide or exec:java target for assistant from ide

package task also needs jar-timestamp-normalize-maven-plugin from
[a fork](github.com/automaticgiant/jar-timestamp-normalize-maven-plugin) until
pr is merged upstream (so builds artifacts can be binary-identical)

##Database:
* apache derby has been brought into the main process as an embedded sql db
* network server is also enabled when assistant is running

###To connect in a Windows client:
(Or use [ij](https://db.apache.org/derby/papers/DerbyTut/ij_intro.html))

See [walkthrough](https://db.apache.org/derby/integrate/SQuirreL_Derby.html)
about SQuirrel SQL (recurse into instructions linked to about Squirrel SQL
installation).

The URL you will want to use for connecting to derby over network is
[jdbc:derby://localhost:1527/valet2k_history](jdbc:derby://localhost:1527/valet2k_history)
with user: APP (no password). This can also be used in IDE plugins. Additionally,
it should be possible to access the db directory (like `~/.config/valet` or
something) with embedded derby driver if you are so inclined.


##ML:
Currently, there is a `libml [n]` nail that will run the HistoryML nail code to
train, display, explain, and predict with a decision tree. It is currently
trained from TF-IDF on working directory and command, with labels coming from a
few bits of code that assign -50 for anything with nonzero elements of
pipestatus, 0 for missing(can't get that info, probably due to null typeset
column), and +10 for all zero. It displays `[n]` or 10 of the most highly rated
log entries with explanatory dimensions.

There is a windows bug (pipestatus parsing - #9)[#9] presently with babun/zsh
storing/displaying pipestatus differently, so the model will not get labels to
train on.
