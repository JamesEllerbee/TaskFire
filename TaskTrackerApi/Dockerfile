FROM alpine:latest

WORKDIR /workdir

RUN apk add openjdk19

COPY ./app/build/libs/taskfireapi.jar /workdir/jars/
COPY taskfireApi.properties /workdir/