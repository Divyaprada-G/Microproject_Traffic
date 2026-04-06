FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -pl common-models,replication-manager -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/replication-manager/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
