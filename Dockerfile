FROM openjdk:11
EXPOSE 8080
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN javac MQClient.java
CMD ["java", "MQClient"]
