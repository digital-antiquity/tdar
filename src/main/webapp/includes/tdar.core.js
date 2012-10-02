//tDAR core javascript utilities (w/ apologies to YUI: http://developer.yahoo.com/yui)

//top-level global object used by tdar js api
if(typeof TDAR === "undefined" || !TDAR) {
    var TDAR = {};
}

//Returns the namespace specified and creates it if it doesn't exist  (e.g. "TDAR.maps.controls",  "TDAR.stringutils")
//see YUI "module pattern",  http://yuiblog.com/blog/2007/06/12/module-pattern/
TDAR.namespace = function() {
    var a=arguments, o=null, i, j, d;
    for (i=0; i<a.length; i=i+1) {
        d=(""+a[i]).split(".");
        o=TDAR;

        // TDAR is implied, so it is ignored if it is included
        for (j=(d[0] == "TDAR") ? 1 : 0; j<d.length; j=j+1) {
            o[d[j]]=o[d[j]] || {};
            o=o[d[j]];
        }
    }

    return o;
};

//TODO: create an "info" function that dumps all the namespaces currently loaded.