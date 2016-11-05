#!/bin/sh

#dev setup
cd /tdar.src/
mvn clean install -DskipTests
cd web

mvn clean compile -Pliquibase-setup-dev-instance
mvn clean compile jetty:run -Pliquibase
