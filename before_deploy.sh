#!/bin/sh
test "$TRAVIS_BRANCH" = master && branch=latest
jarname=assistant-"$branch".jar
s3=https://s3.amazonaws.com/valet2k/builds/
mkdir deploy
mv assistant/target/assistant*normalized.jar deploy/$jarname
cd deploy
sha512sum $jarname | tee $jarname.sha512
curl $s3$jarname.sha512 | sha512sum -c && rm $jarname $jarname.sha512
cd ..
find deploy
