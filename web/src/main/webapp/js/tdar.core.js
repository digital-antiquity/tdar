(function (console, window) {
    'use strict';

//define global TDAR object if not defined already
    var TDAR = window['TDAR'] || {};
    window.TDAR = TDAR;
    if (TDAR['vuejs'] == undefined) {
        TDAR['vuejs'] = {};
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

    TDAR.configureJiraCollector = function (ctx, username) {
        if (!username) return;

        if (!ctx.ATL_JQ_PAGE_PROPS) {
            ctx.ATL_JQ_PAGE_PROPS = {};
        }
        ;
        if (ctx.ATL_JQ_PAGE_PROPS) {
            ctx.ATL_JQ_PAGE_PROPS.fieldValues = {
                summary: "bug report from " + username + "."
            };
        }
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
        //console.debug("loading url: %s", _url);
        if (typeof jQuery === "function") {
            deferred = $.Deferred()
            promise = deferred.promise();

            script.onload = function () {
                deferred.resolve();
                //console.debug("successfully loaded:%s", _url);
            };

            script.onerror = function (err) {
                deferred.rejectWith(err);
                //console.log("failed to load url:%s  error:%s", _url, err);
            };
        }
        script.src = _url;
        head.appendChild(script);
        return promise;
    };

    /**
     * Scan the DOM for SCRIPT nodes of type "application/json", parse their content, and return a map of the parsed objects (keyed by script.id).  Useful
     * for ingesting inlined data from server.
     * @returns {{}}
     * @private
     */
    TDAR.loadDocumentData = function _loadDocumentData() {
        var dataElements = $('[type="application/json"][id]').toArray();
        var map = {};
        dataElements.forEach(function (elem) {
            var key = elem.id;
            var val = JSON.parse(elem.innerHTML);
            map[key] = val;
        });
        return map;
    };

//define TDAR.uri(). Note, if deploying app in other than root context,  you must set <base href="${request.contextPath}">
    TDAR.uri = function (path) {
        if (!window.location.origin) {
            window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
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
        if (uri.lastIndexOf("/") == uri.length - 1 && path != undefined && path.indexOf("/") == 0) {
            uri = uri.substring(0, uri.lastIndexOf("/"));
        }

        if (path) {
            uri += path;
        }
        return uri;
    };

    TDAR.assetsUri = function (path) {
        if (!window.location.origin) {
            window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
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
        if (uri.lastIndexOf("/") == uri.length - 1 && path != undefined && path.indexOf("/") == 0) {
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
     * @param url
     */
    TDAR.windowLocation = function (url) {
        window.location = url;
    };

    /**
     * Execute any main() functions found in the API
     */
    TDAR.main = function () {
        for(var key in TDAR) {
            if(typeof TDAR[key] !== 'object') {continue}
            if(typeof (TDAR[key]['main']) !== 'function' ) {continue}
            var pkg = TDAR[key];
            console.log('executing main in package:' + key);
            pkg.main();
        }
        $(".input-group-append .fa-search").click(function(){
            $(this).parents('form:first').submit();
        });
    };


    /**
     * Define dummy console + log methods if not defined by browser.
     */
    if (!window.console) {
        console = {};
    }

    var _noop = function () {
    };
    console.log = console.log || _noop;
    console.info = console.info || console.log;
    console.error = console.error || console.log;
    console.warn = console.warn || console.log;
    console.debug = console.debug || console.log;
    console.table = console.table || console.log;


})(console, window);