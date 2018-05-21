# The Digital Archaeological Record

[The Digital Archaeological Record (tDAR)](http://www.tdar.org) is an international digital repository for
archaeological investigations. tDAR’s development and maintenance are governed by [Digital Antiquity](http://www.digitalantiquity.org),
an organization dedicated to ensuring the long-term preservation of irreplaceable archaeological data and to broadening
the access to these data. 

For more information, please visit http://www.tdar.org

## Initial Setup

    # build a clean copy of tDAR without tests
    mvn clean install -DskipTests 

    # create database instances for tdar (tdarmetadata and tdardata)
    cd web/
    mvn clean compile -Psetup-new-instance,liquibase-setup-production-instance

## Running the webapp

    cd web/
    # edit src/main/resources/tdar.properties to point to localhost
    # edit src/main/resources/hibernate.properties to point to your local database
    mvn clean compile jetty:run -Pliquibase


## Testing

    # create the test_tdarmetadata database
    # install selenium's chromedriver
    # install chrome
    mvn clean verify -Ptest
