# Production image for multi-reestr-helper (Jmix 3 / Spring Boot).
# Build: docker compose build
# Same image on Mac and Windows. Main DB is PostgreSQL 16 (compose service postgres).
# HSQLDB is not used in this path: MAIN_DATASOURCE_URL must point at Postgres.

# --- build production-jar (same flags as dev/deploy/build-jar.sh) ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src/main ./src/main

# Tests are not part of the image. Vaadin frontend is packed into the jar.
# .git stays out of the context; generateGitProperties writes an empty file if git is missing.
# Optional: --build-arg GIT_BRANCH / GIT_COMMIT / GIT_COMMIT_TIME for «О программе».
ARG GIT_BRANCH=
ARG GIT_COMMIT=
ARG GIT_COMMIT_TIME=
ENV GIT_BRANCH=$GIT_BRANCH \
    GIT_COMMIT=$GIT_COMMIT \
    GIT_COMMIT_TIME=$GIT_COMMIT_TIME
# Spring Boot writes a boot jar and a -plain.jar; only the boot jar is runnable.
RUN ./gradlew --no-daemon bootJar -Pvaadin.productionMode=true -x test \
    && JAR="$(ls build/libs/*.jar | grep -v -- '-plain.jar' | head -n 1)" \
    && test -n "$JAR" \
    && cp "$JAR" /src/app.jar

# --- runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 10001 --home-dir /app --shell /usr/sbin/nologin app \
    && mkdir -p /app/.jmix \
    && chown -R app:app /app

COPY --from=build --chown=app:app /src/app.jar /app/app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Duser.timezone=Europe/Moscow", "-jar", "/app/app.jar"]
