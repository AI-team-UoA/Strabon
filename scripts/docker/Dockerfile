############################
# Dockerfile for Strabon   #
# RK-team                  #
# Madgik                   #
# DI @ UoA                 #
#                          #
# java8                    #
# maven 3.6.3              #
# git 1.9.1                #
# tomcat 9.0.71            #
# postgres 12.13           #
# postgis 3.3.2            #
############################


FROM ubuntu:20.04

ARG DEBIAN_FRONTEND=noninteractive

ENV PORT 8080
ENV TOMCAT_MAJOR_VERSION 9
ENV TOMCAT_MINOR_VERSION 9.0.71


# INSTALL PREREQUISITIES
RUN apt-get update \
 && apt-get install -y \
    wget \
    openjdk-8-jdk \
    curl \
    git \
    maven \
    gnupg2 \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

#SET UP JAVA 8
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

#DOWNLOAD AND INSTALL maven 3.6.3
RUN wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz \
     && tar -xvf apache-maven-3.6.3-bin.tar.gz \
     && mv apache-maven-3.6.3 /opt/

RUN M2_HOME='/opt/apache-maven-3.6.3' \
     && PATH="$M2_HOME/bin:$PATH" \
     && export PATH

# DOWNLOAD AND INSTALL postgres AND postgis
RUN touch /etc/apt/sources.list.d/pgdg.list \
 && echo "deb http://apt.postgresql.org/pub/repos/apt/ focal-pgdg main" >> /etc/apt/sources.list.d/pgdg.list \
 && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -

RUN apt-get update \
 && apt-get install -y \
    postgresql-12 \
    postgresql-server-dev-12 \
    postgresql-12-postgis-3 \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

ADD initdb.sh enable_remote.sh /usr/local/bin/

RUN chmod -v +x /usr/local/bin/initdb.sh
RUN chown -v postgres:postgres /usr/local/bin/initdb.sh
RUN chmod -v +x /usr/local/bin/enable_remote.sh

USER postgres

## start postgres, add postgis, create template for strabon and stop postgres
RUN initdb.sh

USER root


# INSTALL TOMCAT
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-${TOMCAT_MAJOR_VERSION}/v${TOMCAT_MINOR_VERSION}/bin/apache-tomcat-${TOMCAT_MINOR_VERSION}.tar.gz \
 && wget -qO- https://archive.apache.org/dist/tomcat/tomcat-${TOMCAT_MAJOR_VERSION}/v${TOMCAT_MINOR_VERSION}/bin/apache-tomcat-${TOMCAT_MINOR_VERSION}.tar.gz.sha512 | sha512sum -c - \
 && tar zxf apache-tomcat-*.tar.gz \
 && rm apache-tomcat-*.tar.gz \
 && mv apache-tomcat* tomcat

# DOWNLOAD AND BUILD strabon
RUN git clone https://github.com/AI-team-UoA/Strabon \
 && cd Strabon \
 && mvn clean package

RUN cp /Strabon/endpoint/target/strabon-endpoint-3.3.3-SNAPSHOT.war /tomcat/webapps/Strabon.war \
 && rm -rf /Strabon

# FINAL CONFIGURATIONS
RUN mkdir /inout

ADD conf.sh /usr/local/bin/

RUN chmod -v +x /usr/local/bin/conf.sh

EXPOSE $PORT

CMD enable_remote.sh && su - postgres -c "/usr/lib/postgresql/12/bin/postgres -D /var/lib/postgresql/12/main -c config_file=/etc/postgresql/12/main/postgresql.conf"