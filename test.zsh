#!/usr/bin/zsh

setopt ERR_EXIT
setopt PRINT_EXIT_VALUE

source ./load.zsh
(cd $valet2k_repo/assistant && java -jar ./target/assistant-*-normalized.jar&)
sleep 10

$valet2k_ng ng-alias | grep lognew
echo | $valet2k_ng lognew testentry
$valet2k_ng logshow | grep testentry
$valet2k_ng logshow | grep logging

