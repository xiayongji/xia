FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/smart-home-app.jar app.jar

EXPOSE 8080 8081 8082 8083 8084

ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=docker"

ENTRYPOINT ["java", "-jar", "app.jar"]