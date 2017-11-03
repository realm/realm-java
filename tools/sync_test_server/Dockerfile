FROM node:6.11.4

ARG ROS_DE_VERSION

# Install realm object server
RUN npm install -g realm-object-server@$ROS_DE_VERSION -S

# Install test server dependencies
RUN npm install winston temp httpdispatcher@1.0.0 fs-extra moment

COPY keys/public.pem keys/private.pem keys/127_0_0_1-server.key.pem keys/127_0_0_1-chain.crt.pem configuration.yml /
COPY ros-testing-server.js /usr/bin/

#Bypass the ROS license check
ENV DOCKER_DATA_PATH /
ENV ROS_TOS_EMAIL_ADDRESS 'ci@realm.io'

CMD /usr/bin/ros-testing-server.js /tmp/ros-testing-server.log
