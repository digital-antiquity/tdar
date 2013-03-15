#!/bin/sh
echoerr() { echo "$@" 1>&2; }
sudo echo "deploying alpha"

if [  $(id -u) -eq 0  ]
 then
   echoerr "This script should NOT be run as root"
   exit 1
fi

cd /home/tdar/tdar.src/
hg pull
#hg update -C
mvn clean compile war:war -Palpha
if [ $? -ne 0 ] 
  then
   echoerr "==============================================="
   echoerr "|               BUILD FAILED                  |"
   echoerr "|             SKIPPING  DEPLOY                |"
   echoerr "==============================================="
   exit 1
  else
    sudo service tomcat7 stop
    sudo rm -Rrf ~tdar/app/ROOT
    sudo service tomcat7 restart
fi
