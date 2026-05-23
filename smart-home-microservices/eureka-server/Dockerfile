FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=builder /build/target/*.jar app.jar

RUN mkdir -p /app/logs && chown -R spring:spring /app

USER spring:spring

ENV JAVA_OPTS="-Xms128m -Xmx256m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/ \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=docker"

EXPOSE 8761

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
  CMD java -jar /app/app.jar healthcheck || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]