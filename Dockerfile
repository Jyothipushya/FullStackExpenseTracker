FROM maven:3.9.6-eclipse-temurin-11 AS build

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /app/target/budgetwise-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]