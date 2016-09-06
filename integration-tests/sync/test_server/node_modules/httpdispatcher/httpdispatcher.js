var util = require('util');
var HttpDispatcher = function() {
	this.listeners = { get: [ ], post: [ ] };
	this.filters = { before: [ ], after: [ ] };
	this.errorListener = function(req, res) { 
		res.writeHead(404);
		res.end();
	}
	this.staticFolderPrefix = '/static';
	this.staticDirname;
}
HttpDispatcher.prototype.on = function(method, url, cb) {
	this.listeners[method].push({
		cb: cb,
		url: url
	});
}
HttpDispatcher.prototype.filter = function(method, url, cb) {
	this.filters[method].push({
		cb: cb,
		url: url
	});
}
HttpDispatcher.prototype.onGet = function(url, cb) {
	this.on('get', url, cb);
}	
HttpDispatcher.prototype.onPost = function(url, cb) {
	this.on('post', url, cb);
}
HttpDispatcher.prototype.onError = function(cb) {
	this.errorListener = cb;
}
HttpDispatcher.prototype.setStatic = function(folder) {
	this.on('get', new RegExp("\/"+folder), this.staticListener.bind(this));
}
HttpDispatcher.prototype.setStaticDirname = function(dirname) {
	this.staticDirname = dirname;
}
HttpDispatcher.prototype.beforeFilter = function(url, cb) {
	this.filter('before', url, cb);
}
HttpDispatcher.prototype.afterFilter = function(url, cb) {
	this.filter('after', url, cb);
}
HttpDispatcher.prototype.dispatch = function(req, res) {
	var url = require('url').parse(req.url, true);
	var method = req.method.toLowerCase();
	var dispatcher = this;
	var doDispatch = function() {
		var httpChain = new HttpChain();
		var beforeFilters = this.getFilters(url.pathname, 'before');
		httpChain.addAll(beforeFilters);
		var listener = this.getListener(url.pathname, method);
		var listenerCb = listener ? listener : this.errorListener;
		httpChain.add(httpChain.getWrapped(listenerCb));
		var afterFilters = this.getFilters(url.pathname, 'after');
		httpChain.addAll(afterFilters);
		httpChain.next(req, res);
	}
	if(method == 'post') {
		var body = '';
		req.on('data', function(data) {
			body += data;
		});
		req.on('end', function() {
			var post = require('querystring').parse(body);
			req.body = body;
			req.params = post;
			doDispatch.call(dispatcher);
		});
	} else {
		var url_parts = require('url').parse(req.url, true);
		req.params = url_parts.query;
		doDispatch.call(dispatcher);
	}
}
HttpDispatcher.prototype.staticListener =  function(req, res) {
	var url = require('url').parse(req.url, true);
	var filename = "." + require('path').join(this.staticDirname, url.pathname);
	var errorListener = this.errorListener;
	require('fs').readFile(filename, function(err, file) {
		if(err) {
			errorListener(req, res);
			return;
		}
		res.writeHeader(200, {
			"Content-Type": require('mime').lookup(filename)
		});
		res.write(file, 'binary');
		res.end();
	});
}
HttpDispatcher.prototype.getListener = function(url, method) {
	for(var i = 0, listener; i<this.listeners[method].length; i++) {
		listener = this.listeners[method][i];
		if(this.urlMatches(listener.url, url)) return listener.cb;
	}
}
HttpDispatcher.prototype.getFilters = function(url, type) {
	var filters = [];
	for(var i = 0, filter; i<this.filters[type].length; i++) {
		filter = this.filters[type][i];
		if(this.urlMatches(filter.url, url)) filters.push(filter.cb);
	}
	return filters;
}
HttpDispatcher.prototype.urlMatches = function(config, url) {
	if(config instanceof RegExp) return config.test(url);
	if(util.inspect(config) == "[Function]") return config(url);
	return config == url;
}
var HttpChain = function() {
	this.queue = [];
}
HttpChain.prototype.add = function(cb) {
	this.queue.push(cb);
}
HttpChain.prototype.addAll = function(cbs) {
	for(var i = 0; i<cbs.length; i++) this.add(cbs[i]);
}
HttpChain.prototype.next = function(req, res) {
	var cb = this.queue.shift();
	if(cb) cb(req, res, this);
}
HttpChain.prototype.stop = function(req, res) {
	res.end();
}
HttpChain.prototype.getWrapped = function(cb) {
	return function(req, res, chain) {
		cb(req, res);
		chain.next(req, res);
	}
}
module.exports = new HttpDispatcher();
