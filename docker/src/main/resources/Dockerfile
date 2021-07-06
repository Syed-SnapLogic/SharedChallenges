FROM openjdk:8-jre
MAINTAINER Syed M Ahmed <syed@snaplogic.com>

EXPOSE 8191
EXPOSE 8943
ENTRYPOINT ["/usr/local/openjdk-8/bin/java", "-jar", "/usr/share/snapbox/snapbox.jar"]

RUN install -d /usr/share/snapbox

COPY snapbox-1.0-SNAPSHOT.jar /usr/share/snapbox/snapbox.jar