/* global describe, it, expect */
describe("tests for TDAR.leaflet methods", function() {  

	it("should work when we call initLeafletMaps", function() {
	   var options = null;
	   var expectedVal = null;

	   //var result = TDAR.leaflet.adminUsageStats(options);
	   expect(TDAR.leaflet).toExist(); //fixme: implement this test
	});

	it("should work when we call initLeafletMaps", function() {
	   loadFixtures("leaflet-view.html");
//	   $(".leaflet-map").data("maxy","");
       TDAR.leaflet.initLeafletMaps();
	   var options = null;
	   var expectedVal = null;
	   //var result = TDAR.leaflet.adminUsageStats(options);
	   console.log(TDAR.leaflet.initialized());
	   //https://www.npmjs.com/package/jasmine-jquery-matchers
	   expect(TDAR.leaflet.initialized()).toBe(true); //fixme: implement this test
	   expect($(".leaflet-container")).toHaveLength(3);
	   
	   // assert that the map was properly initialized and has the rectangle
	   // assert that the map was not proeprly initialized because it was missing a parameter
	   // assert that an "edit" was initialzied
	   // assert that a change to the map, updated hidden inputs and visa-versa (may not be possible)
	   // assert that the degree translation on the hidden vs. the visible inputs updated properly
	   // assert that a result map has items
	});

});
