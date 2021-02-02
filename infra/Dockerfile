FROM maven:3.6-jdk-11-slim

MAINTAINER Bryan Sin "csin001@e.ntu.edu.sg"

RUN apt update -y

RUN mkdir /root/app
WORKDIR /root/app

ADD . .

CMD sleep 10 && java -jar /root/app/src/target/cogbench-0.0.1-SNAPSHOT.jar
