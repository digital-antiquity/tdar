#!/usr/bin/sh
echoerr() { echo "$@" 1>&2; }

TDIR=/home/tdar/app/


handleError () 
{
if [ $? -ne 0 ] 
  then
   echoerr "==============================================="
   echoerr "|               BUILD FAILED                  |"
   echoerr "|             SKIPPING  DEPLOY                |"
   echoerr "==============================================="
   exit 1
fi
}

hg pull
hg update 
mvn clean package -Pprepare
handleError
sudo service tomcat7 stop

mvn package -PupdateDB
handleError

mkdir old/
cp $TDIR/*.war old/
sudo rm -Rrf /home/tdar/app/*
cp target/web.war $TDIR/ROOT.war
cp target/dataone.war $TDIR/dataone.war
cp target/oai-pmh.war $TDIR/oai-pmh.war
sudo service tomcat7 start


