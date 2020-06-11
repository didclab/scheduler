FROM openjdk:8
EXPOSE 8080
ADD target/rabbitMq-scheduler.jar rabbitMq-scheduler.jar
ENTRYPOINT["java","-jar","/rabbitMq-scheduler.jar"]