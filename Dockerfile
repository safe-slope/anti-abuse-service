## Multi-stage Dockerfile for Spring Boot multimodule Maven application
#
## Stage 1: Build stage
#FROM maven:3.9-eclipse-temurin-21 AS builder
#
## Set working directory
#WORKDIR /app
#
## Copy the entire source code
#COPY anti-abuse-microservice ./anit-abuse-microservice
#
## Build the application (skip tests for faster builds)
#RUN cd anti-abuse-microservice && mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
# The Spring Boot Maven plugin creates an executable JAR in the api module
COPY anti-abuse-microservice/api/target/app.jar app.jar

# Expose the application port
EXPOSE 8080

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
