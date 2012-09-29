#!/bin/sh
pg_dump --encoding=UNICODE -D -O -x -U tdar tdarmetadata > tdarmetadata.sql
