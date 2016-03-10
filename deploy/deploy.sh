#!/usr/bin/sh
echoerr() { echo "$@" 1>&2; }

TDIR=/home/tdar/app-support/
ADIR=/home/tdar/deploy-archive/

mkdir -p ${ADIR}

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


echoerr() { echo "$@" 1>&2; }
sudo echo "deploying production: " + ${date}
#java -XshowSettings 2>&1 | grep tmp | awk -F= '{print "wro4jDir:" $2 "/.wro4j/"}'

if [  $(id -u) -eq 0  ]
 then
   echoerr "This script should NOT be run as root"
   exit 1
fi

echo "the following users are logged in:"
LOGGED=$(curl -silent https://core.tdar.org/admin/loggedIn | grep "\[")
echo "\t$LOGGED"

while true; do
    read -p "Is the build clean (GREEN) and are there NO ACTIVE users? " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) exit;;
        * ) echo "Please answer yes or no.";;
    esac
done

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



hg pull
hg update ${PARAM} -C
# -Ddeploy.version=f9ad4d9b7942
VERSION=`hg id -i`
EXTRA=-Ddeploy.version=${VERSION}

perl ../release/release.pl
if [ -e /usr/local/lib/nagios3/amazon-sns-notify.rb ]
then
       /usr/local/lib/nagios3/amazon-sns-notify.rb "tdar.org > DEPLOY - $(hg id -n -b)"
fi


echo "setting version to ${EXTRA}"
mvn clean compile -Pversion ${EXTRA}
handleError

echo "downloading versions..."
mvn clean package -Pprepare -q
handleError

cd target/

echo "setup:web"
rm -Rrf WEB-INF/classes/
mkdir -p WEB-INF/classes/
cp ../../web/src/main/resources/log4j2.xml WEB-INF/classes/
cp ../../web/src/main/resources/*-local-settings.xml WEB-INF/classes/
jar uvf web.war WEB-INF/classes/
handleError

echo "setup:tag"
rm -Rrf WEB-INF/classes/
mkdir -p WEB-INF/classes/
cp ../../tag/src/main/resources/log4j2.xml WEB-INF/classes/
cp ../../tag/src/main/resources/*-local-settings.xml WEB-INF/classes/
jar uvf tag.war WEB-INF/classes/
handleError

echo "setup:oai-pmh"
rm -Rrf WEB-INF/classes/
mkdir -p WEB-INF/classes/
cp ../../oai-pmh/src/main/resources/log4j2.xml WEB-INF/classes/
# cp ../../oai-pmh/src/main/resources/*-local-settings.xml WEB-INF/classes/
jar uvf oai-pmh.war WEB-INF/classes/
handleError


echo "setup:dataone"
rm -Rrf WEB-INF/classes/
mkdir -p WEB-INF/classes/
cp ../../dataone/src/main/resources/log4j2.xml WEB-INF/classes/
cp ../../dataone/src/main/resources/*-local-settings.xml WEB-INF/classes/
jar uvf dataone.war WEB-INF/classes/
handleError

cp *.war ${ADIR}
sudo service tomcat7 stop

echo "update:database"
mvn package -Pprepare,updateDB -q
handleError

sudo rm -Rrf /home/tdar/app/*
cp web.war $TDIR/ROOT.war
sudo service tdar-tomcat7 start

sudo service tdar-tomcat7 stop

sudo rm -Rrf /home/tdar/app-support/*
cp tag.war $TDIR/services.war
cp dataone.war $TDIR/dataone.war
cp oai-pmh.war $TDIR/oai-pmh.war
sudo service tdar-tomcat7 start
echo "done: ${date}"

