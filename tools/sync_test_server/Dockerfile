FROM ubuntu:16.04

# Add realm repo
RUN apt-get update -qq \
    && apt-get install -y curl npm \
    && curl -s https://packagecloud.io/install/repositories/realm/realm/script.deb.sh | bash \
    && npm install winston temp httpdispatcher
COPY keys/private.pem keys/public.pem configuration.yml /
COPY ros-testing-server /usr/bin/
# Install realm object server
RUN apt-get install -y realm-object-server-de=1.0.0-beta-18.0-203
