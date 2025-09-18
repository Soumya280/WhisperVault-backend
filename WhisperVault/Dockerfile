# Use OpenJDK 24
FROM openjdk:24-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR
COPY target/WhisperVault-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
