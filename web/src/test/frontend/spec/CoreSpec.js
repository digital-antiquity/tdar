/* global describe, it */
describe("TDAR root functions", function() {


	it("should build correct URI striings", function() {

		expect(TDAR.uri("taco.js")).toBe(window.location.origin + "/taco.js")
	})


});