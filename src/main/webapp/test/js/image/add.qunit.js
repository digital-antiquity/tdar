(function(common, fileupload, $){
    "use strict";

    var basic = {
            helper: {},
            fileuploadSelector: "#divFileUpload",
            formSelector: "#metadataForm",
            inputSelector: "#fileAsyncUpload",
            form: null,
            setup: function() {
                _pageinit();

                var helper = basic.helper = $(basic.fileuploadSelector).data("fileuploadHelper");
                basic.form = $(basic.formSelector);
                ok(basic.form.length, "form exists");

                //dont show modal when validation fails
                basic.form.validate().showErrors = function(errorMap, errorList) {
                };
            },

            teardown: function() {
                //$("#metadataForm").fileupload("destroy");
            },

            fillOutRequiredFields: function() {
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
        $(basic.fileuploadSelector).fileupload("destroy");
    } catch(err) {
        console.log("tried to destroy fileupload:: %s", err);
    }

    function _pageinit() {
        //hack:  mimic the one-time initialization that happens on the edit page.
        console.log("running initEditPage");
        common.initEditPage($(basic.formSelector)[0]);

        var helper = TDAR.fileupload.registerUpload({
            informationResourceId: -1,
            acceptFileTypes: /\.(aaa|bbb|ccc|jpg|jpeg|tif|tiff)$/i,
            formSelector: basic.fileuploadSelector,
            inputSelector: basic.inputSelector
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
        var $filesContainer = $($(basic.helper.context).fileupload("option","filesContainer"));
        data.result.context = $filesContainer.find("tr").last();
        $(ctx).trigger("fileuploadcompleted", data.result);
    };

    function _mockFile(name) {
        var stats = _fakestats(name);
        var file  = {
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
        var i,
            id="",
            size=0;
        if(str === "" || str == undefined) return 0;
        for(var i = 0; i < str.length; i++) {
            var code = str[i].charCodeAt(0);
            id += code;
            size += code;
        }
        //not sure id is necessary for this mock, since we wouldn't get it from the uploadController.upload() response json
        // return {"fileId": id, "size": size};
        return {"size": size};
    }

    function _upload(filename) {
        _mockUpload(basic.helper, filename);
    }

    function _fillout(map) {
        for(var key in map) {
            $(key).val(map[key]);
        }
    }



$(function() {
    module("basic", basic);
    test ("sanity check", function () {
        basic.fillOutRequiredFields();
        ok(basic.form.valid(), "form should have zero validation errors");
    });

    test("confidential file should not submit unless we have a contact", function() {
        basic.fillOutRequiredFields();
        _upload("foo.jpg");
        equal($('.fileProxyConfidential').length, 1, "should only be one file row");

        $('.fileProxyConfidential').val("CONFIDENTIAL");
        ok(!basic.form.valid(), "form validation should return false, because we have at least one confidential file but zero contacts");

        $('.fileProxyConfidential').val("EMBARGOED");
        ok(!basic.form.valid(), "form validation should return false, because we have at least one confidential file but zero contacts");

        $('.fileProxyConfidential').val("PUBLIC");
        ok(basic.form.valid(), 'form should be valid now because file is public');
    });


    test("confidential file should be allowed because we have a contact", function() {
        basic.fillOutRequiredFields();
        _upload("foo.jpg");

    });

    test("replace file should work", function() {
        _upload("one.jpg");
        _upload("two.jpg");
        var $filesContainer = $($(basic.helper.context).fileupload("option","filesContainer"));
        var $replaceTarget = $filesContainer.find("tr").last();
        _mockReplace(basic.helper, $replaceTarget, "two-replaced.jpg");
        console.log("next object is last row");
        console.dir($filesContainer.find("tr").last().html());
        equal($filesContainer.find("tr").length, 2, "we should still only have two files after replace operation");
    });


    TDAR.maxUploadFiles = 2;

    test("file upload should fail after reaching cap", function() {
        _upload("one.jpg");
        _upload("two.jpg");
        _upload("three.jpg");

        var $filesContainer = $($(basic.helper.context).fileupload("option","filesContainer"));
        equal($filesContainer.find("tr").length, 3);
        notEqual($replaceTarget.html().toLowerCase().indexOf("error"), -1, "the last row should contain an error message");
    });

    test("replace file works even if already at file cap", function() {
        _upload("one.jpg");
        _upload("two.jpg");

        var $filesContainer = $($(basic.helper.context).fileupload("option","filesContainer"));
        var $replaceTarget = $filesContainer.find("tr").last();
        _mockReplace(basic.helper, $replaceTarget, "two-replaced.jpg");
        equal($filesContainer.find("tr").length, 2, "we should still only have two files after replace operation");
        equal($replaceTarget.html().toLowerCase().indexOf("error"), -1, "there should be no error text in the last row");

    });


});


})(TDAR.common, TDAR.fileupload, jQuery);