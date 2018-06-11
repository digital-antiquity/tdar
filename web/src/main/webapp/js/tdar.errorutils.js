/**
 * top-level support for unhandled errors. Helpful info here:
 *    http://www.quirksmode.org/dom/events/error.html
 *    http://www.quirksmode.org/dom/events/tests/error.html#
 */
(function () {
    "use strict";
    //assume that no libraries or globals are available to us yet
    var _head = document.getElementsByTagName('head')[0];
    if (typeof(TDAR_jsErrorDelim) === 'undefined') {
        var TDAR_jsErrorDelim = "ɹǝʇıɯıןǝp";
    }
    var _delim = TDAR_jsErrorDelim;
    var _errors = window.__errorMessages = [];
    var _start = Date.now();

    /**
     * Return current date  - Date.now() is not supported in IE8
     * @returns {number} number representing epoch time
     * @private
     */
    function _now() {
        return (new Date()).getTime();
    }

    /**
     * Shortcut for document.getElementById.
     *
     * @param id element ID to search for
     * @returns {HTMLElement} DOM Element if found, otherwise undefined.
     * @private
     */
    function _id(id) {
        var elem = document.getElementById(id);
        return elem;
    }

    /**
     * Wrapper for JSON.stringify(), does not throw error if feature not supported.
     * @param obj
     * @returns {string} stringified version of supplied object, or "not supported".
     * @private
     */
    function _json(obj) {
        var str = "not supported";
        if (JSON) {
            str = JSON.stringify(obj);
        }
        return str
    }

    /**
     * Returns a node that contains a textarea node that we will use to dump error information.
     * @returns {HTMLElement}
     * @private
     */
    function _errorTextarea() {
        var elem = _id("javascriptErrorLog");
        //TODO: hold off on dynamically creating the error log textarea.  Haven't decided if it's a good idea.
        //        if(!elem && !document.forms.length) {
        //            _el("textarea", document.forms[document.forms.length-1], {id: "javascriptErrorLog", name: "javascriptErrorLog", style:"display:none"});
        //        }
        return elem;
    }

    /**
     * convenience method for creating dom node
     * @param objtype  type of node to create
     * @param attrs  map of attributes
     * @private
     */
    function _el(objtype, parent, attrs) {
        var el, attr, val;
        el = document.createElement(objtype);
        for (attr in attrs) {
            el[attr] = attrs[attr];
        }
    }

    /**
     * Append an error to the error log.
     * @param obj
     * @private
     */
    function _addErr(obj) {
        var txt = "";
        if (_errors.length > 0) {
            txt += _delim;
        }
        obj.time = ((_now() - _start) / 1000).toFixed(3) + 's';

        _errors.push(_json(obj));
        //console.log(_json(obj));
        var ta = _errorTextarea();
        //a page might not have a textarea (e.g. a view page) or an error event happened before it was parsed
        if (ta) {
            txt = ta.value;
            for (var k in obj) {
                txt += "; " + k + ":" + obj[k];
            }
            ta.value = txt;
        }
    }

    /**
     * Error handler used if the browser supports the Error event.  Otherwise we use a separate window.onerror handler
     * Note that the amount of information that we get is based on:
     *      - type of error: parse errors, runtime errors, uri failure
     *      - vendor: IE8 uses window.onerror, chrome/FF use "error" event.
     *      - source:
     *          inline: reports most information (message,  file, line, col)
     *          same domain: same, but not as helpful if you use a minifier
     *          other domain:  uri failures are reported,  but parse/runtime errors have line/message stripped.
     * @param e
     * @private
     */
    function _errorListener(e) {
        //if listener was called, assume onerror is redundant and unregister it
        //console.log("error listener support detected");
        window.onerror = null;
        var evt = e || window.event;
        var tgt = evt.target;
        var filename = evt.filename, line = evt.lineno, col = evt.colno;

        //if filename blank but lineno exists, it's an inline script
        if (!filename && evt.lineno) {
            filename = window.location.pathname;
        }

        if (col) {
            line += ":" + col;
        }

        var obj = {
            message: "errorEvent::" + (evt.message || "(no error message)"),
            filename: filename,
            line: line,
            tag: "n/a"
        };

        if (tgt !== window) {
            obj.tag = tgt.outerHTML;
        }
        if (!!tgt.tagName && tgt.tagName !== "SCRIPT") {
            //error event happened, but not due to javascript (perhaps css or image failed to load. don't report
            console.log("non-script-related error occured");
        } else {
            _addErr(obj);
        }
    }

    /**
     * Callback for window.onerror. Instead of an  event object, the error details are passed in as arguments
     * to the callback
     * @param msg error message
     * @param url url, or file name where the error occured
     * @param line
     * @private
     */
    function _onerror(msg, url, line) {
        //ignore dom error events, they're handled by the error listener
        if (typeof msg !== "string") {
            return;
        }

        _addErr({
            message: "onerror::" + msg,
            filename: url,
            line: line
        });
    }

    // Register both types of error handlers: event based, and onerror-based. Once we detect that browser supports
    // the error event we disable window.onerror (so we don't double-up on error messages.
    if(window.addEventListener) {
        window.addEventListener("error", _errorListener, true);
    }
    window.onerror = _onerror;

})();
