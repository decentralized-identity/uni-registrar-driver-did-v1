# Dockerfile for universalregistrar/driver-did-v1

FROM ubuntu:18.04
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root

RUN apt-get -y update && \
    apt-get install -y --no-install-recommends openjdk-11-jdk-headless maven git && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/
ENV PATH $JAVA_HOME/bin:$PATH

# install dependencies

ENV APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE=DontWarn
RUN apt-get -y update && \
    apt-get install -y --no-install-recommends curl software-properties-common gpg gpg-agent make build-essential python && \
    curl -sL https://deb.nodesource.com/setup_8.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

#RUN cd /opt/ && \
#	mkdir did-cli && \
#	cd did-cli && \
#	npm install did-cli@0.4.1

RUN cd /opt/ && \
	git clone https://github.com/digitalbazaar/did-cli.git && \
	cd did-cli && \
	git checkout v0.6.0 && \
	npm install

# build driver-did-v1

ADD . /opt/driver-did-v1

RUN cd /opt/driver-did-v1 && mvn clean install package -N -DskipTests

# variables

ENV uniregistrar_driver_did_v1_trustAnchorSeed=

# done

EXPOSE 9080

RUN chmod a+rx /opt/driver-did-v1/docker/run-driver-did-v1.sh
CMD "/opt/driver-did-v1/docker/run-driver-did-v1.sh"
