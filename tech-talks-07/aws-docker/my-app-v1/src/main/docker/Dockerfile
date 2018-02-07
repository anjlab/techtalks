FROM tomcat:8.5.11
MAINTAINER dmitrygusev

# Remove default ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/*

# Redis client libraries
ADD tomcat-libs/*.jar /usr/local/tomcat/lib/

ADD localhost-rsa.jks /usr/local/tomcat/conf/localhost-rsa.jks
ADD tomcat-8.5-context.xml /usr/local/tomcat/conf/context.xml
ADD tomcat-8.5-server.xml /usr/local/tomcat/conf/server.xml

EXPOSE 8443
EXPOSE 8080

VOLUME /opt/example

ADD war-exploded-changes-rarely /usr/local/tomcat/webapps/ROOT/
ADD war-exploded-changes-often  /usr/local/tomcat/webapps/ROOT/

CMD ["catalina.sh", "run"]
