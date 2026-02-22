# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY checkstyle.xml .
COPY config ./config
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image d'exécution légère, sécurisée (non-root)
FROM eclipse-temurin:21-jre

# Créer un utilisateur non-root
RUN useradd -ms /bin/bash springuser

# Répertoire de travail
WORKDIR /app

# Copier le jar et donner les droits à l'utilisateur
COPY --from=build /app/target/*.jar app.jar
RUN chown springuser:springuser /app/app.jar

# Bascule vers l'utilisateur non-root
USER springuser

# HEALTHCHECK
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]