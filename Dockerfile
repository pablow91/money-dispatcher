FROM openjdk:9-slim
VOLUME /tmp
ADD build/libs/money-dispatcher-0.0.1-SNAPSHOT.jar app.jar
RUN touch /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]