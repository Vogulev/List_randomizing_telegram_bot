FROM gradle:8.6.0-jdk21-alpine as build
ADD --chown=gradle:gradle . /application/
WORKDIR /application
RUN gradle clean build --no-daemon --stacktrace

FROM bellsoft/liberica-openjdk-debian:21.0.1-12 as prod
WORKDIR /application
ARG WAR_FILE=/application/build/libs/List_randomizing_telegram_bot-1.0.0.war
COPY --from=build ${WAR_FILE} /application/app.war
ENV DB_USERNAME=example
ENV DB_PASSWORD=example
ENV DB_NAME=example
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV APP_PORT=8080
ENV BOT_USERNAME=example
ENV BOT_TOKEN=example
ENV ADMINS=example
ENTRYPOINT ["java", "-Dspring.datasource.password=${DB_PASSWORD}", "-Dspring.datasource.username=${DB_USERNAME}", "-Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}", "-Dserver.port=${APP_PORT}", "-jar", "/application/app.war"]