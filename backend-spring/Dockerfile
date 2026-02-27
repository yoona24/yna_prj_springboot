FROM --platform=linux/amd64 gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY . .

RUN gradle build -x test --no-daemon

FROM --platform=linux/amd64 eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
