/* global describe, it, expect */
describe("D3TreeSpec", function() {

        it("should work when we call init", function() {
            loadFixtures("ontology-tree.html");

            expect($("#d3 svg")).toHaveLength(0);
            expect(TDAR.d3tree).toExist(); 
            TDAR.d3tree.init();
            expect($("#d3 svg")).toHaveLength(1);
        });
    });
