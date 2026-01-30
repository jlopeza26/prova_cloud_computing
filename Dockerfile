FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiem Maven Wrapper i pom
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Donem permisos a mvnw
RUN chmod +x mvnw

# Baixem dependències offline
RUN ./mvnw dependency:go-offline

# Copiem codi
COPY src ./src

# Compilem el projecte sense tests
RUN ./mvnw clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Start command
CMD ["java", "-jar", "target/cloudapi-0.0.1-SNAPSHOT.jar"]
