#!/bin/sh
sudo echo "deploying production"

if [  $(id -u) -eq 0  ]
 then
   echo "This script should NOT be run as root" 1>&2
   exit 1
fi

cd /home/tdar/tdar.src/
hg pull
hg update -C
perl src/main/release/release.pl
mvn clean compile war:war -Pproduction
sudo service tomcat6 stop
sudo rm -Rrf ~tdar/app/ROOT
sudo service tomcat6 restart
sudo service apache2 restart
