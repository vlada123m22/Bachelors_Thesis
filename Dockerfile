# Stage 1: Build with Maven + Java 21
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only what is needed for build
COPY pom.xml .
COPY src ./src

# Build the app
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy over the built JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
