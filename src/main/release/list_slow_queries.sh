#!/bin/sh
#lists slow database queries sorted by time from the postgres logs
grep "duration" ../postgresql/postgresql-Mon.log |  sed "s/^\(.*\) duration\: \(.*\) ms  execute \(.*\)/\2 \3/" | awk '{printf("%.6d\t%s\n",$1,$0 )}' | sort
