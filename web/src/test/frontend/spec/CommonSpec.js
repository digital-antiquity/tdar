/* global jasmine, describe, it, expect, loadFixtures, $j, $, beforeEach, afterEach, TDAR */
describe("CommonSpec.js: edit page tests", function () {
    "use strict";

    var formProps = {
        formSelector: "#metadataForm",
        includeInheritance: true,
        acceptFileTypes: /\.(pdf|doc|docx|rtf|txt)$/i,
        multipleUpload: true,
        validExtensions: "pdf|doc|docx|rtf|txt",
        validExtensionsWarning: "Please enter a valid file (pdf, doc, docx, rtf, txt)",
        ableToUpload: true,
        dataTableEnabled: false
    };

    it("initializes the edit page", function () {

        loadFixtures("document-add-form.html", "fileupload-templates.html");
        var form = document.getElementById('metadataForm');
        expect($j("#template-upload")).toHaveLength(1);
        expect($j("#metadataForm")).toHaveLength(1);
        var result = TDAR.common.initEditPage(form, formProps);

    });

    it("form handler should trim fields with .trim class", function() {
        loadFixtures("document-add-form.html");
        TDAR.common.initEditPage($('#metadataForm')[0], formProps);
        expect($('#resourceRegistrationTitle')).not.toHaveClass('trim');
        var originalTitle = '       title     ';
        var trimmedTitle = 'title';

        $('#resourceRegistrationTitle').val(originalTitle);
        $('#metadataForm').submit();
        expect($('#resourceRegistrationTitle')).toHaveValue(originalTitle);

        $('#resourceRegistrationTitle').addClass('trim');
        $('#metadataForm').submit();
        expect($('#resourceRegistrationTitle')).toHaveValue(trimmedTitle);

    });


    describe("TDAR.common functions that utilize ajax", function() {
        "use strict";

        beforeEach(function() {
            jasmine.Ajax.install();
        });

        afterEach(function() {
            jasmine.Ajax.uninstall();
        });


        it("updates the subcategory options when you select a category", function () {
            //create a simple cat/subcat form.
            var $categoryIdSelect = $j('<select id="cat"><option>foo</option></select>');
            var $subCategoryIdSelect = $j('<select id="subcat"></select>');

            //user selects the 'foo' category
            setFixtures($categoryIdSelect);
            $categoryIdSelect.val('foo');
            appendSetFixtures($subCategoryIdSelect);
            $expect('select').toHaveLength(2);
            TDAR.common.changeSubcategory($categoryIdSelect, $subCategoryIdSelect);

            //server responsds with array containing the 'bar' subcategory
            jasmine.Ajax.requests.mostRecent().respondWith({
                status: 200,
                contentType: 'text/json',
                responseText: JSON.stringify([{id:123, label:'bar'}])
            });

            expect($subCategoryIdSelect.val()).toBe('123');
            expect($subCategoryIdSelect.find('option').text()).toBe('bar');
        });

        it("registers the global ajax status indicator", function () {
            var cb = function(arg){};
            setFixtures('<div id="ajaxIndicator"><strong> </strong><span> </span>');
            var $ajaxIndicator = $('#ajaxIndicator')
            var $label = $ajaxIndicator.find('strong');
            var $message = $ajaxIndicator.find('span');
            TDAR.common.registerAjaxStatusContainer();
            var ajaxSettings = {
                url: '/foo/bar',
                dataType: "json",
                success: cb,
                error: cb,
                complete: cb,
                waitMessage: 'waitmsg',
                doneMessage: 'done',
                failMessage: 'error'

            };

            //indicator should show wait message while request is in flight
            $.ajax(ajaxSettings);
            expect($message).toHaveText('waitmsg');

            //after respose, indicator shows 'done' message'
            jasmine.Ajax.requests.mostRecent().respondWith( {
                status: 200,
                contentType: 'text/json',
                responseText: JSON.stringify([{id:123, label:'bar'}])
            });
            expect($message).toHaveText('done');

            //after an error the indicator should show an error message
            //FIXME:  responseError() doesn't appear to work in karma-jasmine-ajax using latest jquery.  figure out later
            //$.ajax(ajaxSettings);
            //jasmine.Ajax.requests.mostRecent().responseError();
            //expect($message).toHaveText('error');
        });
    });


    describe("TDAR.common: tests that override Modernizer", function() {
        var placeholder;
    
        beforeEach(function(){
            var placeholder = Modernizr.input.placeholder;
        });
    
        afterEach(function(){
            Modernizr.input.placeholder = placeholder
        });
    
        it("applies watermarks using jquery when not supported by browser", function () {
            Modernizr.input.placeholder = false;
            setFixtures('<input type="text" placeholder="howdy" value="" name="field1">');
            spyOn($.fn, 'watermark');
            TDAR.common.applyWatermarks();
            expect($.fn.watermark).toHaveBeenCalledWith('howdy');
        });
    });

    describe("TDAR.common: miscellaneaous tests", function () {
        "use strict";
    
        beforeEach(function(){
            //we don't include analytics.js in our fixtures,   so fake it.
            window.ga = jasmine.createSpy();
        });
    
        afterEach(function(){
            if(!!window.ga) {
                delete window.ga;
            }
        });
    
    
        it("should work when we call applyTreeviews", function () {
    
            loadFixtures('treeview.html');
            //before calling treeview, none of the lists are decorated
            expect($j(".hitarea, .treeview")).toHaveLength(0);
    
            var result = TDAR.common.applyTreeviews();
    
            //after treeview(), parent nodes will have .hitarea and the top-level node will have .treeview class
            expect($j(".hitarea")).toHaveLength(1);
            expect($j(".treeview")).toHaveLength(1);
        });
    
        it("should display the 'search within this collectin' checkbox when user focuses on the search textbox on a collection view page", function () {
            loadFixtures('searchheader.html');
            var result = TDAR.common.initializeView();
            $expect('#divSearchContext.active').not.toBeInDOM();
    
            $(".searchbox").focus(); //workaround for ff issue. test fails unless focus event triggered twice
            $(".searchbox").focus();
            $expect('#divSearchContext.active').toBeInDOM();
            expect($j('#divSearchContext')).toHaveClass('active');
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
    
        it("should set setAdhocTarget", function () {
    
            var $container = $j('<div id="adhocTarget"></div>');
            $container.append(
                $j('<input type="hidden" id="hiddenParentId" name="parentId" value="">'
                + '<input type="text" name="parentTitle" value="">'));
            setFixtures($container);
    
            var selector = "#adhocTarget"
            TDAR.common.setAdhocTarget($('#hiddenParentId')[0], selector );
    
            expect($('body').data()).toBeDefined();
            expect($('body').data('adhocTarget').html()).toBe($container.html())
        });
    
        it("registers download links", function () {
            var url = 'http://insanity.today';
            var tdarId = 110102;
            var expectedVal = null;

            var result = TDAR.common.registerDownload(url, tdarId);
            expect(window.ga).toHaveBeenCalledWith("send", "event", "download", "download", url, tdarId);

        });
    
        it("registers 'share' buttons", function () {
            var service = 'instagram';
            var url = 'http://insane.solutions';
            var tdarId = 1234;
            //ignore '_trackEvent failed'; we only care that the message is put on the queue
            var result = TDAR.common.registerShare(service, url, tdarId);
            expect(window.ga).toHaveBeenCalledWith("send", "event", service, "shared", url, tdarId);
        });
    

        it("registers outbound link clicks", function () {
            var elem = document.createElement('A');
            elem.href = 'http://www.cnn.com'

            //ignore '_trackEvent failed'; we only care that the message is put on the queue
            var result = TDAR.common.outboundLink(elem);
            expect(window.ga).toHaveBeenCalledWith("send", "event", "outboundLink", "clicked", elem.href);

        });


        it("field visibility changes depending on documenttype", function () {
            var $form = $('<form></form>');
            var $container = $('<div id="divContainer"></div>');
            $form
                .append('<input id="fakeRadio" type="hidden" name="documentType" value="">')
                .append($container);
            $container
                .append('<div class="typeToggle type1">type1</div>')
                .append('<div class="typeToggle type2">type2</div>')
                .append('<div class="typeToggle type3">type3</div>')
                .append('<div class="typeToggle type4 type5 type6">type456</div>');
            setFixtures($form);
            var $fakeRadio = $('#fakeRadio');
    
            for(var i = 1; i <= 6; i++) {
                $fakeRadio.val('type' + i);
                TDAR.common.switchType($fakeRadio, $container);
                if(i < 4) {
                    //if section doesn't have a class that corresponds to selected doctype that section should be invisible.
                    expect($('.typeToggle:visible').length).toBe(1);
                    expect($('.typeToggle:not(visible)').length).toBe(4);
    
                } else {
                    //doctype fields are not mutually exclusive.  a section may be visible for more than one doctype
                    expect($('.typeToggle:visible')).toHaveClass('type4');
                    expect($('.typeToggle:visible')).toHaveClass('type5');
                    expect($('.typeToggle:visible')).toHaveClass('type6');
                }
            }
        });
    
        it("sets up the documentType switcher", function () {
            var $form = $('<form class="doctype" id="citationInformation"></form>');
            var $container = $('<div id="divContainer"></div>');
            $form
                .append('<input type="radio" class="" name="dts" value="type1">opt1 ' + 
                    '<input type="radio" class="" name="dts" value="type2" checked="checked">')
                .append($container);
            $container
                .append('<div class="doctypeToggle type1">type1<input type="hidden" value="THISTEXTWILLBEBLANK" /><input type="radio" value="CHECKED" checked="CHECKED" /></div>')
                .append('<div class="doctypeToggle type2">type2</div>');
            setFixtures($form);
    
            TDAR.common.setupDocumentEditForm();
            expect($('.doctypeToggle:visible')).toHaveLength(1);
            expect($('.doctypeToggle:visible')).toHaveText('type2');
            
            //Verify the hidden inputs clear.
            $(".doctypeToggle:hidden input").each(function(){
        		var el = $(this);
        		if($(this).attr("type")=="radio"){
        			expect($(this).attr("checked")).toBeFalsy();
        		}
        		else{
        			expect($(this).val()).toBe("");
        		}
    		}
        	);
            
            
        });
    
    
    
        it("expands template strings with sprintf()", function () {
    
            var format = "Hello {0}, good {1}! How are things in {0}-land?"
            var result = TDAR.common.sprintf(format, 'bob', 'morning');
            expect(result).toBe("Hello bob, good morning! How are things in bob-land?")
    
        });
    
        it("decodes html strings", function () {
            expect(TDAR.common.htmlDecode('&amp;')).toBe('&'); 
        });
    
        it("encode strings into html", function () {
            expect(TDAR.common.htmlEncode('&')).toBe('&amp;'); 
        });
    
        it("should work when we call htmlDoubleEncode", function () {
            expect(TDAR.common.htmlDoubleEncode('&')).toBe('&amp;amp;');
        });
    
        it("should work when we call coordinatesCheckboxClicked", function () {
            setFixtures('<input type="checkbox" id="cb" name="cb">' + 
                '<div id="explicitCoordinatesDiv">hi</div>');
            expect($('#explicitCoordinatesDiv')).toBeVisible();
            TDAR.common.coordinatesCheckboxClicked($('#cb')[0]);
            expect($('#explicitCoordinatesDiv')).not.toBeVisible();
        });
    
        it("initializes image galleries", function() {
            setFixtures('<div class="image-carousel"> '
                + '<img class="thumbnailLink" data-src="/images/add.gif" data-url="/images/book.png" data-access-rights="awesome"> '
                + '<img id="bigImage">'
                + '<span id="confidentialLabel"> </span>'
                + '</div>')
            TDAR.common.initImageGallery();
            expect($j('img').get(0).src).toContain('/images/add.gif');
    
            //clicking on a thumbnail should update the featured image and label
            $(".thumbnailLink").click();
            expect($j('#confidentialLabel')).toHaveText('This file is awesome, but you have rights to see it');
            expect($j('#bigImage').get(0).src).toContain('/images/book.png');
    
        });
    
        it("should work when we call refreshInputDisplay", function () {
            var $form = $('<form><input type="text" id="inputMethodId">'  
                + '<div id="uploadFileDiv">file</div>' 
                + '<div id="textInputDiv">text</div>'
                + '</form>');
            setFixtures($form);
            $('#inputMethodId').val('file');
            TDAR.common.refreshInputDisplay();
            expect($('#uploadFileDiv')).toBeVisible();
            expect($('#textInputDiv')).not.toBeVisible();
            
            $('#inputMethodId').val('text');
            TDAR.common.refreshInputDisplay();
            expect($('#uploadFileDiv')).not.toBeVisible();
            expect($('#textInputDiv')).toBeVisible();
        });
    
    
        it("initializes collection lists  when we call collectionTreeview", function () {
            loadFixtures('treeview.html');
            //before calling treeview, none of the lists are decorated
            expect($j(".hitarea, .treeview")).toHaveLength(0);
    
            TDAR.common.collectionTreeview()
    
            //after treeview(), parent nodes will have .hitarea and the top-level node will have .treeview class
            expect($j(".hitarea")).toHaveLength(1);
            expect($j(".treeview")).toHaveLength(1);
    
        });
    
        it("displays friendly file sizes ", function () {
            expect(TDAR.common.humanFileSize(5)).toBe('5 B');
            ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'].forEach(function(unit, i) {
                var bytes = 1 * Math.pow(10, 3 * (i+1));
                expect(TDAR.common.humanFileSize(bytes)).toBe('1.0 ' + unit);
                expect(TDAR.common.humanFileSize(bytes * 1.33333)).toBe('1.3 ' + unit);
            });
        });
    
        it("formats large & fractional numbers", function () {
            var formatNumber = TDAR.common.formatNumber.bind(TDAR.common);
    
            expect(formatNumber( 1)).toBe('1'); 
            expect(formatNumber( 1.1)).toBe('1.10');
            expect(formatNumber( 10/3)).toBe('3.33');
            expect(formatNumber( 1234567)).toBe('1,234,567');
        });
    
        it("compares arrays",  function(){
            //empty arrays are always equal
            expect($.compareArray([], [])).toBe(true);
    
            //comparison ignoring order
            expect($.compareArray([1, 2, 3], [3, 2, 1], true)).toBe(true);
    
            //comparing order 
            expect($.compareArray([1, 2, 3], [3, 2, 1], false)).toBe(false);
    
        });
    
        it("doesn't modify input arguments when comparing arrays", function(){
            var array1 = [1,2,3];
            var array2 = [3,2,1];
            var array2Copy = [3,2,1];
            $.compareArray(array1, array2);
            expect(array2).toEqual(array2Copy);
        });
    });
});