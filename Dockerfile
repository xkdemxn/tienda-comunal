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

# Flags para achicar la memoria reservada (Railway cobra por GB-minuto, sin
# esto la JVM asume que puede usar hasta el limite del contenedor):
#   MaxRAMPercentage: el heap solo puede crecer hasta el 65% de la memoria
#     que Railway le asigne al contenedor (deja margen para stacks de
#     threads, metaspace, etc. que no son parte del heap).
#   UseSerialGC: para una app de una sola tienda con poco trafico, el
#     recolector G1 (el que usa por defecto) reserva mas memoria de la que
#     realmente hace falta; el recolector serial es mas chico y mas simple,
#     el pequeno costo en velocidad de recoleccion no se nota a esta escala.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65.0", "-XX:+UseSerialGC", "-jar", "app.jar"]
