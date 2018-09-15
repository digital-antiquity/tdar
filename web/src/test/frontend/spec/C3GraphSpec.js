/* global describe, it, expect */
describe("C3GraphSpec.js: C3GraphSpec", function() {

    beforeEach(function(){
        jasmine.getFixtures().fixturesPath  =  "base/src/test/frontend/fixtures/";
    });
    
    it("should work when we call initC3Graph", function() {
       var options = null;
       var expectedVal = null;

       //var result = TDAR.leaflet.adminUsageStats(options);
       expect(TDAR.c3graph).toExist(); 
    });
    
    it("initC3Graph:pieChart", function() {
        loadFixtures("c3/piechart.html");

        TDAR.c3graph.initPieChart();
        var options = null;
        var expectedVal = null;

        expect($("#resourceTypeChart")).toHaveLength(1);
        expect($("#resourceTypeChart")).toHaveClass("c3");

        expect($(".c3-arcs-Document")).toHaveLength(1);
        expect($(".c3-arcs-Project")).toHaveLength(1);
        expect($(".c3-arcs-Image")).toHaveLength(0);
     });

    
    it("initC3Graph:barChart", function() {
        loadFixtures("c3/barchart.html");

        TDAR.c3graph.initBarChart();
        var options = null;
        var expectedVal = null;

        expect($("#resourceBarGraph")).toHaveLength(1);
        expect($("#resourceBarGraph")).toHaveClass("c3");

        expect($(".c3-bar")).toHaveLength(7);
        expect($(".c3-axis-x").text()).toContain("Document");
     });


    it("initC3Graph:areaChart", function() {
        loadFixtures("c3/areachart.html");

        TDAR.c3graph.initAreaGraph();
        var options = null;
        var expectedVal = null;

        expect($("#timelineGraph")).toHaveLength(1);
        expect($("#timelineGraph")).toHaveClass("c3");

        // points
        expect($(".c3-circle")).toHaveLength(18);
        expect($(".c3-lines")).toHaveLength(1);
        // combination of years
        expect($(".c3-axis-x").text()).toContain("199020002010");
     });

    
    it("initC3Graph:tableLineChart", function() {
        loadFixtures("c3/linegraph-table.html");

        TDAR.c3graph.initLineGraph();
        var options = null;
        var expectedVal = null;

        expect($("#graphuserstats")).toHaveLength(1);
        expect($("#graphuserstats")).toHaveClass("c3");

        // points
        expect($(".c3-circle")).toHaveLength(28);
        expect($(".c3-lines")).toHaveLength(2);
        expect($(".c3-legend-item")).toHaveLength(2);
        expect($(".c3-legend-item").text()).toContain("# of Contributors");
        expect($(".c3-legend-item").text()).toContain("# of Users");
//        // combination of years
//        expect($(".c3-axis-x").text()).toContain("199020002010");
     });

    
});