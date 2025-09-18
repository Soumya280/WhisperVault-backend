# Use Maven + JDK image to build
FROM maven:3.9.3-openjdk-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use lightweight JDK image to run
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=build /app/target/WhisperVault-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
