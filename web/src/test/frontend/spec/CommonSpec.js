/* global jasmine, describe, it, expect, loadFixtures, $j, $, beforeEach, afterEach */
describe("TDAR.common: edit page tests", function () {

    it("initializes the edit page", function () {
        var form = null;
        var props = {
            formSelector: "#metadataForm",
            includeInheritance: true,
            acceptFileTypes: /\.(pdf|doc|docx|rtf|txt)$/i,
            multipleUpload: true,
            validExtensions: "pdf|doc|docx|rtf|txt",
            validExtensionsWarning: "Please enter a valid file (pdf, doc, docx, rtf, txt)",
            ableToUpload: true,
            dataTableEnabled: false
        };

        loadFixtures("document-add-form.html", "fileupload-templates.html");
        expect($j("#template-upload")).toHaveLength(1);
        expect($j("#metadataForm")).toHaveLength(1);
        form = document.getElementById('metadataForm');
        var result = TDAR.common.initEditPage(form, props);

    });

    it("initializes form validation", function () {
        loadFixtures("document-add-form.html", "fileupload-templates.html");
        var $mapdiv = $j(jasmine.getFixtures().read("map-div.html"))

        //add a mapdiv to implicitly load gmap api and perform init
        $j("#divSpatialInformation").append($mapdiv);

        expect($j('#divSpatialInformation .google-map')).toHaveLength(1);
        form = document.getElementById('metadataForm');
        var result = TDAR.common.initFormValidation(form);
    });
});

describe("TDAR.common: miscellaneaous tests", function () {
    it("should work when we call applyTreeviews", function () {

        loadFixtures('treeview.html');
        //before calling treeview, none of the lists are decorated
        expect($j(".hitarea, .treeview")).toHaveLength(0);

        var result = TDAR.common.applyTreeviews();

        //after treeview(), parent nodes will have .hitarea and the top-level node will have .treeview class
        expect($j(".hitarea")).toHaveLength(1);
        expect($j(".treeview")).toHaveLength(1);
    });

    it("initializes a the search box on view page", function () {
        loadFixtures('searchheader.html');
        var result = TDAR.common.initializeView();
        $expect('.active').not.toBeInDOM();
        $j(".searchbox").focus();
        $expect('.active').toBeInDOM();
    });

    //fixme: this  passes on my  osx,  windows instances but fails on bamboo (due to timeout). Skipping for now.  
    // I suspect that ajax requests to the google api/tile server are failing (or are blocked?) on build.tdar.org for some reason.
    // todo: narrow the scope of the test by performing an async load of a locally hosted file (e.g. notification.gif )
    xit("initializes a a map on view page", function (done) {
        loadFixtures('searchheader.html', 'map-div.html');
        var result = TDAR.common.initializeView();
        var mapInitialized = false;
        var promise = TDAR.maps.mapPromise;
        promise
            .done(function(api){
                expect(api).toBeDefined();
                done();})
            .fail(function(err){
                fail('Received error message from mapPromise:' + err);
            });
    });

    it("should register the validation form when we call initRegformValidation", function () {
        loadFixtures("registration-form.html");
        var form = document.getElementById('accountForm')

        var result = TDAR.common.initRegformValidation(form);
        var validator = $(form).validate();
        //before we validate we should have no errors
        expect(validator.errorList).toHaveLength(0);

        //since all fields are blank, we should have at least a couple validation errors
        $(form).valid();
        expect(validator.errorList.length).toBeGreaterThan(0);
    });

    it("should work when we call determineResponsiveClass", function () {
        var f =  TDAR.common.determineResponsiveClass.bind(TDAR.common);
        expect(f(1200 + 1)).toBe('responsive-large-desktop');
        expect(f(979 + 1)).toBe('responsive-desktop');
        expect(f(767 + 1)).toBe('responsive-tablet');
        expect(f(500 + 1)).toBe('responsive-phone');
        expect(f(1 + 1)).toBe('responsive-phone-portrait');
    });

    it("should work when we call ellipsify", function () {
        var text = "It's Monty Pythons Flying circus";
        var n = 6;
        var useWordBoundary = false;
        expect(TDAR.ellipsify(text, n, useWordBoundary)).toBe("It's ..."); 
        var useWordBoundary = true;
        expect(TDAR.ellipsify(text, n, useWordBoundary)).toBe("It's...");
    });

    //note use of $j and $ is intentional
    it("should work when we call populateTarget", function () {
        var obj = {id:'12345', title:'a parent title'};

        var $container = $j('<div id="adhocTarget"></div>');
        $container.append($j('<input type="hidden" name="parentId" value="">'
            + '<input type="text" name="parentTitle" value="">'));
        setFixtures($container);
        //sanity check: did we really add this to dom?
        $expect('input').toHaveLength(2);

        $('body').data('adhocTarget', $container[0]);
        expect($container).toHaveLength(1);
        TDAR.common.populateTarget(obj);

        expect($container.find('[type=hidden]').val()).toBe(obj.id);
        expect($container.find('[type=text]').val()).toBe(obj.title);
    });

    it("shouldn't leak data when run our populateTarget test", function() {
        expect($j('body').data("adhocTarget")).not.toBeDefined();
    });

    it("should work when we call prepareDateFields", function () {
        var $form = $('<form>' + readFixtures('coverage-dates.html') + '</form>');
        var sel = $form.find('select')[0];
        var validator = $form.validate();
        setFixtures($form);

        //validation: everything blank - no errors 
        $(sel).val('NONE');
        TDAR.common.prepareDateFields(sel);
        $form.valid();
        expect(validator.errorList.length).toBe(0);
        
        //validation: coverage date incomplete 
        $(sel).val('CALENDAR_DATE');
        TDAR.common.prepareDateFields(sel);
        $form.find('.coverageStartYear').val('2001');
        $form.valid();
        expect(validator.errorList.length).toBeGreaterThan(0);

        //validation: 2001-1999 is an invalid calendar date...
        $(sel).val('CALENDAR_DATE');
        TDAR.common.prepareDateFields(sel);
        $form.find('.coverageStartYear').val('2001');
        $form.find('.coverageEndYear').val('1999');
        $form.valid();
        expect(validator.errorList.length).toBeGreaterThan(0);

        //validation: ...but it's a valid radiocarbon date
        $(sel).val('RADIOCARBON_DATE');
        TDAR.common.prepareDateFields(sel);
        $form.valid();
        expect(validator.errorList.length).toBe(0);
    });

    it("should set setAdhocTarget", function () {

        var $container = $j('<div id="adhocTarget"></div>');
        $container.append(
            $j('<input type="hidden" id="hiddenParentId" name="parentId" value="">'
            + '<input type="text" name="parentTitle" value="">'));
        setFixtures($container);

        var selector = "#adhocTarget"
        TDAR.common.setAdhocTarget($('#hiddenParentId')[0], selector )

        expect($('body').data()).toBeDefined()
        expect($('body').data('adhocTarget').html()).toBe($container.html())
    });

    describe("TDAR.common functions that utilize ajax", function() {

        beforeEach(function() {
            jasmine.Ajax.install();
        });

        afterEach(function() {
            jasmine.Ajax.uninstall();
        });

        it("updates th subcategory options when you select a category", function () {

            var $categoryIdSelect = $j('<select id="cat"></select>');
            var $subCategoryIdSelect = $j('<select id="subcat"></select>');
            setFixtures($categoryIdSelect);
            appendSetFixtures($subCategoryIdSelect);

            //TDAR.common.changeSubcategory
            $expect('select').toHaveLength(2);
        });
    });


    xit("should work when we call registerDownload", function () {
        var url = null;
        var tdarId = null;
        var expectedVal = null;

        //var result = TDAR.common.registerDownload(url, tdarId);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call registerShare", function () {
        var service = null;
        var url = null;
        var tdarId = null;
        var expectedVal = null;

        //var result = TDAR.common.registerShare(service, url, tdarId);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call gaevent", function () {
        var expectedVal = null;

        //var result = TDAR.common.gaevent();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call outboundLink", function () {
        var elem = null;
        var expectedVal = null;

        //var result = TDAR.common.outboundLink(elem);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call setupSupportingResourceForm", function () {
        var totalNumberOfFiles = null;
        var rtype = null;
        var expectedVal = null;

        //var result = TDAR.common.setupSupportingResourceForm(totalNumberOfFiles, rtype);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call switchType", function () {
        var radio = null;
        var container = null;
        var expectedVal = null;

        //var result = TDAR.common.switchType(radio, container);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call setupDocumentEditForm", function () {
        var expectedVal = null;

        //var result = TDAR.common.setupDocumentEditForm();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call sessionTimeoutWarning", function () {
        var expectedVal = null;

        //var result = TDAR.common.sessionTimeoutWarning();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call applyBookmarks", function () {
        var expectedVal = null;

        //var result = TDAR.common.applyBookmarks();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call sprintf", function () {
        var expectedVal = null;

        //var result = TDAR.common.sprintf();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call htmlDecode", function () {
        var value = null;
        var expectedVal = null;

        //var result = TDAR.common.htmlDecode(value);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call htmlEncode", function () {
        var value = null;
        var expectedVal = null;

        //var result = TDAR.common.htmlEncode(value);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call htmlDoubleEncode", function () {
        var value = null;
        var expectedVal = null;

        //var result = TDAR.common.htmlDoubleEncode(value);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call applyWatermarks", function () {
        var context = null;
        var expectedVal = null;

        //var result = TDAR.common.applyWatermarks(context);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call coordinatesCheckboxClicked", function () {
        var elem = null;
        var expectedVal = null;

        //var result = TDAR.common.coordinatesCheckboxClicked(elem);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call refreshInputDisplay", function () {
        var expectedVal = null;

        //var result = TDAR.common.refreshInputDisplay();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call tmpl", function () {
        var a = null;
        var c = null;
        var expectedVal = null;

        //var result = TDAR.common.tmpl(a, c);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call validateProfileImage", function () {
        var expectedVal = null;

        //var result = TDAR.common.validateProfileImage();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call collectionTreeview", function () {
        var expectedVal = null;

        //var result = TDAR.common.collectionTreeview();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call humanFileSize", function () {
        var bytes = null;
        var si = null;
        var expectedVal = null;

        //var result = TDAR.common.humanFileSize(bytes, si);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call initImageGallery", function () {
        var expectedVal = null;

        //var result = TDAR.common.initImageGallery();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call formatNumber", function () {
        var num = null;
        var expectedVal = null;

        //var result = TDAR.common.formatNumber(num);
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call registerAjaxStatusContainer", function () {
        var expectedVal = null;

        //var result = TDAR.common.registerAjaxStatusContainer();
        expect(true).toBe(false); //fixme: implement this test
    });

    xit("should work when we call suppressKeypressFormSubmissions", function () {
        var $form = null;
        var expectedVal = null;

        //var result = TDAR.common.suppressKeypressFormSubmissions($form);
        expect(true).toBe(false); //fixme: implement this test
    });

});
