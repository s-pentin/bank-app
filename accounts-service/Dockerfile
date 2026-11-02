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
    ./gradlew :accounts-service:dependencies --no-daemon > /dev/null 2>&1 || true

COPY . .

RUN --mount=type=cache,target=/root/.gradle/caches/modules-2 \
    --mount=type=cache,target=/root/.gradle/wrapper/dists \
    ./gradlew :accounts-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system appuser && useradd --system --gid appuser appuser
COPY --from=builder /app/accounts-service/build/libs/*.jar app.jar
USER appuser
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]