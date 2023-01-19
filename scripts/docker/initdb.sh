#!/bin/sh

#start postgres
service postgresql start

#Provide a password for default user (postgres)
psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"

#Create the spatial database that will be used as a template
createdb -E UTF8 -T template0 template_postgis

#Enbale postGIS extension on template_postgis
 psql -d template_postgis -c "CREATE EXTENSION postgis"

#Allow users to alter spatial tables
psql -d template_postgis -c "GRANT ALL ON geometry_columns TO PUBLIC;"
psql -d template_postgis -c "GRANT ALL ON geography_columns TO PUBLIC;"
psql -d template_postgis -c "GRANT ALL ON spatial_ref_sys TO PUBLIC;"

#Perform garbage collection
psql -d template_postgis -c "VACUUM FULL;"
psql -d template_postgis -c "VACUUM FREEZE;"

#Allows non-superusers the ability to create from this template.
psql -d postgres -c "UPDATE pg_database SET datistemplate='true' WHERE datname='template_postgis';"
psql -d postgres -c "UPDATE pg_database SET datallowconn='false' WHERE datname='template_postgis';"

#Create a spatially-enabled database named endpoint.
createdb endpoint -T template_postgis

#stop postgres
service postgresql stop