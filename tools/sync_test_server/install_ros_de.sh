#!/bin/sh

apt-get update -qq && \
apt-get install -y curl npm && \
# Setup Realm repo
curl -s https://packagecloud.io/install/repositories/realm/realm/script.deb.sh | bash && \
# Install realm object server
apt-get install -y realm-object-server-de=1.0.0-beta-18.0-203 && \

