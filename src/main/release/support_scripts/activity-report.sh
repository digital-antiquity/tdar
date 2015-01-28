#!/bin/sh
GREPDATE=`date -d yesterday +%Y-%m-%d`
FILE="/var/log/tomcat7/tdar.$GREPDATE.log"
OFILE="/home/abrin/reports/$GREPDATE.log"
CREATING=`zgrep -c "CREATING" ${FILE}`
RSS=`zgrep -c  "begin: GET:/search/rss" ${FILE}`
OAI=`zgrep -c "begin: GET:/oai-pmh/oai" ${FILE}`
INTG_SELECT=`zegrep -c "begin: GET:/workspace/(select-tables|integrate)" ${FILE}`
INTG_COL=`zgrep -c "begin: POST:/workspace/select-columns" ${FILE}`
INTG_FILTER=`zgrep -c "begin: POST:/workspace/filter" ${FILE}`
INTG_RESULTS=`zgrep -c "begin: POST:/workspace/display-filtered" ${FILE}`
SAVING=`zgrep -c "SAVING" ${FILE}`
SEARCH_DOWNLOAD=`zgrep -c "begin: GET:/search/download" ${FILE}`
DELETING=`zgrep -c "DELETING" ${FILE}`
SEARCH=`zgrep -c "SEARCH:" ${FILE}`
KBROWSE=`zgrep -c "KeywordBrowse:" ${FILE}`
SIMPLESEARCH=`zgrep -c "searchType=simple" ${FILE}`
SIMPLESEARCH=`expr ${SIMPLESEARCH} / 2`
TAG=`zgrep -c "TAGGateway: Called" ${FILE} `
API=`zgrep ":/api/" ${FILE} | grep -c "begin"`
API_VIEW=`zgrep -c "API VIEWING" ${FILE}`
API_UPDATE=`zgrep -c "API UPDATED" ${FILE}`
API_SAVE=`zgrep -c "API CREATED" ${FILE}`
MODS=`zgrep  "/mods" ${FILE} | grep -c "begin" `
DC=`zgrep  "/dc" ${FILE} | grep -c "begin" `
BOOKMARKS=`zgrep  "/bookmark" ${FILE} | grep -c 'begin'`
ADVANCEDSEARCH=`zgrep -c "searchType=advanced" ${FILE}`
ADVANCEDSEARCH=`expr ${ADVANCEDSEARCH} / 2`

DOWNLOADS=`zgrep -i "downloading" ${FILE} | egrep -v "\_(sm|md|lg)\.jpg" | wc -l`

CREATOR=`zgrep -c "browseCreators:" ${FILE}`
COLLECTION=`zgrep -c "browseCollections:" ${FILE}`
COLLECTION_VIEW=`zegrep  "begin: GET:/collection/[0-9]+/.+" ${FILE} | grep -v "/edit" -c `
PROJECT=`zgrep -c "ProjectBrowse:" ${FILE}`
VIEW_=`zegrep "begin: GET:/(document|image|dataset|codingSheet|ontology|geospatial|project|sensoryData|audio|archive)/[0-9]+/.+" ${FILE} | grep -v "/edit" -c `
VIEW=$(($VIEW_ - $COLLECTION_VIEW))
UNIQUE_USERS=`zgrep "logged in" ${FILE} | awk '/ \- /{print $11}' | sort -u | wc -l`
USERS=`zgrep "logged in" ${FILE} | awk '/ \- /{print $11}' | wc -l`

echo "DAY : ${FILE}"
echo "USERS: ${USERS} (unique: ${UNIQUE_USERS})"
echo "DOWNLOADS: ${DOWNLOADS}"
echo "create: ${CREATING}"
echo "save: ${SAVING}"
echo "delete: ${DELETING}"
echo "view: ${VIEW} MODS: ${MODS} DC: ${DC} Collection: ${COLLECTION_VIEW}"
echo "searches: ${SEARCH} (simple: ${SIMPLESEARCH} ; advanced: ${ADVANCEDSEARCH})"
echo "downloaded searches: ${SEARCH_DOWNLOAD}"
echo "rss: ${RSS}"
echo "OAI: ${OAI} TAG: ${TAG}"
echo "Browse - Collection: ${COLLECTION} Keyword: ${KBROWSE} Project: ${PROJECT} Creator: ${CREATOR}"
echo "bookmarks: ${BOOKMARKS}"
echo "Integration Tables: ${INTG_SELECT} Cols: ${INTG_COL} Filter: ${INTG_FILTER} Results: ${INTG_RESULTS}"
echo "API: ${API} (view: ${API_VIEW}, create: ${API_SAVE}, update: ${API_UPDATE})"