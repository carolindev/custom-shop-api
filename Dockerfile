# First stage: build project
FROM maven:3.8.2-openjdk-17-slim AS maven_build

WORKDIR /app

# Debug log - Show working directory
RUN echo "Current working directory: $(pwd)"

# Copy project files
COPY . ./

# Debug log - Check if POM and source files are copied
RUN echo "Contents of /app after COPY:" && ls -l /app

# Build the JAR file
RUN echo "Building the project..." && mvn clean package -DskipTests

# Debug log - Check if JAR was created
RUN echo "Checking target folder after build:" && ls -l /app/target/

# Second stage: run the app
FROM openjdk:17-jdk

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=maven_build /app/target/custom-shop-api-1.0-SNAPSHOT.jar /app/custom-shop-api.jar

# Debug log - Check if JAR was copied successfully
RUN echo "Checking contents of /app after JAR copy:" && ls -l /app/

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "/app/custom-shop-api.jar"]