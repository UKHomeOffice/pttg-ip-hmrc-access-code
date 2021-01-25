FROM quay.io/ukhomeofficedigital/openjdk8:v1.8.0.131


ENV USER user-pttg-ip-hmrc-access
ENV USER_ID 1000
ENV GROUP group-pttg-ip-hmrc-access
ENV NAME pttg-ip-hmrc-access-code

ENV JAR_PATH build/libs

RUN yum update -y glibc && \
    yum update -y nss && \
    yum update -y bind-license

WORKDIR /app

RUN groupadd -r ${GROUP} && \
    useradd -r ${USER} -u ${USER_ID} -g ${GROUP} -d /app && \
    mkdir -p /app && \
    chown -R ${USER}:${GROUP} /app

COPY ${JAR_PATH}/${NAME}*.jar /app
COPY run.sh /app
COPY src/main/resources/rds-combined-ca-bundle.pem /app

RUN chmod a+x /app/run.sh

EXPOSE 8081

USER ${USER_ID}

ENTRYPOINT /app/run.sh
