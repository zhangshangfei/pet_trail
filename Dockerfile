FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY backend/pom.xml .
RUN mvn dependency:go-offline

COPY backend/src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl bash ca-certificates && \
    update-ca-certificates

COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 8080

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/app.jar"]
