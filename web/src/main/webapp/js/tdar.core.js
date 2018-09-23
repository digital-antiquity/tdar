
// define global TDAR object if not defined already
var TDAR = window['TDAR'] || {};
window.TDAR = TDAR;
if (TDAR['vuejs'] == undefined) {
	TDAR['vuejs'] = {};
}

/**
 * Returns the namespace specified and creates it if it doesn't exist (e.g.
 * "TDAR.maps.controls", "TDAR.stringutils") (see YUI "module pattern",
 * http://yuiblog.com/blog/2007/06/12/module-pattern/) for more information
 */
var namespace = function() {
	var a = arguments, o = null, i, j, d;
	for (i = 0; i < a.length; i = i + 1) {
		d = ("" + a[i]).split(".");
		o = TDAR;

		// TDAR is implied, so it is ignored if it is included
		for (j = (d[0] == "TDAR") ? 1 : 0; j < d.length; j = j + 1) {
			o[d[j]] = o[d[j]] || {};
			o = o[d[j]];
		}
	}
	return o;
};

var configureJiraCollector = function(ctx, username) {
	if (!username)
		return;

	if (!ctx.ATL_JQ_PAGE_PROPS) {
		ctx.ATL_JQ_PAGE_PROPS = {};
	}
	;
	if (ctx.ATL_JQ_PAGE_PROPS) {
		ctx.ATL_JQ_PAGE_PROPS.fieldValues = {
			summary : "bug report from " + username + "."
		};
	}
};

/**
 * Load a script asynchronously. If jQuery is available, this function returns a
 * promise object. If the caller provides a callback function, this function
 * will call it once after the client successfully loads the resource.
 * 
 * @param url
 *            url containing the javascript file.
 * @param cb
 * @returns {*}
 */
var loadScript = function(url) {
	var _url = url;
	var head = document.getElementsByTagName("head")[0];
	var script = document.createElement("script");
	var deferred, promise;
	// console.debug("loading url: %s", _url);
	if (typeof jQuery === "function") {
		deferred = $.Deferred()
		promise = deferred.promise();

		script.onload = function() {
			deferred.resolve();
			// console.debug("successfully loaded:%s", _url);
		};

		script.onerror = function(err) {
			deferred.rejectWith(err);
			// console.log("failed to load url:%s error:%s", _url, err);
		};
	}
	script.src = _url;
	head.appendChild(script);
	return promise;
};

/**
 * Scan the DOM for SCRIPT nodes of type "application/json", parse their
 * content, and return a map of the parsed objects (keyed by script.id). Useful
 * for ingesting inlined data from server.
 * 
 * @returns {{}}
 * @private
 */
var loadDocumentData = function _loadDocumentData() {
	var dataElements = $('[type="application/json"][id]').toArray();
	var map = {};
	dataElements.forEach(function(elem) {
		var key = elem.id;
		var val = JSON.parse(elem.innerHTML);
		map[key] = val;
	});
	return map;
};

// define TDAR.uri(). Note, if deploying app in other than root context, you
// must set <base href="${request.contextPath}">
var uri = function(path) {
	if (!window.location.origin) {
		window.location.origin = window.location.protocol + "//"
				+ window.location.hostname
				+ (window.location.port ? ':' + window.location.port : '');
	}
	var base = window.location.origin;
	var baseElems = document.getElementsByTagName('base');
	if (baseElems.length) {
		if (baseElems[0].href != undefined) {
			base = baseElems[0].href;
		}
	}
	var uri = base;
	if (uri.lastIndexOf("/") != uri.length - 1) {
		uri += "/";
	}
	if (uri.lastIndexOf("/") == uri.length - 1 && path != undefined
			&& path.indexOf("/") == 0) {
		uri = uri.substring(0, uri.lastIndexOf("/"));
	}

	if (path) {
		uri += path;
	}
	return uri;
};

var assetsUri = function(path) {
	if (!window.location.origin) {
		window.location.origin = window.location.protocol + "//"
				+ window.location.hostname
				+ (window.location.port ? ':' + window.location.port : '');
	}
	var base = window.location.origin;
	var baseElems = document.getElementsByTagName('base');
	if (baseElems.length) {
		base = baseElems[0].assetsHref;
	}
	var uri = base;

	if (base == undefined) {
		return TDAR.uri(path);
	}

	if (uri.lastIndexOf("/") != uri.length - 1) {
		uri += "/";
	}
	if (uri.lastIndexOf("/") == uri.length - 1 && path != undefined
			&& path.indexOf("/") == 0) {
		uri = uri.substring(0, uri.lastIndexOf("/"));
	}

	if (path) {
		uri += path;
	}
	return uri;
};

/* istanbul ignore next */
/**
 * Wrapper for window.location setter
 * 
 * @param url
 */
var windowLocation = function(url) {
	window.location = url;
};


/**
 * Returns a copy of a string, terminated by ellipsis if input string exceeds max length
 * @param str input string
 * @param maxlength maximum length of the copy string.
 * @param useWordBoundary should we ellipsify in the middle of a word?
 * @returns {*} copy of string no longer than maxlength.
 */
var _ellipsify = function(text, n, useWordBoundary) {
        /* from: http://stackoverflow.com/questions/1199352/smart-way-to-shorten-long-strings-with-javascript */
        var toLong = text.length > n, s_ = toLong ? text.substr(0, n - 1) : text;
        s_ = useWordBoundary && toLong ? s_.substr(0, s_.lastIndexOf(' ')) : s_;
        return  toLong ? s_ + '...' : s_;
}

/**
 * Execute any main() functions found in the API
 */
 var main = function() {
     let TDAR = window.TDAR;
	// FIXME: I don't fully work, because some things are 3 levels down in the
	// object tree tdar.vue.upload (e.g.)
	for ( var key in TDAR) {
		if (typeof TDAR[key] !== 'object') {
			continue
		}
		if (typeof (TDAR[key]['main']) !== 'function') {
			continue
		}
		var pkg = TDAR[key];
		console.log('executing main in package:' + key);
		pkg.main();
	}
};

/**
 * Define dummy console + log methods if not defined by browser.
 */
if (!window.console) {
	console = {};
}

var _noop = function() {
	
};

console.log = console.log || _noop;
console.info = console.info || console.log;
console.error = console.error || console.log;
console.warn = console.warn || console.log;
console.debug = console.debug || console.log;
console.table = console.table || console.log;

module.exports = {
	main: main,
	configureJiraCollector:configureJiraCollector,
	loadScript: loadScript,
	loadDocumentData: loadDocumentData,
	uri : uri,
	assetsUri : assetsUri,
	windowLocation: windowLocation,
	namespace: namespace,
	ellipsify: _ellipsify
}
