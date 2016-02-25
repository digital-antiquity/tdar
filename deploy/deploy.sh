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
EXTRA=-Ddeploy.version=${PARAM}

perl ../release/release.pl
if [ -e /usr/local/lib/nagios3/amazon-sns-notify.rb ]
then
       /usr/local/lib/nagios3/amazon-sns-notify.rb "tdar.org > DEPLOY - $(hg id -n -b)"
fi

echo "setup..."
mvn clean compile -Pversion ${EXTRA} -q
handleError

echo "build web..."
mvn clean compile war:war -P web -q
handleError

mv target/tdar-web.war ${ADIR}
sudo service tomcat7 stop

mvn package -Pprepare,updateDB -q
handleError

sudo rm -Rrf /home/tdar/app/*
cp ${ADIR}/tdar-web.war $TDIR/ROOT.war
sudo service tdar-tomcat7 start

echo "build tag..."
mvn clean compile war:war -P tag -q
handleError
mv target/tag.war ${ADIR}

echo "build oai-pmh..."
mvn clean compile war:war -P oai-pmh -q
handleError
mv target/oai-pmh.war ${ADIR}

echo "build dataone..."
mvn clean compile war:war -P dataone -q
handleError
mv target/dataone.war ${ADIR}

sudo service tdar-tomcat7 stop

sudo rm -Rrf /home/tdar/app-support/*
cp ${ADIR}/dataone.war $TDIR/dataone.war
cp ${ADIR}/tag.war $TDIR/services.war
cp ${ADIR}/oai-pmh.war $TDIR/oai-pmh.war
sudo service tdar-tomcat7 start
echo "done: ${date}"

