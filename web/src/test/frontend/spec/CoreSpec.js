/* global describe, it */
describe("CoreSpec.js: TDAR root functions", function() {


    it("TDAR.uri should build correct URI strings", function() {

        expect(TDAR.uri("taco.js")).toBe(window.location.origin + "/taco.js")
    })

    it("TDAR.assetsUri should build correct URI strings (when not enabled)", function() {

        expect(TDAR.assetsUri("taco.js")).toBe(window.location.origin + "/taco.js")
    })


});