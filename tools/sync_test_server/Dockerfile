FROM ubuntu:16.04

ARG ROS_DE_VERSION

# Add realm repo
RUN apt-get update -qq \
    && apt-get install -y curl npm \
    && curl -s https://packagecloud.io/install/repositories/realm/realm/script.deb.sh | bash

# ROS npm dependencies
RUN npm init -y
RUN npm install winston temp httpdispatcher@1.0.0

COPY keys/private.pem keys/public.pem configuration.yml /
COPY ros-testing-server.js /usr/bin/
# Install realm object server
RUN apt-get update -qq \
    && apt-get install -y realm-object-server-developer=$ROS_DE_VERSION \
    && apt-get clean

CMD /usr/bin/ros-testing-server.js /tmp/ros-testing-server.log
