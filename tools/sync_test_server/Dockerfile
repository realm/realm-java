FROM ubuntu:16.04

ARG ROS_DE_VERSION

# Install all normal dependencies
RUN apt-get update -qq \
    && apt-get install -y curl

# Install node using nodesource to be able to install desired major version
# Not doing this will cause apt-get to install from 4.*
RUN curl -sL https://deb.nodesource.com/setup_6.x | bash - \
    && apt-get install -y nodejs build-essential

# ROS npm dependencies
RUN npm init -y
RUN npm install winston temp httpdispatcher@1.0.0

# Install realm object server
RUN npm install realm-object-server@$ROS_DE_VERSION

COPY keys/public.pem keys/private.pem keys/127_0_0_1-server.key.pem keys/127_0_0_1-chain.crt.pem configuration.yml /
COPY ros-testing-server.js /usr/bin/

CMD /usr/bin/ros-testing-server.js /tmp/ros-testing-server.log
