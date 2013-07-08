#!/bin/sh
# gives some understanding of what happened the day before by grepping and counting the number of views, searches, etc. from the tdar log files
GREPDATE=`date -d yesterday +%Y-%m-%d`
FILE="/var/log/tomcat6/tdar.$GREPDATE.log"
CREATING=`grep -c "CREATING" ${FILE}`
SAVING=`grep -c "SAVING" ${FILE}`
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
echo "creator browse: ${CREATOR}"
echo "collection browse: ${COLLECTION}"
echo "project browse: ${PROJECT}"
echo "view: ${VIEW}"

