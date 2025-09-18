# Build stage - try to find a Maven image with Java 24
FROM maven:3.9.6-eclipse-temurin-24 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/target/WhisperVault-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]