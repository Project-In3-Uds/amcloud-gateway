# Use an OpenJDK base image
FROM openjdk:17-jdk-slim

# Install curl
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
