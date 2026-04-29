# ---- Stage 1: build ----
# WHY multi-stage: the Maven image (~600MB) is only needed to compile.
# The final image only contains the compiled JAR + a slim JRE (~200MB).
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app

# Copy POM first and download dependencies before copying source.
# WHY: Docker caches this layer. Subsequent builds skip the slow dependency
# download unless pom.xml actually changes.
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -Dmaven.test.skip=true -q

# ---- Stage 2: run ----
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
