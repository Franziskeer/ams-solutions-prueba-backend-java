FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package && mv target/*.jar app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar
ENV MOCKS_BASE_URL=http://host.docker.internal:3001
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]
