/* global describe, it, expect, beforeEach, jasmine */

    //custom jasmine matchers and common setup/teardown go here
    var self = this;
    
        
    /**
     * Karma webserver rootUrl for static content is  "/base", so a file located in /src/test/frontend/fixtures will 
     * have a url of localhost:9876/base/src/test/frontend/fixtures
     */
    jasmine.getFixtures().fixturesPath = "base/src/test/frontend/fixtures";

    self.$expect = function(selector){
        return expect($j(selector));
    }


    /**
     * Simple parser for querystrings. beware that dupe param names are overwritten.
     * @param qs
     * @returns {*}
     */
    self.queryParams = function(qs){
        if(qs.indexOf('?') > -1) {
            qs = qs.substr(qs.indexOf('?')+1);
        }
        var components = qs.split('&');
        return components.reduce(function(dct, component){
            var kv=component.split('=');
            if(kv.length===2){
                dct[kv[0]] = kv[1]
            } else if(kv.length===1) {
                dct[kv[0]] = true;
            }
            return dct;
        }, {});
    }


    /**
     * Helper function that encodes an object as a jQuery 'JSONP' string.  For use when passing responseText to a
     * jasmine.Ajax spy.
     *
     * TODO:// should work with POST requests but only tested with GET
     *
     * @param obj  The pojo you wish to encode
     * @param request the request associated with the response.
     * @returns {string} the jsonp-format test
     */
    self.jsonpEncode = function(obj, request) {
        var json = JSON.stringify(obj);
        var params = request.data();
        if(!params.callback){
            params = self.queryParams(request.url);
        }
        var jsonp = params.callback + '(' + json + ')';
        return jsonp;
    };
