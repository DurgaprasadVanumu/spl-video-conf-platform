#FROM openjdk:1.8

#COPY target/AI_MatcherService-0.0.1-SNAPSHOT.jar  test.jar

#ENTRYPOINT ["java","-jar","/test.jar"]

#EXPOSE 80

FROM alpine

RUN apk update && \
    apk upgrade 
RUN apk add openjdk8=8.345.01-r0

# FROM java:8
VOLUME /tmp
COPY ./target/*.jar app.jar
EXPOSE 8087
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","app.jar"]
#EXPOSE 8086