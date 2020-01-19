# Instructions for how to setup tDAR

## Prerequisites:
1. Java 8 (JDK) (https://www.oracle.com/technetwork/java/javase/downloads/index.html#JDK8)
2. Maven 3.5.4+ (http://maven.apache.org/download.cgi)
3. Git 2.17+
4. NPM 6.4+
5. bower 1.8.4 (if not on yarn branch)
6. SOLR 7.3.1 (if you're not running it in 'embedded mode')
7. PostgreSQL 9.5 with Postgis 2.2 (in production)



## get tDAR:

1. Checkout tDAR from git https://github.com/digital-antiquity/tdar.git

## setting up the database(s) general:

1. Create a tdar user in the postgres database and grant them login permissions
2. Create the following normal databases: *tdarmetadata* and *tdardata* with owner "tdar"
3. Create a PostGIS database: *tdargis* with owner "tdar"

## setting up tDAR

1. In the main tdar foldar, compile and install tDAR: `mvn clean install -DskipTests`.  This will test that bower, npm, and maven are setup properly. It will also download all of tDAR dependencies
2. move into the "web directory" `cd web`
3. setup config files for tdar: `mvn clean compile -Psetup-new-instance`
    a. open the *hibernate.properties* in *src/main/resources* and edit the database names and tDAR username and password; save
    b.open the *tdar.properties* in *src/main/resources* and edit the properties for the various local path locations: *file.store.location*, *personal.file.store.location*, *hosted.file.store.location*, enable https: *https.enabled=true* the database names and tDAR username and password; save 


## setting up the database(s) with test data:

1. run tDAR's database setup `mvn clean install -Pliquibase-setup-dev-instance -DskipTests` in the "web" directory
2. register for tDAR
3. in Crowd grant yourself admin permissions
