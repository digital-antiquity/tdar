#!/bin/sh
echoerr() { echo "$@" 1>&2; }
sudo echo "deploying production"

if [  $(id -u) -eq 0  ]
 then
   echoerr "This script should NOT be run as root"
   exit 1
fi

cd /home/tdar/tdar.src/
hg pull
hg update -C
perl src/main/release/release.pl
mvn clean compile war:war -Pproduction
if [ $? -ne 0 ] 
  then
   echoerr "==============================================="
   echoerr "|               BUILD FAILED                  |"
   echoerr "|             SKIPPING  DEPLOY                |"
   echoerr "==============================================="
   exit 1
  else
    sudo service tomcat6 stop
    sudo rm -Rrf ~tdar/app/ROOT
    sudo service tomcat6 restart
fi
