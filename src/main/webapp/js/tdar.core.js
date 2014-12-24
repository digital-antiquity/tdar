/** top level namespace for tDAR javascript libraries */
//FIXME: remove this if-wrapper once TDAR-3830 is complete
if(typeof TDAR === "undefined") {
    var TDAR =  {};
}

/**
 * Returns the namespace specified and creates it if it doesn't exist  (e.g. "TDAR.maps.controls",  "TDAR.stringutils")
 * (see YUI "module pattern",  http://yuiblog.com/blog/2007/06/12/module-pattern/) for more information
 */
TDAR.namespace = function () {
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


/**
 * Load a script asynchronously. If jQuery is available, this function returns a promise object.  If the caller
 * provides a callback function, this function will call it once after the client successfully loads the resource.
 * @param url url containing the javascript file.
 * @param cb
 * @returns {*}
 */
TDAR.loadScript = function (url) {
    var _url = url;
    var head = document.getElementsByTagName("head")[0];
    var script = document.createElement("script");
    var deferred, promise;
    console.log("loading url: %s", _url);
    if (typeof jQuery === "function") {
        deferred = $.Deferred()
        promise = deferred.promise();

        script.onload = function () {
            deferred.resolve();
            console.log("successfully loaded:%s", _url);
        };

        script.onerror = function (err) {
            deferred.rejectWith(err);
            console.log("failed to load url:%s  error:%s", _url, err);
        };
    }
    script.src = _url;
    head.appendChild(script);
    return promise;
}

/**
 * Define dummy console + log methods if not defined by browser.
 */
if (!window.console) {
    console = {};
}

console.log = console.log || function () {
};
console.warn = console.warn || function () {
};
console.debug = console.debug || function () {
};
console.error = console.error || function () {
};
console.info = console.info || function () {
};
console.trace = function () {
};
