FROM bellsoft/liberica-openjdk-debian:21.0.1-12
ARG WAR_FILE=build/libs/List_randomizing_telegram_bot-1.0.0.war
ENV DB_USERNAME=example
ENV DB_PASSWORD=example
ENV DB_NAME=example
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV APP_PORT=8080
COPY ${WAR_FILE} app.war
ENTRYPOINT ["java", "-Dspring.datasource.password=${DB_PASSWORD}", "-Dspring.datasource.username=${DB_USERNAME}", "-Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}", "-Dserver.port=${APP_PORT}", "-jar", "app.war"]