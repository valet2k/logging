#!/bin/sh
jarname=assistant-latest.jar
s3=https://s3.amazonaws.com/valet2k/builds/
mkdir deploy
mv assistant/target/assistant*.jar deploy/$jarname
cd deploy
sha512sum $jarname > $jarname.sha512
curl $s3$jarname.sha512 | sha512sum -c && rm $jarname $jarname.sha512
cd ..
find deploy
