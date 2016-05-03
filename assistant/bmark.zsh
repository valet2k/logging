#!/usr/bin/env zsh
$valet2k_ng ng-stop
(mvn exec:java &> assistant.log) &
sleep 10

prefix=$(git rev-parse HEAD)

date > $prefix.default.log
$valet2k_ng logml train &>> $prefix.default.log
date >> $prefix.default.log
$valet2k_ng logml suggest &>> $prefix.default.log
date >> $prefix.default.log
$valet2k_ng logml test &>> $prefix.default.log
date >> $prefix.default.log

date > $prefix.second.log
$valet2k_ng logml train &>> $prefix.second.log
date >> $prefix.second.log
$valet2k_ng logml suggest &>> $prefix.second.log
date >> $prefix.second.log
$valet2k_ng logml test &>> $prefix.second.log
date >> $prefix.second.log


date > $prefix.10000.log
$valet2k_ng logml train -v 10000 &>> $prefix.10000.log
date >> $prefix.10000.log
$valet2k_ng logml suggest &>> $prefix.10000.log
date >> $prefix.10000.log
$valet2k_ng logml test &>> $prefix.10000.log
date >> $prefix.10000.log

date > $prefix.50000.log
$valet2k_ng logml train -v 50000 &>> $prefix.50000.log
date >> $prefix.50000.log
$valet2k_ng logml suggest &>> $prefix.50000.log
date >> $prefix.50000.log
$valet2k_ng logml test &>> $prefix.50000.log
date >> $prefix.50000.log

date > $prefix.50000.log
$valet2k_ng logml train -v 50000 &>> $prefix.50000.log
date >> $prefix.50000.log
$valet2k_ng logml suggest &>> $prefix.50000.log
date >> $prefix.50000.log
$valet2k_ng logml test &>> $prefix.50000.log
date >> $prefix.50000.log
