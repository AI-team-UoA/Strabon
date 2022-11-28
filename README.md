# Strabon
<br/>

## Installing Strabon on Linux ( Ubuntu 20.04 )

### Prerequisites

1. Install maven:

        sudo apt-get install maven

2. Install Java 11:

        sudo apt-get install openjdk-11-jdk openjdk-11-jre

### Installing PostgreSQL and PostGIS

1. Install PostgreSQL 12.0 :

        sudo apt-get install postgresql-12

2. Install PostGIS 3.0 :

        sudo apt-get install postgresql-12-postgis-3

3. Provide a password for default user (postgres)

        sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"
        
### Creating a spatially enabled database for PostgreSQL

1. Set postgis-3 path:

        POSTGIS_SQL_PATH=`pg_config --sharedir`/contrib/postgis-3
        
2. Create the spatial database that will be used as a template:

        createdb -E UTF8 -T template0 template_postgis
