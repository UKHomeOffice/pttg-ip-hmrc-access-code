#!/usr/bin/env bash

NAME=${NAME:-pttg-ip-hmrc-access-code}
JAR=$(find . -name ${NAME}*.jar | head -1)

if [ -z ${ACP_MODE} ]; then
  echo "EBSA Deployment"
  java ${JAVA_OPTS} -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"
else
  echo "ACP Deployment"

  certfiles=$(awk '/-----BEGIN CERTIFICATE-----/{filename="acpca-a-"NR; print filename}; {print >filename}' /certs/acp-root.crt)

  for file in ${certfiles}; do
    keytool -import -alias "${file}" -file "${file}" -keystore ./truststore.jks -noprompt -storepass changeit -trustcacerts
    rm "${file}"
  done

  certfiles=$(awk '/-----BEGIN CERTIFICATE-----/{filename="acpca-b-"NR; print filename}; {print >filename}' /certs/ca-certificates.crt)

  for file in ${certfiles}; do
    keytool -import -alias "${file}" -file "${file}" -keystore ./truststore.jks -noprompt -storepass changeit -trustcacerts
    rm "${file}"
  done

  java ${JAVA_OPTS} -Djavax.net.ssl.trustStore=/app/truststore.jks \
    -Dcom.sun.management.jmxremote.local.only=false \
    -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"
fi
