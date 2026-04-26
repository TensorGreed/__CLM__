FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar /app/clm-backend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/clm-backend.jar"]
