# First stage: build project
FROM maven:3.8.2-openjdk-17-slim AS MAVEN_BUILD

WORKDIR /app

# Copy the pom and src code to the container
COPY . ./

# Package our application code and generate executable jar file
RUN mvn clean package -DskipTests

# Second stage: get the app up and running
FROM openjdk:17-jdk

WORKDIR /app

# Copy only the artifacts we need from the first stage and discard the rest
COPY --from=MAVEN_BUILD /app/target/custom-shop-api-1.0-SNAPSHOT.jar custom-shop-api.jar

# Set the startup command to execute the jar
ENTRYPOINT ["java", "-jar", "/custom-shop-api.jar"]