FROM openjdk:9-jre
VOLUME /tmp
ADD build/libs/slack-money-0.0.1-SNAPSHOT.jar app.jar
RUN touch /app.jar
VOLUME /graph
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]