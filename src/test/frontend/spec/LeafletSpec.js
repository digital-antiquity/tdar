/* global describe, it, expect */
describe("LeafletSpec", function() {  

	it("should work when we call initLeafletMaps", function() {
	   var options = null;
	   var expectedVal = null;

	   //var result = TDAR.leaflet.adminUsageStats(options);
	   expect(TDAR.leaflet).toExist(); //fixme: implement this test
	});

    it("initLeafletMaps:validSetup", function() {
        loadFixtures("leaflet-view.html");
//      $(".leaflet-map").data("maxy","");
        TDAR.leaflet.initLeafletMaps();
        var options = null;
        var expectedVal = null;
        //var result = TDAR.leaflet.adminUsageStats(options);
        console.log(TDAR.leaflet.initialized());
        //https://www.npmjs.com/package/jasmine-jquery-matchers
        expect(TDAR.leaflet.initialized()).toBeGreaterThan(-1); //fixme: implement this test
        expect($(".leaflet-container")).toHaveLength(1);
        
        // assert that the map was properly initialized and has the rectangle
        // assert that the map was not proeprly initialized because it was missing a parameter
        // assert that an "edit" was initialzied
        // assert that a change to the map, updated hidden inputs and visa-versa (may not be possible)
        // assert that the degree translation on the hidden vs. the visible inputs updated properly
        // assert that a result map has items
     });

    it("initLeafletMaps:invalidSetup", function() {
        loadFixtures("leaflet-view.html");
      $(".leaflet-map").data("maxy","");
        TDAR.leaflet.initLeafletMaps();
        var options = null;
        var expectedVal = null;
        //https://www.npmjs.com/package/jasmine-jquery-matchers
        expect(TDAR.leaflet.initialized()).toBeLessThan(-1); //fixme: implement this test
        
        // assert that the map was properly initialized and has the rectangle
        // assert that the map was not proeprly initialized because it was missing a parameter
        // assert that an "edit" was initialzied
        // assert that a change to the map, updated hidden inputs and visa-versa (may not be possible)
        // assert that the degree translation on the hidden vs. the visible inputs updated properly
        // assert that a result map has items
     });

     it("initLeafletMaps:validResultsSetup", function() {
         loadFixtures("leaflet-results.html");
         TDAR.leaflet.initResultsMaps();
         var options = null;
         var expectedVal = null;
         //var result = TDAR.leaflet.adminUsageStats(options);
         console.log(TDAR.leaflet.initialized());
         //https://www.npmjs.com/package/jasmine-jquery-matchers
         expect(TDAR.leaflet.initialized()).toBeGreaterThan(-1); //fixme: implement this test
         expect($(".leaflet-container")).toHaveLength(1);
		 // 1 rectangle and 4 place-holders
		 expect($(".leaflet-clickable")).toHaveLength(4);
		 expect($("div.marker-cluster")).toHaveLength(2);
		 expect($("img.leaflet-marker-icon")).toHaveLength(1);
		 // $("img.leaflet-marker-icon").click();
		 // var popup = $(".leaflet-popup-content");
		 // expect(popup).toHaveLength(1);
		 // console.log(popup);
      });


      it("initLeafletMaps:validEdit", function() {
          loadFixtures("leaflet-edit.html");
          TDAR.leaflet.initEditableLeafletMaps();
          var options = null;
          var expectedVal = null;
          //var result = TDAR.leaflet.adminUsageStats(options);
          console.log(TDAR.leaflet.initialized());
          //https://www.npmjs.com/package/jasmine-jquery-matchers
          expect(TDAR.leaflet.initialized()).toBeGreaterThan(-1); //fixme: implement this test
          expect($(".leaflet-container")).toHaveLength(1);
 		  // 1 rectangle and 4 place-holders
 		  expect($(".leaflet-clickable")).toHaveLength(1);
		 
       });



});
