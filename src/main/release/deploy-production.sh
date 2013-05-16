#!/bin/sh
echoerr() { echo "$@" 1>&2; }
sudo echo "deploying production"

if [  $(id -u) -eq 0  ]
 then
   echoerr "This script should NOT be run as root"
   exit 1
fi


export PARAM=" -C "
while getopts Dr: opt; do
  case $opt in
    r)
      export PARAM=" -r $OPTARG "
      ;;
    D)
      export PARAM=""
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

echo " hg update $PARAM "


cd /home/tdar/tdar.src/
hg pull
hg update $PARAM
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
    sudo service tomcat7 stop
    sudo rm -Rrf ~tdar/app/ROOT
    sudo service tomcat7 restart
fi
