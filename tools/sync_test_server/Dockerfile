FROM node:6.11.4

# set timezone to Copenhagen (by default it's using UTC) to match Android's device time.
RUN cp /usr/share/zoneinfo/Europe/Copenhagen /etc/localtime
RUN echo "Europe/Copenhagen" >  /etc/timezone

ARG ROS_VERSION
ARG REALM_FEATURE_TOKEN
RUN if [ "x$ROS_VERSION" = "x" ] ; then echo Non-empty ROS_VERSION required ; exit 1; fi
RUN if [ "x$REALM_FEATURE_TOKEN" = "x" ] ; then echo Non-empty REALM_FEATURE_TOKEN required ; exit 1; fi

# Install netstat (used for debugging)
RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive apt-get install -y \
    net-tools \
    psmisc \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

## Copy ROS node template project to image. Then configure and prepare it for usage.
COPY ros /ros
WORKDIR "/ros"
RUN sed -i -e "s/%ROS_VERSION%/$ROS_VERSION/g" package.json
RUN sed -i -e "s/%REALM_FEATURE_TOKEN%/$REALM_FEATURE_TOKEN/g" src/index.ts
RUN npm install
WORKDIR "/"

# Install test server dependencies
RUN npm install winston@2.4.0 temp httpdispatcher@1.0.0 fs-extra moment is-port-available@0.1.5

COPY keys/public.pem keys/private.pem keys/127_0_0_1-server.key.pem keys/127_0_0_1-chain.crt.pem /
COPY integration-test-command-server.js /usr/bin/

# Bypass the ROS license check
ENV DOCKER_DATA_PATH /
ENV ROS_TOS_EMAIL_ADDRESS 'ci@realm.io'

# Run integration test server
CMD /usr/bin/integration-test-command-server.js /tmp/integration-test-command-server.log
