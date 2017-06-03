/* global describe, it, expect, beforeEach, jasmine */

//custom jasmine matchers and common setup/teardown go here
var self = this;


/**
 * Karma webserver rootUrl for static content is  "/base", so a file located in /src/test/frontend/fixtures will
 * have a url of localhost:9876/base/src/test/frontend/fixtures
 */
jasmine.getFixtures().fixturesPath = "base/src/test/frontend/integrate";
jasmine.getJSONFixtures().fixturesPath="base/src/test/frontend/fixtures/integrate";
