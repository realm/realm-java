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
const temp = require('temp');
const spawn = require('child_process').spawn;
const exec = require('child_process').exec;
const execSync = require('child_process').execSync;
const isPortAvailable = require('is-port-available');
var http = require('http');
var dispatcher = require('httpdispatcher');
var fs = require('fs-extra');
var moment = require('moment')

// Automatically track and cleanup files at exit
temp.track();

if (process. argv. length <= 2) {
    console.log("Usage: " + __filename + " somefile.log");
    process.exit(-1);
}

const logFile = process.argv[2];
winston.level = 'debug';
winston.add(winston.transports.File, {
    filename: logFile,
    json: false,
    formatter: function(options) {
        return moment().format('YYYY-MM-DD HH:mm:ss.SSSS') + ' ' + (undefined !== options.message ? options.message : '');
    }
});

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
function waitForRosToInitialize(attempts, onSuccess, onError, startSequence) {
    if (attempts == 0) {
        onError("Could not get ROS to start. See Docker log.");
        return;
    }

    http.get("http://0.0.0.0:9080/health", function(res) {
        if (res.statusCode != 200) {
            winston.warn("command-server: ROS /health/ returned: " + res.statusCode)
            setTimeout(function() {
                waitForRosToInitialize(attempts - 1, onSuccess, onError, startSequence);
            }, 500);
        } else {
            onSuccess(startSequence);
        }
    }).on('error', function(err) {
        winston.warn("command-server: ROS /health/ returned an error: " + err)
        // ROS not accepting any connections yet.
        // Errors like ECONNREFUSED 0.0.0.0:9080 will be reported here.
        // Wait a little before trying again (common startup is ~1 second).
        setTimeout(function() {
            waitForRosToInitialize(attempts - 1, onSuccess, onError, startSequence);
        }, 500);
    });
}

function startRealmObjectServer(onSuccess, onError) {
    stopRealmObjectServer(() => {
        waitForPortToBeReady(20, function() {
                doStartRealmObjectServer(onSuccess, onError)
        }, onError);
    }, onError)
}

// When starting a new server, the old one might still be in the process of being
// torn down. This can sometimes cause the new server to fail to start due to the
// port still being used. To prevent that, we wait for the port to be ready
// before trying to start the server.
function waitForPortToBeReady(attempts, onSuccess, onError) {
    if (attempts == 0) {
        onError("Port failed to become ready in time");
        return;
    }
    // Port 9080 and 9443 are being used by ROS
    isPortAvailable("9443").then( status => {
        if (status) {
            onSuccess();
        } else {
            winston.info("command-server: Port still in use. Retrying.")
            setTimeout(function() {
                waitForPortToBeReady(attempts - 1, onSuccess, onError);
            }, 500);
        }
    });
}

function doStartRealmObjectServer(onSuccess, onError) {
    temp.mkdir('ros', function(err, path) {
        if (!err) {
            winston.info("command-server: Starting sync server in ", path);
            var env = Object.create( process.env );
            winston.info(env.NODE_ENV);
            env.NODE_ENV = 'development';

            // Cleanup any previous server state
            winston.info("command-server: Cleaning old server state");
            fs.removeSync('/ros/data');
            fs.removeSync('/ros/realm-object-server');
            fs.removeSync('/ros/log.txt');
            if (fs.existsSync('/ros/data')) {
                onError("Could not delete data directory: " + globalNotifierDir);
                return;
            }
            if (fs.existsSync('/ros/realm-object-server')) {
                onError("Could not delete global notifier directory: " + globalNotifierDir);
                return;
            }

            // Start ROS
            syncServerChildProcess = spawn('npm', ['start'], { env: env, cwd: '/ros' });

            // local config:
            syncServerChildProcess.stdout.on('data', (data) => {
                winston.info(`ros: ${data}`);
            });

            syncServerChildProcess.stderr.on('data', (data) => {
                winston.info(`ros: ${data}`);
            });

            // The interval between every health check is 0.5 second. Give the ROS 30 seconds to get fully initialized.
            waitForRosToInitialize(60, onSuccess, onError, Date.now());
        }
    });
}

function stopRealmObjectServer(onSuccess, onError) {
    if(syncServerChildProcess == null || syncServerChildProcess.killed) {
        onSuccess("No ROS process found or the process has been killed before");
    }
    if (syncServerChildProcess) {
        syncServerChildProcess.on('exit', function(code) {
            // Manually kill sub process started by node.
            // It is not killed when killing the process running NPM
            exec('pkill -f "node dist/index.js"', (error, stdout, stderr) => {
                if (error.code) {
                    onError(error)
                    return;
                }
                syncServerChildProcess.removeAllListeners('exit');
                syncServerChildProcess = null;
                onSuccess();
            });
        });
        syncServerChildProcess.kill('SIGINT');
    }
}

// start sync server
dispatcher.onGet("/start", function(req, res) {
     winston.info("command-server: Attempting to start ROS");
     startRealmObjectServer((startSequence) => {
         res.writeHead(200, {'Content-Type': 'text/plain'});
         let response = `ROS started after ${Date.now() - startSequence} ms`;
         winston.info("command-server: " + response);
         res.end(response);
     }, function (err) {
         winston.error('command-server: Starting ROS failed: ' + err);
         res.writeHead(500, {'Content-Type': 'text/plain'});
         res.end('Starting ROS failed: ' + err);
     });
});

// stop a previously started sync server
dispatcher.onGet("/stop", function(req, res) {
      winston.info("command-server: Attempting to stop ROS");
      stopRealmObjectServer(function() {
            winston.info("command-server: ROS stopped");
            res.writeHead(200, {'Content-Type': 'text/plain'});
            res.end('ROS stopped');
      }, function(err) {
            winston.error('command-server: Stopping ROS failed: ' + err);
            res.writeHead(500, {'Content-Type': 'text/plain'});
            res.end('Stopping ROS failed: ' + err);
      });
});

//Create and start the Http server
var server = http.createServer(handleRequest);
server.listen(PORT, function() {
    winston.info("command-server: Integration test server listening on: 127.0.0.1:%s", PORT);
});
