#!/bin/sh
export TMPFILE=/tmp/whathappened.txt
export GREPDATE=`date -d yesterday +%Y-%m-%d`
rm $TMPFILE
egrep "(SAVING|CREATING|EDITING|DELETING)" /var/log/tomcat7/tdar.$GREPDATE.log > $TMPFILE
export FSIZE=`stat $TMPFILE -c "%s"`
if [ "$FSIZE" -gt "0" ];
then
  mailx -s "daily update for $GREPDATE" adam.brin@asu.edu < $TMPFILE
fi
