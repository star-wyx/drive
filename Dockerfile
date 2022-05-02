#FROM azul/zulu-openjdk-alpine:11.0.3-jre
FROM adoptopenjdk/openjdk11:jre-11.0.14.1_1-ubuntu

#更新源，安装yasm ffmpeg

RUN apt-get update \
    && apt-get install -y curl vim net-tools  \
#    && apt-get install -y mongodb \
    && apt-get install -y ffmpeg

#RUN apk update && \
#    apk add yasm && \
#    apk add ffmpeg
#    echo 'http://dl-cdn.alpinelinux.org/alpine/v3.6/main' >> /etc/apk/repositories && \
#    echo 'http://dl-cdn.alpinelinux.org/alpine/v3.6/community' >> /etc/apk/repositories && \
#    apk update && \
#    apk add mongodb=3.4.4-r0 &&

#RUN echo 'http://dl-cdn.alpinelinux.org/alpine/v3.6/main' >> /etc/apk/repositories
#RUN echo 'http://dl-cdn.alpinelinux.org/alpine/v3.6/community' >> /etc/apk/repositories
#RUN apk update
#RUN apk add mongodb=3.4.4-r0

VOLUME /tmp

COPY ./target/*.jar /app.jar

CMD ["--server.port=9090"]

EXPOSE 9090

ENTRYPOINT ["java","-jar","/app.jar"]
