# Scripts for Strabon

Various scripts functionalities are provided for Strabon.

## Docker

* Get into scripts/docker and run the Dockerfile :

        docker build -t strabon .
* A docker-image named strabon is available either locally (previously built), or in DockerHub. Use the docker-image (terminal of your machine): 

        docker run --name strabon-container -p 9999:8080 -v /<some_local_dir>:/inout strabon
* Now the docker-container strabon-container should be running. Use (the command line of) the container (different terminal on your machine): 

        docker exec -it strabon-container /bin/bash
* Finally, start tomcat on the docker terminal :
        
        ./usr/local/bin/conf.sh

Now Strabon is available in your machine's port 9999 :

http://localhost:9999/Strabon


* Going through the logs (terminal of your machine): docker logs docker-container-name
    
* You can make local files available in container's dir /inout by copying them in you machine's dir: /<some_local_dir>

## strabon-cmd & strabon-cmd.bat scripts

Script used to run the main classes of Strabon. The main classes of Strabon comprises QueryOp, UpdateOp, StoreOp, and  DescribeOp.

* strabon-cmd is compatible with Unix systems.
* strabon-cmd.bat is compatible with WIN os.

## endpoint script

Provides a script for executing SPARQL queries and SPARQL Update queries
as well as storing RDF triples on a Strabon Endpoint.

## Miscellaneous scripts

* redeploy: script to redeploy strabon after a new release
* example-query.py: a showcase of a sparql query run against a strabon endpoint in python
* storeTriples.sh : script to store files from a directory into strabon (must be changed manually)