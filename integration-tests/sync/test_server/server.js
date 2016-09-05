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

function handleRequest(request, response){
    try {
        //log the request on console
        winston.log(request.url);
        //Disptach
        dispatcher.dispatch(request, response);
    } catch(err) {
        console.log(err);
    }
}

var syncServerChildProcess;

// start sync server
dispatcher.onGet("/start", function(req, res) {
    temp.mkdir('naruto', function(err, path) {
      if (!err) {
        winston.info("Starting sync server in ", path);
        //TODO get the full path of the build by running
        //     s3cmd -c /tmp/.s3cfg ls  s3://realm-ci-artifacts/sync/
//        syncServerChildProcess = spawn('/tmp/opt/realm-sync-0.27.4-101/bin/realm-server', ['-r', path, '-L', '127.0.0.1', '-l', 'all', '-k', './keys/public.pem', '-K', './keys/private.pem'], {env: {LD_LIBRARY_PATH: '/tmp/opt/realm-sync-0.27.4-101/lib/'}});
        // local config:
        syncServerChildProcess = spawn('./realm-sync-server-0.27.4/realm-server-dbg-noinst', ['-r', path, '-L', '127.0.0.1', '-l', 'all', '-k', './keys/public.pem', '-K', './keys/private.pem'], {env: {LD_LIBRARY_PATH: './realm-sync-0.27.4/'}});
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
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Starting a server');
});

// stop a previously started sync server
dispatcher.onGet("/stop", function(req, res) {
    syncServerChildProcess.kill();
    temp.cleanupSync();
    winston.info("Sync server stopped");
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Stopping the server');
});



//Create and start the Http server
var server = http.createServer(handleRequest);
server.listen(PORT, function() {
    winston.info("Integration test server listening on: 127.0.0.1:%s", PORT);
});
