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
function waitForRosToInitialize(onSuccess) {
    http.get("http://0.0.0.0:9080/health", function(res) {
        if (res.statusCode != 200) {
            winston.info("ROS /health/ returned: " + res.statusCode)
            waitForRosToInitialize(onSuccess)
        } else {
            onSuccess();
        }
    }).on('error', function(err) {
        // ROS not accepting any connections yet.
        // Errors like ECONNREFUSED 0.0.0.0:9080 will be reported here.
        // Wait a little before trying again (common startup is ~1 second).
        setTimeout(function() {
            waitForRosToInitialize(onSuccess);
        }, 200);
    });
}

function startRealmObjectServer(done) {
    // Hack for checking the ROS is fully initialized.
    // Consider the ROS is initialized fully only if log below shows twice
    // "client: Closing Realm file: /tmp/ros117521-7-1eiqt7a/internal_data/permission/__auth.realm"
    // https://github.com/realm/realm-object-server/issues/1297
    var logFindingCounter = 1

    stopRealmObjectServer(function(err) {
        if(err) {
          return;
        }
        temp.mkdir('ros', function(err, path) {
            if (!err) {
                winston.info("Starting sync server in ", path);
                var env = Object.create( process.env );
                winston.info(env.NODE_ENV);
                env.NODE_ENV = 'development';
                syncServerChildProcess = spawn('ros',
                        ['start', '--data', path],
                        { env: env, cwd: path});
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

                waitForRosToInitialize(done);
            }
        });

    });
}

function stopRealmObjectServer(callback) {
    if (syncServerChildProcess) {
        syncServerChildProcess.on('exit', function() {
            syncServerChildProcess = null;
            callback();
        });
        syncServerChildProcess.kill();
    } else {
        callback();
    }
}


// start sync server
dispatcher.onGet("/start", function(req, res) {
    startRealmObjectServer(() => {
        res.writeHead(200, {'Content-Type': 'text/plain'});
        res.end('Starting a server');
    })
});

// stop a previously started sync server
dispatcher.onGet("/stop", function(req, res) {
    stopRealmObjectServer(function() {
      winston.info("Sync server stopped");
      res.writeHead(200, {'Content-Type': 'text/plain'});
      res.end('Stopping the server');
    });
});

//Create and start the Http server
var server = http.createServer(handleRequest);
server.listen(PORT, function() {
    winston.info("Integration test server listening on: 127.0.0.1:%s", PORT);
});
