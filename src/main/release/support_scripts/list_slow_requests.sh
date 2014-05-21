#!/bin/sh
#lists slow requests from the tomcat logs
grep ' ms' $1 | sed  "s/^\(.*\)activity: \(.*\) \[\(.*\) (\(.*\) ms)/\4 \2/" | awk '{printf("%.6d\t%s\n",$1,$2)}' | sort