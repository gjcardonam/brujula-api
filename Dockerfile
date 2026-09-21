# Etapa 1: compilar con Maven (Java 21)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Etapa 2: imagen liviana de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S brujula && adduser -S brujula -G brujula && mkdir -p /app/uploads && chown -R brujula:brujula /app
COPY --from=build /app/target/brujula-api-*.jar app.jar
USER brujula
ENV UPLOADS_DIR=/app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:TieredStopAtLevel=1", "-XX:+UseSerialGC", "-Dspring.jmx.enabled=false", "-jar", "app.jar"]
