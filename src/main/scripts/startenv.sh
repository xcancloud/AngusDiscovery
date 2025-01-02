#!/bin/sh

IP=$1
DISCOVERY_HOST=$1
echo "Deploy host: ${DISCOVERY_HOST}"

set_env(){
  if [ "$IP" = "bj-c1-prod-serv2.xcan.cloud" ]; then
    DISCOVERY_HOST="bj-c1-prod-serv1.xcan.cloud"
  elif [ "$IP" = "bj-c1-prod-serv1.xcan.cloud" ]; then
    DISCOVERY_HOST="bj-c1-prod-serv2.xcan.cloud"
  fi
  export DISCOVERY_HOST
  echo "Set application environment variables: ${DISCOVERY_HOST}"
}
set_env

. /etc/profile

export JAVA_OPTS="-Xnoagent -Dlogs=logs -Dcache=cache -Xloggc:logs/gc.log"
export JAVA_OPTS="$JAVA_OPTS -server -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=45 -Djava.security.egd=file:/dev/./urandom"
export JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=2801"