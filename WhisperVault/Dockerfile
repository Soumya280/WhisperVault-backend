# Use OpenJDK 24
FROM openjdk:24-jdk

# Set working directory inside the container
WORKDIR /app

# Copy the jar file into the container
COPY target/WhisperVault-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot app will run on
EXPOSE 8080

# Run the Spring Boot jar
ENTRYPOINT ["java", "-jar", "app.jar"]