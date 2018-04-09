/* global describe, it, expect */
describe("RepeatrowSpec.js: tests for TDAR.repeatrow methods", function() {
    var $container = null;

    beforeEach(function(){
        setFixtures(''
            + '<div id="repeatrowContainer">'
            + ' <div class="repeat-row" id="testrow_0_">'
            + '     <input id="testId" type="text" name="testRepeatable" value="">'
            + '     <button class="repeat-row-delete " type="button" tabindex="-1">delete</button>'
            + ' </div>'
            + '</div>'
            + '');

        $container = $('#repeatrowContainer');
    });


    it("should work when we call registerRepeatable", function() {
        var selector = "#repeatrowContainer";
        var options = {};

        //sanity check,  we start out w/ 1 input
        expect($j('#repeatrowContainer > .repeat-row')).toHaveLength(1);
        expect($j('#repeatrowContainer input')).toHaveLength(1);
        expect($j('button.addanother')).toHaveLength(0);
        TDAR.repeatrow.registerRepeatable(selector, options);

        //we should have new addanother button
        expect($j('button.addanother')).toHaveLength(1);

        //click add-another and make sure we have two inputs
        $('.addanother').click();
        expect($j('#repeatrowContainer input')).toHaveLength(2);
    });

    it("should work when we call registerDeleteButtons", function() {
        var selector = "#repeatrowContainer";
        var options = {};
        TDAR.repeatrow.registerRepeatable(selector, options);
        var $addButton = $j('.addanother');

        // sanity check: there should be a delete button
        expect($container).toHaveLength(1);
        expect($j('.repeat-row-delete')).toHaveLength(1);

        // click .add-another a few times. now we should have multiple delete buttons
        $addButton.click().click().click();
        expect($j('.repeat-row-delete')).toHaveLength(4);

        //try to delete the last row.  it should be gone.
        expect($j('#testrow_3_')).toHaveLength(1);
        $j('.repeat-row-delete').last().click()
        expect($j('.repeat-row-delete')).toHaveLength(3);
        expect($j('#testrow_3_')).toHaveLength(0);

        //we should never be able to delete the only row in a repeatable
        $j('#testrow_0_').find('input').val('hello');
        expect($j('#testrow_0_').find('input')).toHaveValue('hello');
        $j('#testrow_2_ .repeat-row-delete').click();
        $j('#testrow_1_ .repeat-row-delete').click();
        $j('#testrow_0_ .repeat-row-delete').click();
        expect($j('.repeat-row')).toHaveLength(1);

        // instead of deleting row_0_,  the system should clear field values in the row
        expect($j('#testrow_0_ input')).toHaveValue('');
    });

    xit("should work when we call cloneSection", function() {
        var element = null;
        var appendTo = null;
        var expectedVal = null;

        //var result = TDAR.repeatrow.cloneSection(element, appendTo);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call deleteRow", function() {
        var elem = null;
        var expectedVal = null;

        //var result = TDAR.repeatrow.deleteRow(elem);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call clearInputs", function() {
        var $element = null;
        var expectedVal = null;

        //var result = TDAR.repeatrow.clearInputs($element);
        expect(true).toBe(false); //fixme: implement this test
    });

});
