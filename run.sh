#!/usr/bin/env bash
NAME=${NAME:-pttg-ip-hmrc-access-code}

JAR=$(find . -name ${NAME}*.jar|head -1)
java ${JAVA_OPTS} -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}" >${LOGFILE:=/dev/stdout}
