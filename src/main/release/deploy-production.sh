#!/bin/sh
sudo echo "deploying production"
cd /home/tdar/tdar.src/
svn update .
perl src/main/release/release.pl
mvn clean compile war:war -Pproduction
sudo service tomcat6 stop
sudo rm -Rrf ~tdar/app/ROOT
sudo service tomcat6 restart
sudo service apache2 restart
