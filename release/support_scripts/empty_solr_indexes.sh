#!/bin/#!/usr/bin/env bash

# This script will empty the tdar solr cores on localhost.  Helpful for dev instances; Less-helpful if used on production.

curl http://localhost:8983/solr/annotationKeys/update?commit=true -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'
curl http://localhost:8983/solr/collections/update?commit=true -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'
curl http://localhost:8983/solr/contents/update?commit=true -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'
curl http://localhost:8983/solr/dataMappings/update?commit=true -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'
curl http://localhost:8983/solr/institutions/update?commit=true -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'
curl http://localhost:8983/solr/keywords/update?commit=true -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'
curl http://localhost:8983/solr/people/update?commit=true -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'
curl http://localhost:8983/solr/resources/update?commit=true -H "Content-Type: text/xml" --data-binary '<delete><query>*:*</query></delete>'
