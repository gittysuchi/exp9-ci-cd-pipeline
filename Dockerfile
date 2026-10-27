# ---------- Stage 1: build the JAR ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
# Tests already ran in the CI pipeline, so the image build skips them
RUN mvn -B -ntp -DskipTests package

# ---------- Stage 2: small runtime image ----------
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

# Release metadata - overridden at deploy time (stable / canary)
ARG APP_VERSION=1.0.0
ENV APP_VERSION=${APP_VERSION} \
    APP_TRACK=stable \
    JAVA_OPTS=""

USER app
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=6 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q UP || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
