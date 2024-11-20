#!/bin/sh
#export EUREKA_USER=someUserName
#export EUREKA_PASS=W872INBJNWZcoDN
#export EUREKA_URI=3.136.74.174:8090
export EUREKA_USER=admin
export EUREKA_PASS=admin
export EUREKA_URI=127.0.0.1:8090
export RMQ_ADDRESS="amqps://b-0e720b16-3ea7-4227-ad65-6cce3704121c.mq.us-east-2.amazonaws.com:5671"
export RMQ_USER="jacobtestuser"
export RMQ_PWD="Jacob123"
export ODS_GDRIVE_CLIENT_ID="848658234742-82sesqm2jr9l85f6c1mrq7p31mr1cjd1.apps.googleusercontent.com"
export ODS_GDRIVE_CLIENT_SECRET="aMxUivAtLW42_uGkpFuD1VCD"

mvn clean package -DskipTests
java -Dspring.profiles.active=dev -jar target/rabbitmq-scheduler-0.0.1-SNAPSHOT.jar
