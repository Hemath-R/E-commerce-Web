# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copy Maven wrapper files
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Copy source code
COPY src ./src

# Build application with Maven, skip tests for faster builds
RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests -q

# Stage 2: Runtime stage (minimal image)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy only the compiled JAR from build stage
COPY --from=builder /build/target/*.jar app.jar

# Create non-root user for security
RUN useradd -m -u 1001 appuser && chown -R appuser:appuser /app
USER appuser

# Expose default port (Render will override with PORT env variable)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher || exit 1

# Run the JAR - Render injects PORT environment variable
ENTRYPOINT ["java", "-jar", "app.jar"]