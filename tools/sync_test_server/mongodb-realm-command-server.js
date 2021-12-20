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
var http = require('http');
const fs = require('fs')
const jwt = require('jsonwebtoken')

const isPortAvailable = require('is-port-available');

function handleUnknownEndPoint(req, resp) {
    resp.writeHead(404, {'Content-Type': 'text/plain'});
    resp.end();
}

function handleOkHttp(req, resp) {
    var emitSuccess = req.url.endsWith("?success=true");
    if (emitSuccess) {
        resp.writeHead(200, {'Content-Type': 'text/plain'});
        resp.end(req.method + "-success");
    } else {
        resp.writeHead(500, {'Content-Type': 'text/plain'});
        resp.end(req.method + "-failure");
    }
}

function handleWatcher(req, resp) {
    resp.writeHead(200, {'Content-Type': 'text/event-stream'});

    resp.write("hello world 1\n");
    resp.write("hello world 2\n");
    resp.write("hello world 3\n");
}

function handleApplicationId(appName, req, resp) {
    switch(req.method) {
        case "GET":
            try {
                 const data = fs.readFileSync('/apps/' + appName + '/app_id', 'utf8')
                 console.log(data)
                 resp.writeHead(200, {'Content-Type': 'text/plain'});
                 resp.end(data.replace(/\n$/, ''));
            } catch (err) {
                 console.error(err)
                 resp.writeHead(404, {'Content-Type': 'text/plain'});
                 resp.end(err);
            }
            break;
        case "PUT":
            var body = [];
            req.on('data', (chunk) => {
                body.push(chunk);
            }).on('end', () => {
                body = Buffer.concat(body).toString();
                applicationIds[appName] = body.split("=")[1];
                resp.writeHead(201, {'Content-Location': '/application-id'});
                resp.end();
            });
            break;
        default:
            handleUnknownEndPoint(req, resp);
    }
}

function getRandomInt(max) {
    return Math.floor(Math.random() * max);
}

function handleGenerateJWT(appName, req, resp) {
    let MINUTE = 60000;

    const appId = fs.readFileSync('/apps/' + appName + '/app_id', 'utf8').replace(/\n$/, '');

    let token = jwt.sign(
        {
            "aud": appId,
            "exp": Math.floor((Date.now() + MINUTE)/1000),
            "sub": getRandomInt(100000),
            "user_data": {
                "name": "Jean Valjean",
                "aliases": [
                    "Monsieur Madeleine",
                    "Ultime Fauchelevent",
                    "Urbain Fabre"
                ]
            }
        }, '1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF');

    resp.writeHead(200, {'Content-Type': 'text/plain'});
    resp.end(token);
}

//Create and start the Http server
const PORT = 8888;
var applicationIds = {}  // Should be updated by the Docker setup script before any tests are run.
var server = http.createServer(function(req, resp) {
    try {
        winston.info('command-server: ' + req.method + " " + req.url);
        if (req.url.includes("/okhttp")) {
            handleOkHttp(req, resp);
        } else if (req.url.includes('/testapp1')) {
            handleApplicationId('testapp1', req, resp);
        } else if (req.url.includes('/testapp2')) {
            handleApplicationId('testapp2', req, resp);
        } else if (req.url.includes('/watcher')) {
            handleWatcher(req, resp);
        } else if (req.url.includes('/jwt')) {
            handleGenerateJWT('testapp1', req, resp);
        } else {
            handleUnknownEndPoint(req, resp);
        }
    } catch(err) {
        winston.error('command-server: ' + err);
    }
});
server.listen(PORT, function() {
    winston.info("command-server: MongoDB Realm Integration Test Server listening on: 127.0.0.1:%s", PORT);
});
