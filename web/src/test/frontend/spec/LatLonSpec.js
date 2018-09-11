/* global describe, it, expect, $, $j, jasmine */

const LatLon = require("JS/latLongUtil-1.0").LatLon;
const Geo = require("JS/latLongUtil-1.0").Geo;

describe("LatLonSpec.js: latlong tests", function () {
    "use strict";
    var RADIUS_EARTH = 6371;
    var RADIUS_MARS = 3389.28;
    var PI = Math.PI;
    var TWOPI = 2 * Math.PI;
    var CIRCUMFERENCE_EARTH = TWOPI * RADIUS_EARTH;

    var _ff = 20;
    function _approx(a, b) {
        return Math.abs(a-b) < _ff;
    }
    

    //FIXME:  RTFM Jim, you may learn something: https://github.com/jasmine/jasmine/blob/master/src/core/matchers/toBeCloseTo.js
    // Custom matcher that assumes that any two points/magnitudes within 20km of each other are 'approximately'
    // the same. This is helpful for calculations that introduce minor differences due to floating point precision.
    var latlonMatchers = {
        toBeNear: function (util, customEqualityTesters) {

            return {
                compare: function (actual, expected) {
                    var result = {};

                    if(actual instanceof LatLon) {
                        result.pass = _approx(actual.lat(), expected.lat()) && _approx(actual.lon(), expected.lon());
                    } else {
                        result.pass = _approx(actual, expected);
                    }

                    result.message = result.pass ? "Expected values to not be so close to each other" : "Expected values to be near each other";
                    result.message += " (actual:" + actual + "  expected:" + expected + ")";
                    return result;
                }
            }
        }
    };

    //add matchers before every test
    beforeEach(function() {
        jasmine.addMatchers(latlonMatchers);
    });

    //shortcut for creating a new LatLon instance
    function ll(lat, lon, r) {
        return new LatLon(lat, lon, r);
    }

    it('creates new instance', function () {
        var pEarth = ll(0, 0)
        var pMars = ll(0, 0, RADIUS_MARS);
    });

    it('measures distance', function () {
        var p1 = ll(0, 0)
        var p2 = ll(180, 0);
        var d1 = p1.distanceTo(p2);
        expect(Math.abs(d1 - 40075.017 / 2)).toBeLessThan(20);
    });

    it('calculates bearing', function () {
        var p1 = ll(0, 0);
        var p2 = ll(0, 1);
        expect(p1.bearingTo(p2)).toBe(90);
        expect(p2.bearingTo(p1)).toBe(270);
    });

    it('calculates bearing-to', function () {
        //I'll be real with you, I'm not sure the difference between bearingTo and finalBearingTo
        expect(ll(0, 0).finalBearingTo(ll(0, 1))).toBe(90);
        expect(ll(0, 1).finalBearingTo(ll(0, 0))).toBe(270);
    });

    it('calculates vector midpoint', function () {
        var p1 = ll(0, 0);
        var p2 = ll(0, 10);
        var m1 = ll(0, 5);
        var actual = p1.midpointTo(p2);
        expect(actual).toBeNear(m1);
    });

    it('calculates destination', function () {
        //let's go around the world (along the equator, natch)
        var start = ll(0, 0);
        var endExpected = ll(0, 0);
        var endActual = start.destinationPoint(90, CIRCUMFERENCE_EARTH);
        expect(endActual).toBeNear(endExpected);

        //wait, we actually did move, right?
        endActual = start.destinationPoint(90, CIRCUMFERENCE_EARTH / 2);
        expect(endActual.distanceTo(start)).toBeGreaterThan(100);
    });

    it('calculates intersection', function () {
        var p1 = ll(0, 0);
        var p2 = ll(0, 90);
        //if p1 heads north, and p2 heads due northwest, they obviously will meet at longitude 0
        var x = LatLon.intersection(p1, 0, p2, 315);
        expect(x.lon()).toBeNear(0);
    });

    it('rhumb lines: calculates distance ', function() {
        var p1 = ll(0, 0);
        var p2 = ll(45, 179);
        var rhumbDistance = p1.rhumbDistanceTo(p2);
        var distance = p1.distanceTo(p2);
        expect(rhumbDistance).toBeGreaterThan(distance);
    });

    it('rhumb lines: calculate bearing', function() {
        var p1 = ll(0, 0);
        var p2 = ll(45, 179);
        var a = p1.bearingTo(p2);
        var ra = p1.rhumbBearingTo(p2);
        expect(a).toBeLessThan(ra);
    });

    it('rhumb lines: calculate destination', function() {
        var p1 = ll(0,0);
        var dest = p1.destinationPoint(45, CIRCUMFERENCE_EARTH);
        var rdest = p1.rhumbDestinationPoint(45, CIRCUMFERENCE_EARTH);

        expect(dest).toBeNear(p1);

        //unlike destinationPoint(), our destination will not be the same location as the start
        expect(rdest).not.toBeNear(p1);
    });

    it('converts to/from DMS format', function() {
        var p1 = ll(1.11, 1.11);
        var pdms = {
            lat: p1.lat('dms'),
            lon: p1.lon('dms')
        };
        expect(pdms.lat).toBe("01°06′36″N");
        expect(pdms.lon).toBe("001°06′36″E");

        //and converting back from DMS to degrees
        var p2 = ll(Geo.parseDMS(pdms.lat), Geo.parseDMS(pdms.lon));
        expect(p1).toEqual(p2);

        expect(Geo.toBrng(1.11)).toBe('001°06′36″');
    });

    it('parses DMS', function() {
        expect(Geo.parseDMS('1°N', 'd')).toBe(1);
        expect(Geo.parseDMS('01°06′', 'dm')).toBe(1.1);
        expect(Geo.parseDMS('01°06′36″N', 'dms')).toBe(1.11);

        expect(Geo.toDMS(1, 'dms')).toBe('001°00′00″');
        expect(Geo.toDMS(1, 'dm')).toBe('001°00.00′');
        expect(Geo.toDMS(1, 'd')).toBe('001.0000°');

    })

});
