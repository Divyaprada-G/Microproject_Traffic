FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY user-service/target/*.jar app.jar
EXPOSE 8084
CMD ["java", "-jar", "app.jar"]
