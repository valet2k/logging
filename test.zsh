#!/usr/bin/zsh

setopt ERR_EXIT
setopt PRINT_EXIT_VALUE

source ./load.zsh
(cd $valet2k_repo/assistant && nohup java -jar ./target/assistant-*-normalized.jar)&
sleep 20

$valet2k_ng ng-alias | grep lognew
$valet2k_ng lognew testentry
$valet2k_ng logshow | grep testentry
$valet2k_ng logshow | grep logging

