//custom jasmine matchers and common setup/teardown go here
/* global describe, it, expect, beforeEach, jasmine */
(function() {
    "use strict";
    
    /**
     * Karma webserver rootUrl for static content is  "/base", so a file located in /src/test/frontend/fixtures will 
     * have a url of localhost:9876/base/src/test/frontent/fixtures 
     */
    jasmine.getFixtures().fixturesPath = "base/src/test/frontend/fixtures";
     

})();