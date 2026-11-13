FROM eclipse-temurin:21-jre
COPY build/libs/ai-memory.jar /app/ai-memory.jar
WORKDIR /app/
ENTRYPOINT ["java", "-jar", "/app/ai-memory.jar"]
