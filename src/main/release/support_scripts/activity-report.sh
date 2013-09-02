#!/bin/sh
# gives some understanding of what happened the day before by grepping and counting the number of views, searches, etc. from the tdar log files
GREPDATE=`date -d yesterday +%Y-%m-%d`
FILE="/var/log/tomcat7/tdar.$GREPDATE.log"
CREATING=`grep -c "CREATING" ${FILE}`
RSS=`grep -c  "begin: GET:/search/rss" ${FILE}`
OAI=`grep -c "begin: GET:/oai-pmh/oai" ${FILE}`
SAVING=`grep -c "SAVING" ${FILE}`
SEARCH_DOWNLOAD=`grep -c "begin: GET:/search/download" ${FILE}`
DELETING=`grep -c "DELETING" ${FILE}`
SEARCH=`grep -c "SEARCH:" ${FILE}`
CREATOR=`grep -c "browseCreators:" ${FILE}`
COLLECTION=`grep -c "browseCollections:" ${FILE}`
PROJECT=`grep -c "ProjectBrowse:" ${FILE}`
VIEW=`grep -c "/view?" ${FILE}`
echo "DAY : ${FILE}"
echo "create: ${CREATING}"
echo "save: ${SAVING}"
echo "delete: ${DELETING}"
echo "searches: ${SEARCH}"
echo "downloaded searches: ${SEARCH_DOWNLOAD}"
echo "rss: ${RSS}"
echo "OAI: ${OAI}"
echo "creator browse: ${CREATOR}"
echo "collection browse: ${COLLECTION}"
echo "project browse: ${PROJECT}"
echo "view: ${VIEW}"