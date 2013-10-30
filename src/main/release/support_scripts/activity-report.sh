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
API=`zgrep ":/api/" ${FILE} | grep -c "begin"`
API_VIEW=`zgrep -c "API VIEWING" ${FILE}`
API_UPDATE=`zgrep -c "API UPDATED" ${FILE}`
API_SAVE=`zgrep -c "API CREATED" ${FILE}`

ADVANCEDSEARCH=`zgrep -c "searchType=advanced" ${FILE}`
ADVANCEDSEARCH=`expr ${ADVANCEDSEARCH} / 2`

DOWNLOADS=`zgrep "downloading" ${FILE} | grep -v "deriv" | wc -l`

CREATOR=`zgrep -c "browseCreators:" ${FILE}`
COLLECTION=`zgrep -c "browseCollections:" ${FILE}`
PROJECT=`zgrep -c "ProjectBrowse:" ${FILE}`
VIEW=`zgrep -c "/view?" ${FILE}`

UNIQUE_USERS=`zgrep "logged in" ${FILE} | awk '/ \- /{print $11}' | sort -u | wc -l`
USERS=`zgrep "logged in" ${FILE} | awk '/ \- /{print $11}' | wc -l`

echo "DAY : ${FILE}"
echo "USERS: ${USERS} (unique: ${UNIQUE_USERS})"
echo "DOWNLOADS: ${DOWNLOADS}"
echo "create: ${CREATING}"
echo "save: ${SAVING}"
echo "delete: ${DELETING}"
echo "searches: ${SEARCH} (simple: ${SIMPLESEARCH} ; advanced: ${ADVANCEDSEARCH})"
echo "downloaded searches: ${SEARCH_DOWNLOAD}"
echo "rss: ${RSS}"
echo "OAI: ${OAI}"
echo "creator browse: ${CREATOR}"
echo "collection browse: ${COLLECTION}"
echo "project browse: ${PROJECT}"
echo "view: ${VIEW}"

echo "API: ${API} (view: ${API_VIEW}, create: ${API_SAVE}, update: ${API_UPDATE})"