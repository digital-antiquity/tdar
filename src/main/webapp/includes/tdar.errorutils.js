/**
 * top-level support for unhandled errors. Helpful info here:
 *    http://www.quirksmode.org/dom/events/error.html
 *    http://www.quirksmode.org/dom/events/tests/error.html#
 */
(function(){
    "use strict";
    //assume that no libraries or globals are available to us yet
    var _head =  document.getElementsByTagName('head')[0];
    if(typeof(TDAR_jsErrorDelim)==='undefined') TDAR_jsErrorDelim = "\r\n";
    var _delim = TDAR_jsErrorDelim;
    var _errors = window.__errorMessages = [];

    var _start = Date.now();

    //no now() in ie8
    function _now() {
        return (new Date()).getTime();
    }

    function _id(id) {
        var elem = document.getElementById(id);
        return elem;
    }

    function _json(obj) {
        var str = "stringify not supported";
        if(JSON) {
            str =  JSON.stringify(obj);
        }
        return str
    }

    function _errorTextarea() {
        var elem = _id('javascriptErrorLog');
        if(!elem) {
            //TODO: create the element if it's not found (make sure it's hidden)
        }
        return elem;
    }

    function _addErr(obj) {
        var txt = "";
        if(_errors.length > 0) {
            txt += _delim;
        }
        obj.time = ((_now() - _start) / 1000).toFixed(3) + 's';

        _errors.push(_json(obj));
        //console.log(_json(obj));
        var ta = _errorTextarea();
        //a page might not have a textarea (e.g. a view page) or an error event happened before it was parsed
        if(ta) {
            txt = ta.value;
            for(var k in obj) {
                txt += "\n" + k + ":"  + obj[k];
            }
            ta.value = txt;
        }
    }

    window.addEventListener("error", function(e){
        var evt = e || window.event;
        var tgt = evt.target;
        var obj = {
            message: "errorEvent::" +  (evt.message || "(no error message)"),
            filename: evt.filename || "(no filename - probably script from remote host)",
            line: evt.lineno,
            tag: "(inline script)"
        };

        if(tgt !== window) {
            obj.tag = tgt.outerHTML;
        }
        if(!!tgt.tagName && tgt.tagName !== "SCRIPT") {
            //TODO: not a script issue (e.g. missing css or image), put this in another global error list
        } else {
            _addErr(obj);
        }
    }, true);

//TODO: onerror callback is not as helpful, since it isn't called for missing/unloaded files, but it might be necessary for ie8
//    window.onerror = function(msg, url, line) {
//        //ignore dom error events, they're handled by the error listener
//        if(typeof msg !== "string") return;
//
//        _addErr({
//            message: "onerror::" + msg,
//            filename: url,
//            line: line
//        });
//    }
})();

