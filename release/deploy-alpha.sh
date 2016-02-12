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
mvn clean install -DskipTests -Djetty.skip=true -pl database,locales,core,base,search
mvn clean compile war:war -Pminify-web-resources -pl tag,web,oai-pmh,dataone
mvn process-resources -Pliquibase -pl web

if [ $? -ne 0 ] 
  then
   echoerr "==============================================="
   echoerr "|               BUILD FAILED                  |"
   echoerr "|             SKIPPING  DEPLOY                |"
   echoerr "==============================================="
   exit 1
  else
    sudo service tomcat7 stop
    sudo cp web/target/tdar-web.war ~tdar/app/ROOT.war
    sudo rm -Rrf ~tdar/app/ROOT
    sudo cp oai-pmh/target/tdar-oai-pmh.war ~tdar/app/oai-pmh.war
    sudo rm -Rrf ~tdar/app/oai-pmh
    sudo cp dataone/target/tdar-dataone.war ~tdar/app/dataone.war
    sudo rm -Rrf ~tdar/app/dataone
    # sudo cp tag/target/tdar-tag.war ~tdar/app/services.war
    # sudo rm -Rrf ~tdar/app/services
    sudo service tomcat7 restart
fi


