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
hg update -C
mvn clean install -DskipTests -Djetty.skip=true
cd web
#sudo rm /tmp/.wro4j/buildContext.properties
#sudo rmdir /tmp/.wro4j/
mvn clean compile war:war -Pminify-web-resources,liquibase
cd ../dataone/
mvn clean compile war:war 
cd ../oai-pmh/
mvn clean compile war:war 

if [ $? -ne 0 ] 
  then
   echoerr "==============================================="
   echoerr "|               BUILD FAILED                  |"
   echoerr "|             SKIPPING  DEPLOY                |"
   echoerr "==============================================="
   exit 1
  else
    sudo service tomcat7 stop
    cd ../web/
    sudo cp target/tdar-web.war ~tdar/app/ROOT.war
    sudo rm -Rrf ~tdar/app/ROOT
    cd ../oai-pmh/
    sudo cp target/tdar-oai-pmh.war ~tdar/app/oai-pmh.war
    sudo rm -Rrf ~tdar/app/oai-pmh
    cd ../dataone/
    sudo cp target/tdar-dataone.war ~tdar/app/dataone.war
    sudo rm -Rrf ~tdar/app/dataone
    sudo service tomcat7 restart
fi


