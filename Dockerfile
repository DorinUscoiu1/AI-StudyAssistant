# Etapa 1: Build-ul aplicatiei
FROM public.ecr.aws/docker/library/maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiem fisierul pom.xml si descarcam dependentele
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiem sursele si compilam proiectul
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Crearea imaginii pentru rulare
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2-jre
WORKDIR /app

# Copiem JAR-ul rezultat din etapa de build
COPY --from=build /app/target/*.jar app.jar

# Expunem portul aplicatiei
EXPOSE 9090

# Punctul de intrare pentru container
ENTRYPOINT ["java", "-jar", "app.jar"]