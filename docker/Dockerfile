# Dockerfile for universalregistrar/driver-did-v1

FROM maven:3-eclipse-temurin-17-focal AS build
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-v1

ADD . /opt/driver-did-v1
RUN cd /opt/driver-did-v1 && mvn clean install -P war -N -DskipTests


FROM jetty:11.0.17-jre17-eclipse-temurin
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root
# install dependencies

ENV APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=DontWarn
RUN apt-get -y update && \
    apt-get install -y --no-install-recommends curl && \
    curl -sL https://deb.nodesource.com/setup_16.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN cd /opt/ && \
    npm install did-cli@0.6.0

#USER jetty

# variables

ENV uniregistrar_driver_did_v1_trustAnchorSeed=

# copy from build-stage
COPY --from=build --chown=jetty /opt/driver-did-v1/target/*.war /var/lib/jetty/webapps/ROOT.war

# done

EXPOSE 9080
CMD java -Djetty.http.port=9080 -jar /usr/local/jetty/start.jar
