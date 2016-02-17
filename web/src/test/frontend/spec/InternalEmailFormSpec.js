/* global describe, it, expect */
describe("tests for TDAR.internalEmailForm methods", function() {  
    beforeEach(function(){
       loadFixtures("request-correction-form.html");
    });

    it("test init", function(done) {
        var expectedVal = null;
        
        expect($("#email-form:visible")).toHaveLength(0);
        //expect(TDAR.internalEmailForm).toExist(); 
        TDAR.internalEmailForm.init();
        expect($('#emailButton')).toHaveLength(1);
        expect($('#emailButton')).toBeVisible();
        
        // Bootstrap modals have a fadein transition (i.e. they are async) so we need to wait for the custom 
        //  'shown' event.  Then we can continue our test.  
        $("#email-form").on("shown", function() {
            expect($("#email-form")).toBeVisible();
            done();
        });
        
        // important: define the handler *before* clicking the button 
        $("#emailButton").click();
    }, 
    
    //if the test isn't over by 2.5s, assume it failed
    2500);

});
