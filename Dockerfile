FROM maven:3.9.16-eclipse-temurin-25 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY src ./src

RUN mvn --batch-mode --no-transfer-progress clean package

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /workspace/target/hotel-reservation-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
