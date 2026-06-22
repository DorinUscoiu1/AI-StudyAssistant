# Etapa 1: Build-ul aplicatiei
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiem fisierul pom.xml si descarcam dependentele
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiem sursele si compilam proiectul
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Crearea imaginii pentru rulare
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiem JAR-ul rezultat din etapa de build
COPY --from=build /app/target/*.jar app.jar

# Expunem portul aplicatiei (asa cum e in application.properties)
EXPOSE 9090

# Punctul de intrare pentru container
ENTRYPOINT ["java", "-jar", "app.jar"]
