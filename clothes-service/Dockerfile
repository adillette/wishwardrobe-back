FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/clothes-service-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
