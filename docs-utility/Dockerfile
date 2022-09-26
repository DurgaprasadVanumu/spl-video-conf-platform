FROM alpine
RUN apk update && \
    apk upgrade 
RUN apk add openjdk8=8.345.01-r0

VOLUME /tmp
COPY ./target/*.jar app.jar
EXPOSE 8080
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","app.jar"]