/* global describe, jasmine, $, $j, beforeEach, afterEach, it, xit, loadFixtures */
xdescribe("FormValidateExtensionsSpec.js: form validation suite - root", function(){
    "use strict";

    var helper, fileValidator;

    var validator; //the jquery form validator, not our file upload validator

    function _mockFile(name) {
        var stats = _fakestats(name);
        var file = {
            action: "ADD",
            name: name,
            thumbnail_url: null,
            delete_url: null,
            delete_type: "DELETE"
        };

        return $.extend(file, stats);
    }

    function _fakestats(str) {
        var i, id = "", size = 0;
        if (str === "" || str == undefined) {
            return 0;
        }
        for (var i = 0; i < str.length; i++) {
            var code = str[i].charCodeAt(0);
            id += code;
            size += code;
        }
        //not sure id is necessary for this mock, since we wouldn't get it from the uploadController.upload() response
        // json return {"fileId": id, "size": size};
        return {"size": size}
    };


    function _mockUpload(filename, options) {

        var mockFile = _mockFile(filename);
        var ctx = helper.context;
        var fnDone = $(ctx).fileupload('option', 'done');
        var data = {result: $.extend({files: [mockFile]}, options)};
        fnDone.call(ctx, null, data);
        var $filesContainer = $($(helper.context).fileupload("option", "filesContainer"));
        data.result.context = $filesContainer.find("tr").last();
        $(ctx).trigger("fileuploadcompleted", data.result);
    };


    function _fillout(map) {
        for (var key in map) {
            $(key).val(map[key]);
        }
    }

    function _fillOutRequiredFields() {
        $("#resourceRegistrationTitle").val("a title")
        _fillout({
            "#resourceRegistrationTitle": "a title",
            "#dateCreated": "2002",
            "#resourceDescription": "sample image",
            "#projectId": "-1",
            "#metadataForm_accountId": "111"
        });
    }

    function _mockReplace($targetRow, filename) {
        $targetRow.addClass("replace-target");
        _mockUpload(filename, {$replaceTarget: $targetRow});
        $targetRow.removeClass("replace-target");
    }


    describe("custom jquery validation rules - uploads", function() {
        var form;

        beforeEach(function(){
            loadFixtures("document-add-form.html", "fileupload-templates.html");

            var props = {
                formSelector: "#metadataForm",
                includeInheritance : true,
                acceptFileTypes : /\.(jpg|bmp|pict|tif|jpeg|png|gif|tiff)$/i,
                multipleUpload : true,
                validExtensions : "jpg|bmp|pict|tif|jpeg|png|gif|tiff",
                validExtensionsWarning : "Please enter a valid file (jpg, bmp, pict, tif, jpeg, png, gif, tiff)",
                ableToUpload : true,
                dataTableEnabled : false
            };

            form = document.getElementById('metadataForm');
            TDAR.common.initEditPage(form, props);
            form = $(form)

            helper = TDAR.fileupload.registerUpload({
                informationResourceId: -1,
                acceptFileTypes: /\.(aaa|bbb|ccc|jpg|jpeg|tif|tiff)$/i,
                formSelector: "#metadataForm",
                inputSelector: '#fileAsyncUpload'
            });

            validator = form.data('validator');

        });

        it("sanity check", function() {
            expect(helper).toBeDefined();
            expect(form).toBeDefined();
            expect(validator).toBeDefined();
        });

        it("confidential file should not submit unless we have a contact", function () {
            _fillOutRequiredFields();
            _mockUpload("foo.jpg");
            // should only be one file row
            expect($('.fileProxyConfidential').length).toBe(1);

            $('.fileProxyConfidential').val("CONFIDENTIAL");
            // form validation should return false, because we have at least one confidential file but zero contacts
            expect(form.valid()).toBe(false);

            $('.fileProxyConfidential').val("EMBARGOED");
            // form validation should return false, because we have at least one confidential file but zero contacts
            expect(form.valid()).toBe(false);

            $('.fileProxyConfidential').val("PUBLIC");
            // form should be valid now because file is public
            expect(form.valid()).toBe(true);
        });

        it("replace file should work", function () {
            var $filesContainer = $($(helper.context).fileupload("option", "filesContainer"));
            _mockUpload("one.jpg");
            _mockUpload("two.jpg");
            var $replaceTarget = $filesContainer.find("tr").last();
            _mockReplace($replaceTarget, "two-replaced.jpg");
            console.log("next object is last row");
            console.dir($filesContainer.find("tr").last().html());
            // we should still only have two files after replace operation
            expect($filesContainer.find("tr").length).toBe(2);
        });

    });


    describe("custom jquery validation rules - general purpose", function(){
        var form;

        beforeEach(function(){
            loadFixtures("document-add-form.html", "fileupload-templates.html");

            var props = {
                formSelector: "#metadataForm",
                includeInheritance : true,
                acceptFileTypes : /\.(jpg|bmp|pict|tif|jpeg|png|gif|tiff)$/i,
                multipleUpload : true,
                validExtensions : "jpg|bmp|pict|tif|jpeg|png|gif|tiff",
                validExtensionsWarning : "Please enter a valid file (jpg, bmp, pict, tif, jpeg, png, gif, tiff)",
                ableToUpload : true,
                dataTableEnabled : false
            };

            form = document.getElementById('metadataForm');
            TDAR.common.initEditPage(form, props);
            form = $(form)
            validator = form.data('validator');
        });


        it("sanity check", function() {
            expect(form).toBeDefined();
        });


        it("submitter info", function() {
            // data-submitterid attribute set
            expect(typeof $("#metadataForm").data("submitterid")).toBe("number");
        });

        it("submitter same as authuser ",
            function() {
                //make sure that this section runs after body.onready() and $form.validate()
                    //supress the modal error dialog, form submit
                    var validator = $("#metadataForm").data("validator");
                    var $form = $("#metadataForm");
                    var submitterid = $form.data("submitterid");

                    $form.populate({
                        'document.title': "sample title",
                        'document.date': 2014,
                        'document.description': "sample description",
                        'projectId': -1,
                        'authorizedUsers[0].user.tempDisplayName': "Bobby Tables",
                        'authorizedUsers[0].user.id': submitterid,
                        'accountId': '111'
                    });

                    $form.valid();
                    // should have only one error
                    expect(validator.errorList.length).toBe(0)
            }
        );


    });
});
