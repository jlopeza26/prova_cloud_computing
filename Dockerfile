# ----------- BUILD STAGE -----------
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiar Maven Wrapper i fitxers base
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Donar permisos d'execució
RUN chmod +x mvnw

# Descarregar dependències
RUN ./mvnw dependency:resolve

# Copiar codi font
COPY src src

# Compilar aplicació
RUN ./mvnw clean package -DskipTests


# ----------- RUNTIME STAGE -----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el jar generat
COPY --from=build /app/target/*.jar app.jar

# Port utilitzat per Spring Boot
EXPOSE 8080

# Arrencar aplicació
ENTRYPOINT ["java","-jar","app.jar"]
