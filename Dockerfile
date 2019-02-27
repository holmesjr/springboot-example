FROM openjdk:8-jdk-alpine
VOLUME /tmp

COPY ./target/SpringTest-1.0-SNAPSHOT.jar app.jar
COPY ./run.sh .
ENTRYPOINT ["./run.sh"]