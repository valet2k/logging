#!/bin/sh
jarname=assistant-latest.jar
mv assistant/target/assistant*.jar deploy/$jarname
mkdir deploy
cd deploy
wget https://s3.amazonaws.com/valet2k/builds/$jarname.md5
md5sum -c $jarname.md5 && rm $jarname && md5sum $jarname > $jarname.md5
cd ..
