FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml backend/pom.xml
RUN mvn -B -ntp -f backend/pom.xml dependency:go-offline
COPY backend/src backend/src
COPY frontend frontend
RUN mvn -B -ntp -f backend/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system sinnar && useradd --system --gid sinnar sinnar && mkdir /app/uploads && chown sinnar:sinnar /app/uploads
COPY --from=build /app/backend/target/sinnar-mobile-1.0.0.jar /app/app.jar
USER sinnar
ENV UPLOAD_DIR=/app/uploads
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
