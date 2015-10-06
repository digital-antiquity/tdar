/* global describe, it, expect */
describe("tests for TDAR.internalEmailForm methods", function() {  

xit("test init", function() {
   var expectedVal = null;
   loadFixtures("emailspec.html");

   expect($("#email-form:visible")).toHaveLength(0);
   expect(TDAR.internalEmailForm).toExist(); 
   TDAR.internalEmailForm.init();
   $("#emailButton").trigger("click");
   expect($("#email-form:visible")).toHaveLength(1);
   // expect($("#d3 svg")).toHaveLength(1);

   //var result = TDAR.internalEmailForm.inxit();
   // expect(true).toBe(false); //fixme: implement this test
});

});
