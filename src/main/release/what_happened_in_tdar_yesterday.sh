#!/bin/sh
export TMPFILE=/tmp/whathappened.txt
export GREPDATE=`date -d yesterday +%Y-%m-%d`
rm $TMPFILE
egrep "$GREPDATE(.+)(SAVING|CREATING|EDITING|DELETING)" /var/log/tomcat6/catalina.out* > $TMPFILE
#perl -pi -e"s/^(.+)\s\s([\d\-\:\s]+)(.+) \- (.+)/$2 ::: $4/ig" $TMPFILE
mailx -s "daily update for $GREPDATE" adam.brin@asu.edu < $TMPFILE
