FROM node:6.11.4

# set timezone to Copenhagen (by default it's using UTC) to match Android's device time.
RUN cp /usr/share/zoneinfo/Europe/Copenhagen /etc/localtime
RUN echo "Europe/Copenhagen" >  /etc/timezone

ARG ROS_DE_VERSION

# Install netstat (used for debugging)
RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive apt-get install -y \
    net-tools \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*


## Copy ROS node project to image and install and configure it
# FIXME: Figure out how to set DE_VERSION
COPY ros /ros
WORKDIR "/ros"
RUN npm install
WORKDIR "/"

# Install test server dependencies
RUN npm install winston@2.4.0 temp httpdispatcher@1.0.0 fs-extra moment

COPY keys/public.pem keys/private.pem keys/127_0_0_1-server.key.pem keys/127_0_0_1-chain.crt.pem /
COPY integration-test-command-server.js /usr/bin/

#Bypass the ROS license check
ENV DOCKER_DATA_PATH /
ENV ROS_TOS_EMAIL_ADDRESS 'ci@realm.io'

CMD /usr/bin/integration-test-command-server.js /tmp/integration-test-command-server.log
