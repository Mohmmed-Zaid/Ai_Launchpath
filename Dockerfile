# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S launchpath && adduser -S launchpath -G launchpath

# Copy jar from builder stage
COPY --from=builder /app/target/ai-service-0.0.1-SNAPSHOT.jar app.jar

# Own the file
RUN chown launchpath:launchpath app.jar

USER launchpath

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]