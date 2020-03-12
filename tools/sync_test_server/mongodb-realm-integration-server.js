#!/usr/bin/env nodejs

/**
 * This script controls the Command Server responsible for starting and stopping
 * ROS instances. The integration tests running on the device will communicate
 * with it using a predefined port in order to say when the ROS instance
 * should be started and stopped.
 *
 * This script is responsible for cleaning up any server state after it has been
 * stopped, so a new integration test will start from a clean slate.
 */

var winston = require('winston'); //logging
const isPortAvailable = require('is-port-available');
var http = require('http');

//Create and start the Http server
const PORT = 8888;
var server = http.createServer(function(request, response) {
    try {
        winston.info('command-server: ' + request.method + " " + request.url);
        if (request.url.includes("/okhttp")) {
                var emitSuccess = request.url.endsWith("?success=true");
                if (emitSuccess) {
                    response.writeHead(200, {'Content-Type': 'text/plain'});
                    response.end(request.method + "-success");
                } else {
                    response.writeHead(500, {'Content-Type': 'text/plain'});
                    response.end(request.method + "-failure");
                }
        } else {
            response.writeHead(404, {'Content-Type': 'text/plain'});
            response.end();
        }
    } catch(err) {
        winston.error('command-server: ' + err);
    }
});
server.listen(PORT, function() {
    winston.info("command-server: MongoDB Realm Integration Test Server listening on: 127.0.0.1:%s", PORT);
});
