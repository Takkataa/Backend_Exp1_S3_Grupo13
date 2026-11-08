# ---------- Etapa 1: compilar con Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Primero solo el pom: Docker reutiliza esta capa (cache) si las dependencias no cambian
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Las pruebas se omiten aqui porque necesitan una base de datos
RUN mvn -B -q -DskipTests package

# ---------- Etapa 2: imagen final liviana (solo JRE) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
# Se ejecuta con un usuario sin privilegios (buena practica de seguridad)
RUN useradd --system --create-home appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
