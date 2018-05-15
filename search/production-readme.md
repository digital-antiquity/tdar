### Notes on installing SOLR in production

1. Download SOLR and install it following: https://lucene.apache.org/solr/guide/6_6/taking-solr-to-production.html#taking-solr-to-production
2. adjust /etc/defaults/solr.in.sh  (set memory limits, data directory, and change request header size)
    * SOLR_JAVA_MEM="-Xms512m -Xmx4096m"
    * SOLR_OPTS="$SOLR_OPTS -Xss256k -Djetty.host=127.0.0.1 -Dsolr.data.dir=/home/tdar/solr-indexes/ -Dsolr.jetty.request.header.size=65535 "
3. install JTS.jar e.g. jts-1.13.jar (same version that's in tDAR). JTS is Java Topology Suite and is necessary for indexing.  it should live in server/lib/
4. if the data-dir is not setup copy src/main/resources/solr/ to /var/solr/data/