FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY edge-node-service/target/*.jar app.jar
EXPOSE 8085
CMD ["java", "-jar", "app.jar"]
