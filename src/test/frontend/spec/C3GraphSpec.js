/* global describe, it, expect */
describe("C3GraphSpec", function() {  

    it("should work when we call initC3Graph", function() {
       var options = null;
       var expectedVal = null;

       //var result = TDAR.leaflet.adminUsageStats(options);
       expect(TDAR.c3graph).toExist(); 
    });
});