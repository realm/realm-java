#
# Version 1

# Pull from CentOS RPM Build Image
FROM centos:7

ARG ACCESS_TOKEN
RUN yum install -y epel-release && \
    curl -s https://$ACCESS_TOKEN:@packagecloud.io/install/repositories/realm/sync/script.rpm.sh | bash

RUN yum -y install realm-object-server-1.0.0_beta_15.0-151.el7
# Need by our testing server
RUN npm install winston temp httpdispatcher

# Copy testing keys
COPY keys/private.pem keys/public.pem /
COPY ros-testing-server /usr/bin/

