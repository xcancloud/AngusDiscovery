FROM openjdk:17.0.2-jre

MAINTAINER http://www.xcan.cloud

WORKDIR /

RUN mkdir logs && touch logs/gc.log

ARG JAR_FILE
ADD ${JAR_FILE} ${JAR_FILE}
ENV JAR_FILE=$JAR_FILE

COPY startenv.sh /startenv.sh
RUN chmod +x /startenv.sh
RUN sh /startenv.sh

EXPOSE 1801

ENTRYPOINT java $JAVA_OPTS -jar $JAR_FILE
