FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY settings.gradle.kts build.gradle.kts ./
COPY accounts-service/build.gradle.kts accounts-service/
COPY cash-service/build.gradle.kts cash-service/
COPY transfer-service/build.gradle.kts transfer-service/
COPY notifications-service/build.gradle.kts notifications-service/
COPY gateway/build.gradle.kts gateway/
COPY front/build.gradle.kts front/

RUN --mount=type=cache,target=/root/.gradle/caches/modules-2 \
    --mount=type=cache,target=/root/.gradle/wrapper/dists \
    ./gradlew :cash-service:dependencies --no-daemon > /dev/null 2>&1 || true

COPY . .

RUN --mount=type=cache,target=/root/.gradle/caches/modules-2 \
    --mount=type=cache,target=/root/.gradle/wrapper/dists \
    ./gradlew :cash-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/cash-service/build/libs/*.jar app.jar
EXPOSE 8082
CMD ["java", "-jar", "app.jar"]