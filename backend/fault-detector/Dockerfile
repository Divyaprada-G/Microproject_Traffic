FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -pl common-models,fault-detector -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/fault-detector/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
