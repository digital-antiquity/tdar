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
hg update -C
mvn clean compile -Pversion
mvn clean package -Pprepare -q
handleError
sudo service tomcat7 stop

mvn package -PupdateDB -q
handleError

if [ ! -e "old" ]
then
  mkdir old/
fi

cp $TDIR/*.war old/
sudo rm -Rrf /home/tdar/app/*
cp target/web.war $TDIR/ROOT.war
cp target/dataone.war $TDIR/dataone.war
cp target/oai-pmh.war $TDIR/oai-pmh.war
sudo service tomcat7 start


