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

1. Log in as superuser
        
        sudo su - postgres

2. Create the spatial database that will be used as a template:

        createdb -E UTF8 -T template0 template_postgis
        
3. Create a PostGIS extention:

        psql -d test_for_postgis -c "CREATE EXTENSION postgis"  
       
### Allow users to alter spatial tables

        psql -d template_postgis -c "GRANT ALL ON geometry_columns TO PUBLIC;"
        psql -d template_postgis -c "GRANT ALL ON geography_columns TO PUBLIC;"
        psql -d template_postgis -c "GRANT ALL ON spatial_ref_sys TO PUBLIC;"

### Perform garbage collection

        psql -d template_postgis -c "VACUUM FULL;"
        psql -d template_postgis -c "VACUUM FREEZE;"


 ### Allows non-superusers the ability to create from this template
 
        psql -d postgres -c "UPDATE pg_database SET datistemplate='true' WHERE datname='template_postgis';"
        psql -d postgres -c "UPDATE pg_database SET datallowconn='false' WHERE datname='template_postgis';"
        
### Create a spatially-enabled database named endpoint

        createdb endpoint -T template_postgis
        
### Compiling Strabon

1. Clone the source code of Strabon from this repository.

2. Change to the directory that Strabon source code resides in:

        cd Strabon
        
 3. Build Strabon:
 
        mvn clean pacakge
        
        
### Tuning PostgreSQL

The default settings of Postgres are rather conservative. As a result, parameter tuning is neccessary for speeding up Postgres, therefore Strabon. If you are using Strabon to compare its performance against your implementation of stSPARQL/GeoSPARQL, you are *strongly* encouraged to contact us using the Strabon Users mailing list for assistance on tuning Postgres. You can follow the instructions below for tuning a Postgres server running on an Ubuntu machine that is dedicated to PostgreSQL and Strabon.

1. Append the following text at the end of postgresql.conf, **Uncomment** the appropriate lines :

        ### RAM
        ## 4 GB of RAM
        #shared_buffers       =  3GB
        #effective_cache_size =  3GB
        #maintenance_work_mem =  1GB
        #work_mem             =  2GB
        ## 8 GB of RAM
        #shared_buffers       =  5GB
        #effective_cache_size =  6GB
        #maintenance_work_mem =  2GB
        #work_mem             =  5GB
        ## 16 GB of RAM
        #shared_buffers       = 10GB
        #effective_cache_size = 14GB
        #maintenance_work_mem =  4GB
        #work_mem             = 10GB
        ## 24 GB of RAM
        #shared_buffers       = 16GB
        #effective_cache_size = 22GB
        #maintenance_work_mem =  6GB
        #work_mem             = 15GB
        ## 48 GB of RAM
        #shared_buffers       = 32GB
        #effective_cache_size = 46GB
        #maintenance_work_mem =  8GB
        #work_mem             = 30GB
        ## 64 GB of RAM
        # contact us to find out!
        ### HD
        ## RAID with ordinary 7.200 disks
        #random_page_cost = 3.5 #3.0-3.5
        ## High-End NAS/SAN
        #random_page_cost = 2 #1.5-2.5
        ## Amazon EBS/Heroku
        #random_page_cost = 1.3 #1.1-2.0
        ## SSD array
        #random_page_cost = 2.0 #1.5-2.5
        ### Planner options
        # Increase the following values in order to avoid using the GEQO planner.
        # Small values (<8) reduce planning time but may produce inferior query plans
        #
        geqo_threshold = 15 # keep this value larger that the following two parameters
        from_collapse_limit = 14
        join_collapse_limit = 14
        ### Misc
        default_statistics_target    = 10000
        constraint_exclusion         = on
        checkpoint_completion_target = 0.9
        wal_buffers                  = 32MB
        ### Connections
        max_connections              = 10
        
 2. Append the following lines at the end of /etc/sysctl.conf, **Uncomment** the appropriate lines:
 
        ## 4 GB of RAM
        #kernel.shmmax = 3758096384
        #kernel.shmall = 3758096384
        #kernel.shmmni = 4096
        ## 8 GB of RAM
        #kernel.shmmax = 5905580032
        #kernel.shmall = 5905580032
        #kernel.shmmni = 4096
        ## 16 GB of RAM
        #kernel.shmmax = 11274289152
        #kernel.shmall = 11274289152
        #kernel.shmmni = 4096
        ## 24 GB of RAM
        #kernel.shmmax = 17716740096
        #kernel.shmall = 17716740096
        #kernel.shmmni = 4096
        ## 48 GB of RAM
        #kernel.shmmax = 35433480192
        #kernel.shmall = 35433480192
        #kernel.shmmni = 4224
        ## 64 GB of RAM
        # contact us to find out!
        
3. Apply all changes by executing :

        sudo sysctl -p
        sudo /etc/init.d/postgresql restart
        
4. Prepare for the next run by issuing the command, where db is the name of the Postgres database that Strabon will use:

        sudo -u postgres psql -c 'VACUUM ANALYZE;' db
        
      
<br/>


## Installing Strabon on Windows

1. First download PostgreSQL database from [here](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads). (try Version 12.13)

2. Then install the .exe file

3. When you will be asked for a password for the user postgres, enter postgres

4. For anything else, keep the defaults

5. Then, in the Stack builder select "PostgreSQL 12 on port 5432"

6. Then check the Spatial Extension PostGIS 3.0 for PostgreSQL 12

7. Then, in the PostGIS installation, check the option "Create spatial database"

8. Then enter the password for the user postgres (it must be postgres from the step 3)

9. Then for the Database Name enter "template_postgis"

10. Then press Yes, Close and Finish

11. Now you have PostgreSQL and PostGIS installed

12. Now open pgadmin III (it was installed with postgreSQL)

13. Create a new database with the name "endpoint" and in the definition select as template the "template_postgis"



<br/>


## Installing a Strabon Endpoint

### Tomcat

1. Install Tomcat compatible with Java 11 (eg. 9.0.68).

2. Add a new user in the tomcat-users.xml file. This file is usually located in the folder $TOMCAT_HOME/conf. Note that this user should be a member of the manager group. For example, add the following line inside a tomcat-users element to the file tomcat-users.conf and restart tomcat:

        <user username="endpoint" password="endpoint" roles="manager-gui"/>

### Initial install

1. Create a directory where you want to place the sources of the Strabon on your machine, and `cd` into that directory, e.g.:

        mkdir Strabon
        cd Strabon
        
2. Clone the source code from this repository.

3. The source code of the endpoint is located at the folder Strabon of the  source code tree that you just cloned, so `cd` into that directory, e.g. :

       cd Strabon
       
4. Edit the endpoint/WebContent/WEB-INF/connection.properties file and define the PostgreSQL host, the database name and the credentials that will be used by for storing stRDF metadata.

5. Compile the endpoint by calling:

        mvn clean package
    
The result of this action is the creation of a .war file inside the target directory.

6. Deploy the endpoint by placing the .war file that was created inside the webapps folder of your Tomcat installation, e.g.:
      
        sudo cp endpoint/target/strabon-endpoint-*.war /opt/tomcat/webapps/strabonendpoint.war

7. Start your Tomcat server, e.g.:

      sudo systemctl start tomcat

8. Open a browser and go to localhost:<port>/manager/html where <port> is the port number you specified during Tomcat’s installation (the default port is 8080).
        

