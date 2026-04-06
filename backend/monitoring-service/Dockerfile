FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY monitoring-service/target/*.jar app.jar
EXPOSE 8086
CMD ["java", "-jar", "app.jar"]
