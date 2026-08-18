# ---- Etapa 1: compilar el proyecto con Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar el cache de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos el resto del codigo y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Etapa 2: imagen final, mas liviana, solo con el .jar ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Railway inyecta la variable PORT; la app ya esta configurada para leerla
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
