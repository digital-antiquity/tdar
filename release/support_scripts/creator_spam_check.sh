#!/bin/sh
FILE="/tmp/spam_check.txt"
GREPDATE=`date -d yesterday +%Y-%m-%d`
if [ -e "$FILE" ]; then
  rm ${FILE}
fi

psql -U tdar -d tdarmetadata -f /home/abrin/sql/creator_spam_check.sql -o ${FILE}
COUNT=`grep -c '0 rows' ${FILE}`
if [ "$COUNT" -eq 0 ]; then
  cat ${FILE} | mail -s "new registered users in tDAR (${GREPDATE})" info@digitalantiquity.org
fi
sleep 60
