#!/bin/sh

#tdar.2015-10-30.log.gz
GREPDATE=`date -d yesterday +%Y-%m-%d`
FILE="/var/log/tomcat7/tdar.$GREPDATE.log.gz"
OAIFILE="/var/log/tdar-tomcat7/oai-pmh.$GREPDATE.log.gz"
TAGFILE="/var/log/tdar-tomcat7/tag.$GREPDATE.log.gz"
DATAONEFILE="/var/log/tdar-tomcat7/dataone.$GREPDATE.log.gz"
OFILE="/home/abrin/reports/$GREPDATE.log"
CREATING=`zgrep -c "CREATING" ${FILE}`
RSS=`zgrep -c  "begin: GET:/search/rss" ${FILE}`
OAI=`zgrep  "> /oai-pmh/oai" ${OAIFILE}  | grep -v -c "nagios"`
#DATAONE=`zgrep -c "> /dataone/v1"  ${DATAONEFILE} `
INTG_SELECT=`zegrep -c "begin: GET:/workspace/(select-tables|integrate)" ${FILE}`
INTG_COL=`zgrep -c "begin: POST:/workspace/select-columns" ${FILE}`
INTG_FILTER=`zgrep -c "begin: POST:/workspace/filter" ${FILE}`
INTG_RESULTS=`zgrep -c "begin: POST:/workspace/display-filtered" ${FILE}`
SAVING=`zgrep -c "SAVING" ${FILE}`
SEARCH_DOWNLOAD=`zgrep -c "begin: GET:/search/download" ${FILE}`
DELETING=`zgrep -c "DELETING" ${FILE}`
SEARCH=`zgrep -c "SEARCH:" ${FILE}`
KBROWSE=`zgrep -c "KeywordBrowse:" ${FILE}`
SIMPLESEARCH=`zegrep -c  "(.+)begin(.+)searchType=simple" ${FILE}`
if [ -f "$TAGFILE" ]
then
    TAG=`zgrep  "TAGGateway: Called" ${TAGFILE} /var/log/tdar-tomcat7/tag.log | grep -c ${GREPDATE} `
else
    TAG=`zgrep  "TAGGateway: Called"  /var/log/tdar-tomcat7/tag.log | grep -c ${GREPDATE} `
fi

if [ -f "$DATAONEFILE" ]
then
    DATAONE=`zgrep  ">> /dataone/v1" ${DATAONEFILE} /var/log/tdar-tomcat7/dataone.log | grep -v "nagios" | grep -c ${GREPDATE} `
else
    DATAONE=`zgrep  ">> /dataone/v1"  /var/log/tdar-tomcat7/dataone.log | grep -v "nagios" | grep -c ${GREPDATE} `
fi

API=`zgrep ":/api/" ${FILE} | grep -c "begin"`
API_VIEW=`zgrep -c "API VIEWING" ${FILE}`
API_UPDATE=`zgrep -c "API UPDATED" ${FILE}`
API_SAVE=`zgrep -c "API CREATED" ${FILE}`
MODS=`zgrep  "/mods" ${FILE} | grep -c "begin" `
DC=`zgrep  "/dc" ${FILE} | grep -c "begin" `
BOOKMARKS=`zgrep  "/bookmark" ${FILE} | grep -c 'begin'`
ADVANCEDSEARCH=`zegrep -c "(.+)begin(.+)searchType=advanced" ${FILE}`

DOWNLOADS=`zgrep -i "downloading" ${FILE} | egrep -v "\_(sm|md|lg)\.jpg" | wc -l`

CREATOR=`zgrep -c "browseCreators:" ${FILE}`
COLLECTION=`zgrep -c "browseCollections:" ${FILE}`
COLLECTION_VIEW=`zegrep  "begin: GET:/collection/[0-9]+/.+" ${FILE} | grep -v "/edit" -c `
PROJECT=`zgrep -c "ProjectBrowse:" ${FILE}`
VIEW_=`zegrep "begin: GET:/(document|image|dataset|codingSheet|ontology|geospatial|project|sensoryData|audio|archive)/[0-9]+/.+" ${FILE} | grep -v "/edit" -c `
VIEW=$(($VIEW_ - $COLLECTION_VIEW))
UNIQUE_USERS=`zgrep "logged in" ${FILE} | awk '/ \- /{print $11}' | sort -u | wc -l`
USERS=`zgrep "logged in" ${FILE} | awk '/ \- /{print $11}' | wc -l`

echo "DAY : ${FILE}" >> $OFILE
echo "USERS: ${USERS} (unique: ${UNIQUE_USERS})" >> $OFILE
echo "DOWNLOADS: ${DOWNLOADS}" >> $OFILE
echo "create: ${CREATING}" >> $OFILE
echo "save: ${SAVING}" >> $OFILE
echo "delete: ${DELETING}" >> $OFILE
echo "view: ${VIEW} MODS: ${MODS} DC: ${DC} Collection: ${COLLECTION_VIEW}" >> $OFILE
echo "searches: ${SEARCH} (simple: ${SIMPLESEARCH} ; advanced: ${ADVANCEDSEARCH})" >> $OFILE
echo "downloaded searches: ${SEARCH_DOWNLOAD}" >> $OFILE
echo "rss: ${RSS}" >> $OFILE
echo "OAI: ${OAI} TAG: ${TAG} DATAONE: ${DATAONE}" >> $OFILE
echo "Browse - Collection: ${COLLECTION} Keyword: ${KBROWSE} Project: ${PROJECT} Creator: ${CREATOR}" >> $OFILE
echo "bookmarks: ${BOOKMARKS}" >> $OFILE
echo "Integration Tables: ${INTG_SELECT} Cols: ${INTG_COL} Filter: ${INTG_FILTER} Results: ${INTG_RESULTS}" >> $OFILE
echo "API: ${API} (view: ${API_VIEW}, create: ${API_SAVE}, update: ${API_UPDATE})" >> $OFILE
cat $OFILE

