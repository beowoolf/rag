# Etap 1: Budowanie aplikacji
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Etap 2: Budowanie obrazu
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.war app.war

# Uruchom aplikację Spring Boot
CMD ["java", "-jar", "app.war"]
