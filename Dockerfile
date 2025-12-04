FROM eclipse-temurin:21-jdk
EXPOSE 8080
COPY backend/target/banking-app.jar banking-app.jar
ENTRYPOINT ["java", "-jar", "banking-app.jar"]