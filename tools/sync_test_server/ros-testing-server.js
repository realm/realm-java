#!/usr/bin/env nodejs

var winston = require('winston'); //logging
const temp = require('temp');
const spawn = require('child_process').spawn;
const exec = require('child_process').exec;
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
        //Dispatch
        dispatcher.dispatch(request, response);
    } catch(err) {
        console.log(err);
    }
}

var syncServerChildProcess = null;

// Waits for ROS to be fully initialized.
function waitForRosToInitialize(attempts, onSuccess, onError) {
    if (attempts == 0) {
        onError("Could not get ROS to start. See Docker log.");
        return;
    }
    http.get("http://0.0.0.0:9080/health", function(res) {
        if (res.statusCode != 200) {
            winston.info("ROS /health/ returned: " + res.statusCode)
            waitForRosToInitialize(attempts - 1,onSuccess)
        } else {
            onSuccess();
        }
    }).on('error', function(err) {
        // ROS not accepting any connections yet.
        // Errors like ECONNREFUSED 0.0.0.0:9080 will be reported here.
        // Wait a little before trying again (common startup is ~1 second).
        setTimeout(function() {
            waitForRosToInitialize(attempts - 1, onSuccess);
        }, 200);
    });
}

function startRealmObjectServer(onSuccess, onError) {
    temp.mkdir('ros', function(err, path) {
        if (!err) {
            winston.info("Starting sync server in ", path);
            var env = Object.create( process.env );
            winston.info(env.NODE_ENV);
            env.NODE_ENV = 'development';
            syncServerChildProcess = spawn('ros',
                    ['start',
                        '--data', path,
                        '--access-token-ttl', '20' //WARNING : Changing this value may impact the timeout of the refresh token test (AuthTests#preemptiveTokenRefresh)
                    ],
                    { env: env, cwd: path});

            // local config:
            syncServerChildProcess.stdout.on('data', (data) => {
                winston.info(`stdout: ${data}`);
            });

            syncServerChildProcess.stderr.on('data', (data) => {
                winston.info(`stderr: ${data}`);
            });

            waitForRosToInitialize(20, onSuccess, onError);
        }
    });
}

function stopRealmObjectServer(onSuccess, onError) {
    if(syncServerChildProcess == null) {
        onError("No ROS process found to stop");
    }

    syncServerChildProcess.on('exit', function(code) {
        winston.info("ROS server stopped due to process being killed. Exit code: " + code);
        syncServerChildProcess.removeAllListeners('exit');
        syncServerChildProcess = null;
        onSuccess();
    });

    syncServerChildProcess.kill();
}

// start sync server
dispatcher.onGet("/start", function(req, res) {
    winston.info("Attempting to start ROS");
    startRealmObjectServer(() => {
        res.writeHead(200, {'Content-Type': 'text/plain'});
        res.end('ROS server started');
    }, function (err) {
        res.writeHead(500, {'Content-Type': 'text/plain'});
        res.end('Starting a ROS server failed: ' + err);
    });
});

// stop a previously started sync server
dispatcher.onGet("/stop", function(req, res) {
  winston.info("Attempting to stop ROS")
  stopRealmObjectServer(function() {
      res.writeHead(200, {'Content-Type': 'text/plain'});
      res.end('ROS server stopped');
  });
});

//Create and start the Http server
var server = http.createServer(handleRequest);
server.listen(PORT, function() {
    winston.info("Integration test server listening on: 127.0.0.1:%s", PORT);
});
