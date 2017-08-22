#!/usr/bin/env nodejs

var winston = require('winston'); //logging
const temp = require('temp');
const spawn = require('child_process').spawn;
const exec = require('child_process').exec;
var http = require('http');
var dispatcher = require('httpdispatcher');

// this query is used to check if ROS has started
// while waiting for a permanante fix in https://github.com/realm/realm-object-server/issues/1297.
// query should return 200 with the JSON payload Ex: {"version":"1.8.1","flavor":"developer","setupRequired":true}
var options = {
  hostname: '127.0.0.1',
  port: 9080,
  path: '/api/info',
  method: 'GET'
};

function tryUntilROSStart(options, callback) {
    var req = http.request(options, function(res) {
        callback(null, res);
    });
    req.on('error', function(e) {
        tryUntilROSStart(options, callback);
    });
    req.end();
}

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

function startRealmObjectServer(done) {
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
                syncServerChildProcess = spawn('realm-object-server',
                        ['--root', path,
                        '--configuration', '/configuration.yml'],
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

                tryUntilROSStart(options, function(err, resp) {
                    winston.info('>>>>>>>>>>>>>>>>>>> [ROS] server started <<<<<<<<<<<<<<<<<<<');
                    done()
                });
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
        res.end('Server started');
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
