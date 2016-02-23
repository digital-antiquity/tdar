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


echoerr() { echo "$@" 1>&2; }
sudo echo "deploying production"
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


mvn clean compile -Pversion ${EXTRA}
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


