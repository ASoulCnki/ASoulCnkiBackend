FROM openjdk:8-slim

ENV JAVA_OPTS="-Xms1024M -Xmx1500M -XX:+UseG1GC -Xloggc:logs/gc.log"

ENV PROFILES="demo"

RUN mkdir -p /opt/${PRO_NAME}

ADD api-latest.jar /opt/api.jar

WORKDIR /opt

RUN bash -c 'touch /opt/app.jar'

ENTRYPOINT ["/bin/sh","-c", "java ${JAVA_OPTS} -jar /opt/api.jar --spring.profiles.active=${PROFILES}"]