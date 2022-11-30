FROM node:18

# This Docker image is only responsible for running the Integration Command Server which can be
# used to instrument other parts of the Integration tests.
#
# It exposes a webserver on port 8888.

# set timezone to Copenhagen (by default it's using UTC) to match Android's device time.
RUN cp /usr/share/zoneinfo/Europe/Copenhagen /etc/localtime
RUN echo "Europe/Copenhagen" >  /etc/timezone

# Install netstat (used for debugging)
RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive apt-get install -y \
    net-tools \
    psmisc \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# Copy webserver script and install dependencies
WORKDIR "/tmp"
COPY mongodb-realm-command-server.js /tmp/
RUN npm install winston@2.4.0 temp httpdispatcher@1.0.0 fs-extra moment is-port-available@0.1.5

# Run integration test server
CMD /tmp/mongodb-realm-command-server.js
