#!/usr/bin/zsh

setopt ERR_EXIT
setopt PRINT_EXIT_VALUE

echo sourcing load.zsh
source ./load.zsh

echo starting assistant in background
(cd $valet2k_repo/assistant && java -jar ./target/assistant-*-normalized.jar&)
echo waiting for 10s for startup
sleep 10

echo testing lognew
$valet2k_ng ng-alias | grep lognew
echo | $valet2k_ng lognew testentry
echo looking for command
$valet2k_ng logshow | grep testentry
echo looking for workingdir
$valet2k_ng logshow | grep logging

