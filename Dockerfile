# syntax=docker/dockerfile:1

# ---- Build stage: compile and package the executable jar ----
# Pinned to Java 21 to match <java.version> in pom.xml.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy sources and build. The BuildKit cache mount keeps the local Maven repo
# across builds, so dependencies aren't re-downloaded on every image build.
# Tests run in CI (and the JaCoCo gate), so the image build skips them.
COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q clean package -DskipTests \
    && cp target/*.jar app.jar

# ---- Runtime stage: distroless JRE, runs as non-root ----
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=build /build/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
