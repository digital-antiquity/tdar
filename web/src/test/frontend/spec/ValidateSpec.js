/* global jasmine, describe, it, expect, loadFixtures, $j, $, beforeEach, afterEach, TDAR */
const TDAR = require("JS/tdar.master");

describe("ValidateSpec.js: TDAR.validate tests", function () {
    "use strict";
    
    beforeEach(function(){
        jasmine.getFixtures().fixturesPath  =  "base/src/test/frontend/fixtures/";
    });

    // FIXME: this does nothing in the leaflet world
    it("initializes form validation", function () {
        loadFixtures("document-add-form.html", "fileupload-templates.html");
        var $mapdiv = $j(jasmine.getFixtures().read("map-div.html"))

        //add a mapdiv to implicitly load gmap api and perform init
        $j("#divSpatialInformation").append($mapdiv);

        expect($j('#divSpatialInformation .google-map')).toHaveLength(1);
        var form = document.getElementById('metadataForm');
        var validator = TDAR.validate.initForm($(form));
    });

    it("should register the validation form when we call initRegformValidation", function () {
        loadFixtures("registration-form.html");
        var form = document.getElementById('accountForm');

        var validator = TDAR.validate.initForm($(form));
        //before we validate we should have no errors
        expect(validator.errorList).toHaveLength(0);

        //since all fields are blank, we should have at least a couple validation errors
        $(form).valid();
        expect(validator.errorList.length).toBeGreaterThan(0);
    });


    it("should work when we call prepareDateFields (NONE)", function () {
        console.log("prepare-date-test (none)");
        loadFixtures("coverage-dates.html");
        var $form = $("#coverage");
        var sel = $form.find('select')[0];
        var validator = TDAR.validate.initForm($form);
        
        //validation: everything blank - no errors 
        $(sel).val('NONE');
        $(sel).change();
//        TDAR.common.prepareDateFields(sel);
        $form.valid();
        expect(validator.errorList.length).toBe(0);
    });


    it("should work when we call prepareDateFields (calendar missing)", function () {
        console.log("prepare-date-test calendar(missing value)");
        loadFixtures("coverage-dates.html");
        var $form = $("#coverage");
        var sel = $form.find('select')[0];
        var validator = TDAR.validate.initForm($form);

        //validation: coverage date incomplete 
        $(sel).val('CALENDAR_DATE');
        $(sel).change();
//        TDAR.common.prepareDateFields(sel);
        $form.find('.coverageStartYear').val('2001');
        $form.valid();
        expect(validator.errorList.length).toBeGreaterThan(0);
    });

    it("should work when we call prepareDateFields (calendar wrong order)", function () {
        console.log("prepare-date-test calendar(invalid)");
        loadFixtures("coverage-dates.html");
        var $form = $("#coverage");
        var validator = TDAR.validate.initForm($form);
        var sel = $form.find('select')[0];

        //validation: 2001-1999 is an invalid calendar date...
        $(sel).val('CALENDAR_DATE');
        $(sel).change();
//        TDAR.common.prepareDateFields(sel);
        $form.find('.coverageStartYear').val('2001');
        $form.find('.coverageEndYear').val('1999');
        $form.valid();
        expect(validator.errorList.length).toBeGreaterThan(0);
    });
    
    it("should work when we call prepareDateFields (RC valid)", function () {
        console.log("prepare-date-test rc-valid");
        loadFixtures("coverage-dates.html");
        var $form = $("#coverage");
        var sel = $form.find('select')[0];
        setFixtures($form);
        var validator = TDAR.validate.initForm($form);

        //validation: ...but it's a valid radiocarbon date
        //console.log($(sel).val());
        $(sel).val('RADIOCARBON_DATE');
        $(sel).change();
        $form.find('.coverageStartYear').val('2001');
        $form.find('.coverageEndYear').val('1999');
        $form.valid();
        console.log(validator.errorList);
        expect(validator.errorList.length).toBe(0);
    });




    it("should initialize the coding-sheet / ontology  validation rules", function () {
        var form = $j('<form class="tdarvalidate" data-validate-method="initBasicForm"></form>');
        form.append(readFixtures('supporting-resource-upload.html'));
        setFixtures(form);
        var validator = TDAR.validate.initForm(form);
        TDAR.common.setupSupportingResourceForm(1, 'coding-sheet');
    });

    it("should apply coding-sheet & ontology validation rules (1 file - valid)", function() {
        loadFixtures("supporting-resource-upload.html");
        var $form = $("#supporting");
        $form.data("total-files",1);
        var validator = TDAR.validate.initForm($form);
        TDAR.common.setupSupportingResourceForm(1, 'coding-sheet');
        $form.valid();
        expect(validator.errorList).toHaveLength(0);
    });
    
    it("should apply coding-sheet & ontology validation rules (0 files - invalid)", function() {
        console.log("test coding-sheet-upload invalid");
        loadFixtures("supporting-resource-upload.html");
        var $form = $("#supporting");
        var validator = TDAR.validate.initForm($form);
        $form.valid();
        console.log(validator.errorList);
        expect(validator.errorList).toHaveLength(2);
    });

    it("should add image extension validation to profile image uploads validateProfileImage", function () {
        loadFixtures("profile-image.html");
        var $form = $("#profile");
        $form.find('input').val('foo.bar');
        var validator = $form.validate({debug:false, ignore:"none"});
        expect($form.valid()).toBeFalsy();
        expect(validator.errorList.length).toBe(1);
        expect(validator.errorList[0].message).toContain("Please upload a file with one of the following extensions");
    });
});