/**
 * top-level support for unhandled errors. Helpful info here:
 *    http://www.quirksmode.org/dom/events/error.html
 *    http://www.quirksmode.org/dom/events/tests/error.html#
 */
(function(){
    "use strict";
    //assume that no libraries or globals are available to us yet
    var _errors = [];
    var _head =  document.getElementsByTagName('head')[0];
    var _delim = "\n****************************";
    var _errs = window.__errorMessages;


    function _id(id) {
        var elem = document.getElement(id);
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
        var elem = _id('errorTextarea');
        if(!elem) {
            //TODO: create the element if it's not found (make sure it's hidden)
        }
        return elem;
    }

    function _addErr(obj) {
        _errs.push(obj.message); //todo: remove this line after you've updated selenium tests
        console.log(_json(obj));
        var ta = _errorTextarea();
        if(!ta) return;
        var txt = _delim;
        for(var k in obj) {
            txt += "\n" + k + ":";
            txt += "\n" + obj[k];
        }
    }

    window.addEventListener("error", function(e){
        var evt = e || window.event;
        var tgt = evt.target;
        var obj = {
            message: evt.message || "failure in script",
            url: tgt.outerHTML,
            lineno: tgt.lineno
        };
        _addErr(obj);
    }, true);


    window.onerror = function(msg, url, line) {
        //ignore dom error events, they're handled by the error listener
        if(typeof msg !== "string") return;
        _addErr({
            message: msg,
            url: url,
            lineno: line
        });
    }

//    window.onerror = function(msg, url, line) {
//        console.error("You've Got Errors!");
//        console.error(msg, url, line);
//        //is msg really an  "error" Event? (e.g. script failed to load/parse)
//        if(typeof msg === "object" && (typeof JSON !== "undefined") ) {
//            if(!msg.target) {
//                msg.target = {src: "na", text:"na"};
//            }
//            var t = msg.target;
//            errs.push("msg:" + msg.message + "\n src:" + t.src  + "\n text:" + t.text);
//
//            if(msg.stopPropagation) msg.stopPropagation();
//
//            //just a regular error - capture msg, url, line#
//        } else {
//            errs.push(msg + " url:" + url + " line:" + line);
//            console.log("logged error message");
//            return false;
//        }
//    };



})();

