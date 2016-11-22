#!/usr/bin/env nodejs

var winston = require('winston');//logging
const temp = require('temp');
const spawn = require('child_process').spawn;
var http = require('http');
var dispatcher = require('httpdispatcher');

// Automatically track and cleanup files at exit
temp.track();

if (process. argv. length <= 2) {
    console.log("Usage: " + __filename + " somefile.log");
    process.exit(-1);
}
const logFile = process.argv[2];
winston.level = 'debug';
winston.add(winston.transports.File, { filename: logFile });

const PORT = 8888;

function handleRequest(request, response) {
    try {
        //log the request on console
        winston.log(request.url);
        //Disptach
        dispatcher.dispatch(request, response);
    } catch(err) {
        console.log(err);
    }
}

var syncServerChildProcess = null;

function startRealmObjectServer() {
    stopRealmObjectServer();
    temp.mkdir('ros', function(err, path) {
        if (!err) {
            winston.info("Starting sync server in ", path);
            syncServerChildProcess = spawn('realm-object-server',
                    ['--root', path,
                    '--configuration', '/configuration.yml']);
            // local config:
            syncServerChildProcess.stdout.on('data', (data) => {
                winston.info(`stdout: ${data}`);
            });

            syncServerChildProcess.stderr.on('data', (data) => {
                winston.info(`stderr: ${data}`);
            });

            syncServerChildProcess.on('close', (code) => {
                winston.info(`child process exited with code ${code}`);
            });
        }
    });
}

function stopRealmObjectServer() {
    if (syncServerChildProcess) {
        syncServerChildProcess.kill();
        syncServerChildProcess = null;
    }
}


// start sync server
dispatcher.onGet("/start", function(req, res) {
    startRealmObjectServer();
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Starting a server');
});

// stop a previously started sync server
dispatcher.onGet("/stop", function(req, res) {
    stopRealmObjectServer();
    winston.info("Sync server stopped");
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Stopping the server');
});

//Create and start the Http server
var server = http.createServer(handleRequest);
server.listen(PORT, function() {
    winston.info("Integration test server listening on: 127.0.0.1:%s", PORT);
});
