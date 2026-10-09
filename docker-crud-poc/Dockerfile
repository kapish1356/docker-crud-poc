# =====================================================================
# Multi-stage Dockerfile
#   Stage 1 (build)   : Maven + JDK 21  -> compiles code, creates app.jar
#   Stage 2 (runtime) : only JRE 21     -> small final image
# =====================================================================

# ---------- Stage 1: BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom.xml first so Docker caches downloaded dependencies
# (dependencies re-download only when pom.xml changes)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now copy source code and build the jar (tests skipped for faster image build)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: RUNTIME ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user (security best practice)
RUN groupadd --system spring && useradd --system --gid spring spring \
    && mkdir -p /app/data && chown -R spring:spring /app
USER spring:spring

# Copy only the jar from the build stage
COPY --from=build /build/target/app.jar app.jar

# H2 database files are written here -> mount a volume to keep data
VOLUME /app/data

EXPOSE 8080

# Docker will mark container "healthy" only if this endpoint responds
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -fs http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
