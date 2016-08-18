const temp = require('temp');
const spawn = require('child_process').spawn;
var http = require('http');
var dispatcher = require('httpdispatcher');

// Automatically track and cleanup files at exit
temp.track();

if (process. argv. length <= 2) {
    console.log("Usage: " + __filename + " absolute_path_to_sync_server_binary");
    process.exit(-1);
}
const syncServerBinaryDir = process.argv[2];

const PORT = 8888;

function handleRequest(request, response){
    try {
        //log the request on console
        console.log(request.url);
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
        console.log("Starting sync server in ", path);
        syncServerChildProcess = spawn(syncServerBinaryDir + '/realm-server-noinst', ['-r', path, '-L', '127.0.0.1', '-l', 'all', '-k', './keys/public.pem', '-K', './keys/private.pem']);

        syncServerChildProcess.stdout.on('data', (data) => {
          console.log(`stdout: ${data}`);
        });

        syncServerChildProcess.stderr.on('data', (data) => {
          console.log(`stderr: ${data}`);
        });

        syncServerChildProcess.on('close', (code) => {
          console.log(`child process exited with code ${code}`);
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
    // Do work
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Stopping the server');
});

//Create and start the Http server
var server = http.createServer(handleRequest);
server.listen(PORT, function() {
    console.log("Integration test server listening on: 127.0.0.1:%s", PORT);
});
