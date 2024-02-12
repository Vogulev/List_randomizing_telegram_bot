FROM bellsoft/liberica-openjdk-debian:21.0.1-12
ARG WAR_FILE=target/*.war
COPY ${WAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.war"]