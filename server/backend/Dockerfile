# Use Maven to build the application
FROM maven:3.8.6-openjdk-11 AS builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy the entire project source
COPY src ./src

# Package the application
RUN mvn package -DskipTests

# Use a lightweight OpenJDK runtime for the final image
FROM openjdk:11-jre-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/employee-management-app-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Commands for building and running the Docker container:
#
# cd IdeaProjects/Employee-Management/backend
# (Fix the path to your project directory)
#
# This builds the Docker image ready for Render deployment.
#
# docker run -p 8080:8080 \
#  -e PGSQL_HOST=${PGSQL_HOST} \
#  -e PGSQL_PORT=${PGSQL_PORT} \
#  -e PGSQL_DB=${PGSQL_DB} \
#  -e PGSQL_USER=${PGSQL_USER} \
#  -e PGSQL_PASSWORD=${PGSQL_PASSWORD} \
# This runs the Docker container locally. Replace the environment variables with your own PostgreSQL database details.
#
# This pushes the Docker image to Docker Hub.
