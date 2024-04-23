# Dockerfile for universalregistrar/driver-did-v1

FROM maven:3-eclipse-temurin-17-focal AS build
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-v1

WORKDIR /opt/driver-did-v1
ADD pom.xml /opt/driver-did-v1
RUN mvn org.apache.maven.plugins:maven-dependency-plugin:3.3.0:go-offline
ADD src/main/webapp/WEB-INF /opt/driver-did-v1/src/main/webapp/WEB-INF
RUN mvn clean package -P war
ADD . /opt/driver-did-v1
RUN mvn clean package -P war

# build image

FROM jetty:12.0.8-jre17-eclipse-temurin
MAINTAINER Markus Sabadello <markus@danubetech.com>

# install dependencies

USER root

RUN apt-get -y update && \
    apt-get install -y --no-install-recommends curl && \
    curl -sL https://deb.nodesource.com/setup_16.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN cd /opt/ && \
    npm install did-cli@0.6.0

USER jetty

# variables

ENV uniregistrar_driver_did_v1_trustAnchorSeed=

# copy from build-stage

COPY --from=build --chown=jetty /opt/driver-did-v1/target/*.war /var/lib/jetty/webapps/ROOT.war

# done

EXPOSE 9080
CMD java -jar $JETTY_HOME/start.jar --add-modules=ee10-deploy && java -jar $JETTY_HOME/start.jar -Djetty.http.port=9080
