# The Digital Archaeological Record

[The Digital Archaeological Record (tDAR)](http://www.tdar.org) is an international digital repository for
archaeological investigations. tDAR’s development and maintenance are governed by [Digital Antiquity](http://www.digitalantiquity.org),
an organization dedicated to ensuring the long-term preservation of irreplaceable archaeological data and to broadening
the access to these data. 

For more information, please visit http://www.tdar.org

# prerequisites to installing tDAR

* Postgres 9.5 (with PostGIS extensions for production)
* Java 8
* Maven 3.5
* Mercurial 
* git (for npm packages)
* npm 5.6+
* Chrome (for Selenium)
* chromedriver (for selenium; in unix assumed to be in /usr/local/bin/chromedriver; if mac /Applications/chromedriver )
* phantomjs (for karma tests)

## getting the basic environment setup
1. check out the source from http://bitbucket.org/tdar/tdar.src
2. do a first clean install *mvn clean install -DskipTests*
3. cd "web" to move into the "web" package
4. run *mvn clean compile -Psetup-new-instance* to copy template configuration files into the src/main/resources/ directory

## setting up postgres

1. install postgres with postgis extensions
2. create tdar user with login permissions from localhost, tests assume password of 'tdar'
3. create the following databases with tdar as the owner:
* *test_tdarmetadata*
* *test_tdardata*
* *tdarmetadata*
* *tdardata*
* _tdargis_ (optional, using the posgis template)

### setting the tdar database

1. run *mvn clean install -DskipTests -Pliquibase-setup-dev-instance*

## next steps
1. try running tdar in the web package: mvn clean compile jetty:run

## testing tdar:
mvn clean verify -Ptest
