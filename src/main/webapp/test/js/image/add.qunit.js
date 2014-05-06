(function (common, fileupload, $) {
    "use strict";

    var fileuploadModule = {
        helper: {},
        fileuploadSelector: "#divFileUpload",
        formSelector: "#metadataForm",
        inputSelector: "#fileAsyncUpload",
        form: null,
        setup: function () {
            _pageinit();

            var helper = fileuploadModule.helper = $(fileuploadModule.fileuploadSelector).data("fileuploadHelper");
            fileuploadModule.form = $(fileuploadModule.formSelector);
            ok(fileuploadModule.form.length, "form exists");

            //dont show modal when validation fails
            fileuploadModule.form.validate().showErrors = function (errorMap, errorList) {
            };
        },

        teardown: function () {
        },

        fillOutRequiredFields: function () {
            $("#resourceRegistrationTitle").val("a title")
            _fillout({
                "#resourceRegistrationTitle": "a title",
                "#dateCreated": "2002",
                "#resourceDescription": "sample image",
                "#projectId": "-1"
            });
        }
    };


    //HACK: kill any initial fileupload registration.  we will do this in our test.
    try {
        $(fileuploadModule.fileuploadSelector).fileupload("destroy");
    } catch (err) {
        console.log("tried to destroy fileupload:: %s", err);
    }

    function _pageinit() {
        //hack:  mimic the one-time initialization that happens on the edit page.
        console.log("running initEditPage");
        common.initEditPage($(fileuploadModule.formSelector)[0]);

        var helper = TDAR.fileupload.registerUpload({
            informationResourceId: -1,
            acceptFileTypes: /\.(aaa|bbb|ccc|jpg|jpeg|tif|tiff)$/i,
            formSelector: fileuploadModule.fileuploadSelector,
            inputSelector: fileuploadModule.inputSelector
        });

        console.log("initEditPage done");
        console.dir(helper);
    }

    function _mockUpload(helper, filename, options) {

        var mockFile = _mockFile(filename);
        var ctx = helper.context;
        var fnDone = $(ctx).fileupload('option', 'done');
        var data = {result: $.extend({files: [mockFile]}, options)};
        fnDone.call(ctx, null, data);
        var $filesContainer = $($(fileuploadModule.helper.context).fileupload("option", "filesContainer"));
        data.result.context = $filesContainer.find("tr").last();
        $(ctx).trigger("fileuploadcompleted", data.result);
    };

    function _mockFile(name) {
        var stats = _fakestats(name);
        var file = {
            action: "ADD",
            name: name,
            thumbnail_url: null,
            delete_url: null,
            delete_type: "DELETE"};

        return $.extend(file, stats);
    };

    function _mockReplace(helper, $targetRow, filename) {
        $targetRow.addClass("replace-target");
        _mockUpload(helper, filename, {$replaceTarget: $targetRow});
        $targetRow.removeClass("replace-target");
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
        //not sure id is necessary for this mock, since we wouldn't get it from the uploadController.upload() response json
        // return {"fileId": id, "size": size};
        return {"size": size};
    }

    function _upload(filename) {
        _mockUpload(fileuploadModule.helper, filename);
    }

    function _fillout(map) {
        for (var key in map) {
            $(key).val(map[key]);
        }
    }

    $(function () {
        TDAR.maxUploadFiles = 2;
        module("FILE UPLOAD", fileuploadModule);
        test("sanity check", function () {
            fileuploadModule.fillOutRequiredFields();
            ok(fileuploadModule.form.valid(), "form should have zero validation errors");
        });

        test("confidential file should not submit unless we have a contact", function () {
            fileuploadModule.fillOutRequiredFields();
            _upload("foo.jpg");
            equal($('.fileProxyConfidential').length, 1, "should only be one file row");

            $('.fileProxyConfidential').val("CONFIDENTIAL");
            ok(!fileuploadModule.form.valid(), "form validation should return false, because we have at least one confidential file but zero contacts");

            $('.fileProxyConfidential').val("EMBARGOED");
            ok(!fileuploadModule.form.valid(), "form validation should return false, because we have at least one confidential file but zero contacts");

            $('.fileProxyConfidential').val("PUBLIC");
            ok(fileuploadModule.form.valid(), 'form should be valid now because file is public');
        });

        test("confidential file should be allowed because we have a contact", function () {
            fileuploadModule.fillOutRequiredFields();
            _upload("foo.jpg");

        });

        test("replace file should work", function () {
            _upload("one.jpg");
            _upload("two.jpg");
            var $filesContainer = $($(fileuploadModule.helper.context).fileupload("option", "filesContainer"));
            var $replaceTarget = $filesContainer.find("tr").last();
            _mockReplace(fileuploadModule.helper, $replaceTarget, "two-replaced.jpg");
            console.log("next object is last row");
            console.dir($filesContainer.find("tr").last().html());
            equal($filesContainer.find("tr").length, 2, "we should still only have two files after replace operation");
        });
    });

    module("JQUERY-VALIDATION", {setup: function(){
        common.initEditPage(document.metadataForm);
        //supress the modal error dialog, form submit
        var validator = $("#metadataForm").data("validator");
        var $form = $("#metadataForm");

        //disable actual form submit
        validator.settings.submitHandler = function(){};
        //don't show error messages in a modal window
        validator.settings.showErrors = function() {};
    }});

    test("submitter info", function() {
        equal(typeof $("#metadataForm").data("submitterid"), "number",  "data-submitterid attribute set");
    });

    test("submitter same as authuser ",
            function() {
                expect(2);

                //make sure that this section runs after body.onready() and $form.validate()
                $(function() {
                    //supress the modal error dialog, form submit
                    var validator = $("#metadataForm").data("validator");
                    var $form = $("#metadataForm");
                    var submitterid = $form.data("submitterid");

                    $form.populate({
                        'image.title': "sample title",
                        'image.date': 2014,
                        'image.description': "sample description",
                        'projectId': -1,
                        'authorizedUsers[0].user.tempDisplayName': "Bobby Tables",
                        'authorizedUsers[0].user.id': submitterid
                    });

                    $form.valid();
                    equal(validator.errorList.length, 1, "should have only one error");
                    if(validator.errorList.length) {
                        equal(validator.errorList[0].message, $.validator.messages["authuserNotSubmitter"], "expecting a specific error message");
                    }

                })
            }
    );


})(TDAR.common, TDAR.fileupload, jQuery);