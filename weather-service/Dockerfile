FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY build/libs/weather-service-1.0.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
