#!/usr/bin/env zsh

# Export the installation directory
valet2k_repo=$(dirname "$(readlink -e "$0")")
lastpwd=$PWD

# Install maven
maven_url=http://www-us.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip
maven_zip="apache-maven-3.3.9-bin.zip"
test -f $maven_zip || curl -O $maven_url
test -d "apache-maven-3.3.9" || unzip $maven_zip &

# Export all necessary variables, and add folders to the PATH
export PATH=$PATH:$PWD/apache-maven-3.3.9/bin:$PWD/nailgun-git

# Build the nailgun and its Snapshot
make -C $valet2k_repo/nailgun-git ng &> /dev/null
export valet2k_ng=$valet2k_repo/nailgun-git/ng

cd $valet2k_repo/nailgun-git/nailgun-server
test -d "target" || mvn clean install &> /dev/null
cd $lastpwd

# Add a variable which store the path to ng
export theNailJar="$valet2k_repo/nailgun-server"

# Add a hook function
autoload add-zsh-hook
add-zsh-hook -d precmd log

function log(){
  #get detailed env and send last history line as args
  #TODO: change
  typeset | $valet2k_ng lognew $(fc -ln -1)
}
add-zsh-hook precmd log

# Add a function to stop valet2k and restart zsh
function quit()
{
	./$valet2k_ng ng-stop
	add-zsh-hook -D log
}


cd assistant

# Download the jar if necessary
s3=https://s3.amazonaws.com/valet2k/builds/assistant-latest.jar
jarname=assistant-latest.jar

test -f $jarname || curl -O $s3

# Start nailgun
$valet2k_ng ng-version &> /dev/null || java -jar assistant-latest.jar & &> /dev/null
sleep 10

#mvn compile exec:java &
cd ..

#../nailgun/ng ng-alias | grep lognew > /dev/null || nohup mvn exec:java&
# ../nailgun/ng ng-alias | grep lognew > /dev/null || nohup mvn exec:java &
# java -jar assistant-1.0-SNAPSHOT.jar &
