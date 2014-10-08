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

/** Simple JavaScript Inheritance
 * By John Resig http://ejohn.org/
 * MIT Licensed.
 */
(function () {
    var initializing = false, fnTest = /xyz/.test(function () {
        xyz;
    }) ? /\b_super\b/ : /.*/;

    /**
     * Simple JavaScript Inheritance By John Resig.
     *
     * This javascript prototype mimics the behavior of class-based inheritance languages. "Sub-class" classes
     * will have a parent methods copied to the subclass' prototype.  In addition,  subclass methods can reference
     * the parent class via this._super.
     *
     * @constructor
     */
    this.Class = function () {
    };

    /**
     * Creates a new "child" class.
     * @param props class js-object containing properties and methods.  This function copies these properties to the proto-
     * type.  If the object  contains a function property with the same name as a "parent" class' prototype property,
     * extend() will "override"  that function, and provide an alias to the parent method via this._super.
     *
     * To specify a constructor for the inheriting class, define a property in the props argument named "init". when
     * instantiating a object (e.g.  new MyClass(arg1, arg2, arg3), the class will execute the init function (along with
     * any arguments supplied by the caller).
     *
     * @returns {Class}
     */
    Class.extend = function (props) {
        var _super = this.prototype;

        // Instantiate a base class (but only create the instance,
        // don't run the init constructor)
        initializing = true;
        var prototype = new this();
        initializing = false;

        // Copy the properties over onto the new prototype
        for (var name in props) {
            // Check if we're overwriting an existing function
            prototype[name] = typeof props[name] == "function" && typeof _super[name] == "function" && fnTest.test(props[name]) ? (function (name, fn) {
                return function () {
                    var tmp = this._super;

                    // Add a new ._super() method that is the same method
                    // but on the super-class
                    this._super = _super[name];

                    // The method only need to be bound temporarily, so we
                    // remove it when we're done executing
                    var ret = fn.apply(this, arguments);
                    this._super = tmp;

                    return ret;
                };
            })(name, props[name]) : props[name];
        }

        /**
         * The base class constructor.
         * @constructor
         */
        function Class() {
            // All construction is actually done in the init method
            if (!initializing && this.init) {
                this.init.apply(this, arguments);
            }
        }

        // Populate our constructed prototype object
        Class.prototype = prototype;

        // Enforce the constructor to be what we expect
        Class.prototype.constructor = Class;

        // And make this class extendable
        Class.extend = arguments.callee;

        return Class;
    };
})();

/**
 * Load a script asynchronously. If jQuery is available, this function returns a promise object.  If the caller
 * provides a callback function, this function will call it once after the client successfully loads the resource.
 * @param url url containing the javascript file.
 * @param cb
 * @returns {*}
 */
TDAR.loadScript = function (url) {
    //TODO: allow for optional callback argument  (e.g.  loadScript("foo.js", function(err, result) {})
    var _url = url;
    var head = document.getElementsByTagName("head")[0];
    var script = document.createElement("script");
    var deferred, promise;

    if (typeof jQuery === "function") {
        deferred = $.Deferred()
        promise = deferred.promise();

        script.onload = function () {
            deferred.resolve();
        };

        script.onerror = function (err) {
            deferred.rejectWith(err);
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
