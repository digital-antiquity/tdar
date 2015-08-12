/* global describe, it, expect */
describe("tests for TDAR.maps methods", function() {  

xit("should work when we call apiLoaded", function() {
   var expectedVal = null;

   //var result = TDAR.maps.apiLoaded();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call initMapApi", function() {
   var expectedVal = null;

   //var result = TDAR.maps.initMapApi();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call setupMap", function() {
   var mapDiv = null;
   var inputContainer = null;
   var expectedVal = null;

   //var result = TDAR.maps.setupMap(mapDiv, inputContainer);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call updateResourceRect", function() {
   var mapDiv = null;
   var swlat = null;
   var swlng = null;
   var nelat = null;
   var nelng = null;
   var expectedVal = null;

   //var result = TDAR.maps.updateResourceRect(mapDiv, swlat, swlng, nelat, nelng);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call clearResourceRect", function() {
   var mapDiv = null;
   var expectedVal = null;

   //var result = TDAR.maps.clearResourceRect(mapDiv);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call setupEditMap", function() {
   var mapDiv = null;
   var inputContainer = null;
   var expectedVal = null;

   //var result = TDAR.maps.setupEditMap(mapDiv, inputContainer);
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call setupMapResult", function() {
   var expectedVal = null;

   //var result = TDAR.maps.setupMapResult();
   expect(true).toBe(false); //fixme: implement this test
});

xit("should work when we call addBound", function() {
   var mapDiv = null;
   var rectStyleOptions = null;
   var lat1 = null;
   var lng1 = null;
   var lat2 = null;
   var lng2 = null;
   var expectedVal = null;

   //var result = TDAR.maps.addBound(mapDiv, rectStyleOptions, lat1, lng1, lat2, lng2);
   expect(true).toBe(false); //fixme: implement this test
});

});

describe("latlong tests", function(){
   var RADIUS_EARTH = 6371;
   var RADIUS_MARS = 3389.28;
   var PI = Math.PI;
   var PI2 = 2 * Math.PI;

   function ll(lat, lon, r) {
      return new LatLon(lat, lon, r);
   }


   it('creates new instance', function() {
      var pEarth  = ll(0,0)
      var pMars  = ll(0,0, RADIUS_MARS);
   });



   it('measures distance', function() {
      var p1 = new ll(0,0)
      var p2 = new ll(180,0);
      var d1 = p1.distanceTo(p2);
      expect(Math.abs(d1 - 40075.017/2)).toBeLessThan(20);
   })

});
