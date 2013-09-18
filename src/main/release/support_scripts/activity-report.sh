#!/bin/sh
GREPDATE=`date -d yesterday +%Y-%m-%d`
FILE="/var/log/tomcat7/tdar.$GREPDATE.log"
CREATING=`zgrep -c "CREATING" ${FILE}`
RSS=`zgrep -c  "begin: GET:/search/rss" ${FILE}`
OAI=`zgrep -c "begin: GET:/oai-pmh/oai" ${FILE}`
SAVING=`zgrep -c "SAVING" ${FILE}`
SEARCH_DOWNLOAD=`zgrep -c "begin: GET:/search/download" ${FILE}`
DELETING=`zgrep -c "DELETING" ${FILE}`
SEARCH=`zgrep -c "SEARCH:" ${FILE}`
SIMPLESEARCH=`zgrep -c "searchType=simple" ${FILE}`
SIMPLESEARCH=`expr ${SIMPLESEARCH} / 2`

ADVANCEDSEARCH=`zgrep -c "searchType=advanced" ${FILE}`
ADVANCEDSEARCH=`expr ${ADVANCEDSEARCH} / 2`

CREATOR=`zgrep -c "browseCreators:" ${FILE}`
COLLECTION=`zgrep -c "browseCollections:" ${FILE}`
PROJECT=`zgrep -c "ProjectBrowse:" ${FILE}`
VIEW=`zgrep -c "/view?" ${FILE}`
echo "DAY : ${FILE}"
echo "create: ${CREATING}"
echo "save: ${SAVING}"
echo "delete: ${DELETING}"
echo "searches: ${SEARCH} (simple: ${SIMPLESEARCH} ; advanced: ${ADVANCEDSEARCH} )"
echo "downloaded searches: ${SEARCH_DOWNLOAD}"
echo "rss: ${RSS}"
echo "OAI: ${OAI}"
echo "creator browse: ${CREATOR}"
echo "collection browse: ${COLLECTION}"
echo "project browse: ${PROJECT}"
echo "view: ${VIEW}"


