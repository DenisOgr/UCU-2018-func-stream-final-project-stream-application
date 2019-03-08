#
# Scala and sbt Dockerfile
#
# https://github.com/spikerlabs/scala-sbt (based on https://github.com/hseeberger/scala-sbt)
#

# Pull base image
FROM  openjdk:8-jre-alpine

ARG SCALA_VERSION
ARG SBT_VERSION

ENV SCALA_VERSION ${SCALA_VERSION:-2.12.8}
ENV SBT_VERSION ${SBT_VERSION:-1.2.4}

COPY . /app
WORKDIR /app

RUN apk update -qq \
    && update-ca-certificates \
    && apk add ca-certificates wget curl openssh bash procps openssl perl ttf-dejavu tini libc6-compat \
    && rm -rf /var/lib/{apt,dpkg,cache,log}/ /tmp/* /var/tmp/*

RUN \
  echo "$SCALA_VERSION $SBT_VERSION" && \
  mkdir -p /usr/lib/jvm/java-1.8-openjdk/jre && \
  touch /usr/lib/jvm/java-1.8-openjdk/jre/release && \
  apk add --no-cache bash && \
  apk add --no-cache curl && \
  curl -fsL http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /usr/local && \
  ln -s /usr/local/scala-$SCALA_VERSION/bin/* /usr/local/bin/ && \
  scala -version && \
  scalac -version

RUN \
  curl -fsL https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz | tar xfz - -C /usr/local && \
  $(mv /usr/local/sbt-launcher-packaging-$SBT_VERSION /usr/local/sbt || true) \
  ln -s /usr/local/sbt/bin/* /usr/local/bin/

RUN apk update && apk add --no-cache libc6-compat

RUN sbt assembly

ENTRYPOINT java -jar target/scala-2.12/UCU-2018-func-stream-final-project-stream-application-assembly-0.1.jar