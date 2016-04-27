/* global jasmine, describe, it, expect, loadFixtures, $j, $, beforeEach, afterEach, TDAR */
describe("TDAR.validate: edit page tests", function () {
    "use strict";

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
        console.log("prepare-date-test");
        var $form = $('<form class="tdarvalidate" data-validate-method="initBasicForm">' + readFixtures('coverage-dates.html') + '</form>');
        var sel = $form.find('select')[0];
        setFixtures($form);
        var validator = TDAR.validate.initForm($form);

        //validation: everything blank - no errors 
        $(sel).val('NONE');
        $(sel).change();
//        TDAR.common.prepareDateFields(sel);
        $form.valid();
        expect(validator.errorList.length).toBe(0);
    });


    it("should work when we call prepareDateFields (calendar missing)", function () {
        console.log("prepare-date-test");
        var $form = $('<form class="tdarvalidate" data-validate-method="initBasicForm">' + readFixtures('coverage-dates.html') + '</form>');
        var sel = $form.find('select')[0];
        setFixtures($form);
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
        console.log("prepare-date-test");
        var $form = $('<form class="tdarvalidate" data-validate-method="initBasicForm">' + readFixtures('coverage-dates.html') + '</form>');
        var sel = $form.find('select')[0];
        setFixtures($form);
        var validator = TDAR.validate.initForm($form);

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
        console.log("prepare-date-test");
        var $form = $('<form class="tdarvalidate" data-validate-method="initBasicForm">' + readFixtures('coverage-dates.html') + '</form>');
        var sel = $form.find('select')[0];
        setFixtures($form);
        var validator = TDAR.validate.initForm($form);

        //validation: ...but it's a valid radiocarbon date
        console.log($(sel).val());
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

    it("should apply coding-sheet & ontology validation rules", function() {
        setFixtures('<form class="tdarvalidate" data-validate-method="initBasicForm">' + readFixtures('supporting-resource-upload.html') + '</form>');
        var validator = $('form').validate();
        TDAR.common.setupSupportingResourceForm(1, 'coding-sheet');
        $('form').valid()
        expect(validator.errorList).toHaveLength(0);

        TDAR.common.setupSupportingResourceForm(0, 'coding-sheet');
        $('form').valid()
        expect(validator.errorList).toHaveLength(2);
    });

    it("should add image extension validation to profile image uploads validateProfileImage", function () {
        var $form = $('<form class="tdarvalidate" data-validate-method="initBasicForm"><input type="file" name="fileupload" class="profileImage"></form>');
        setFixtures($form);
        var validator = TDAR.validate.initForm($form);
        expect(Object.keys($('input[type=file]').rules())).toContain('extension');
    });
});