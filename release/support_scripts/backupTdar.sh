#!/bin/sh
export DATE=`date +%Y-%m-%d`
export DAYS=7
export TMPFILE=/tmp/backup${DATE}.txt
export BKPLOG=/home/tdar/filestore/logs/backup/${DATE}.log
export BKPERR=/home/tdar/filestore/logs/backup/${DATE}.err
find /home/tdar/filestore -mtime -${DAYS} -type f -print > ${TMPFILE}
tar -cvzf /home/tdar/backups/backup${DATE}.tgz -T ${TMPFILE} 1>${BKPLOG} 2>${BKPERR}

echo "backup for ${DAYS} days before ${DATE}"
echo ""
echo "::ERRORS::"
cat ${BKPERR}
echo ""
echo ""
echo "::FILES::"
cat ${BKPLOG}
