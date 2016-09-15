FROM frolvlad/alpine-oraclejdk8:slim

MAINTAINER Andrei Varabyeu <andrei_varabyeu@epam.com>

VOLUME /tmp
ADD gateway-service-${version}.jar app.jar
RUN sh -c 'touch /app.jar'
EXPOSE 8080
EXPOSE 8443
ENTRYPOINT ["java","-Xmx512m","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
