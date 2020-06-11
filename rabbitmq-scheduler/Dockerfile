FROM openjdk:8

ARG PORT=8083
ARG RABBIT_MQ_PORT=5672
ARG MQ_HOST=localhost

ENV ODS_SCHEDULER_PORT=$PORT
ENV ODS_RABBIT_MQ_PORT=$RABBIT_MQ_PORT
ENV ODS_RABBIT_MQ_HOST=$MQ_HOST

EXPOSE $PORT
ADD target/rabbitMq-scheduler.jar rabbitMq-scheduler.jar
ENTRYPOINT ["java","-jar","/rabbitMq-scheduler.jar"]
