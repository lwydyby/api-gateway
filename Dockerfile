FROM openjdk:8-jdk-alpine

WORKDIR /
RUN apk update \
    && apk add curl \
    && apk add tzdata


ADD build/libs/loops-all.jar loops.jar
ENV TZ Asia/Shanghai

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:9100/ || exit 1

EXPOSE 9000
EXPOSE 9100
EXPOSE 9001

ENV JAVA_OPTS="\
-XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap"


CMD java ${JAVA_OPTS} -jar loops.jar

