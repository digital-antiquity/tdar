(function() {
    "option explicit";

    //mock a file upload - similar process as how we render previously uploaded files
    function _mockUpload(helper, filename) {
        var mockFile = _mockFile(filename);
        var ctx = helper.context;
        var fnDone = $(ctx).fileupload('option', 'done');
        fnDone.call(ctx, null, {result: {files: [mockFile]}});
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


    function _mockFile(name) {
        var stats = _fakestats(name);
        var file  = {
            action: "ADD",
            name: name,
            thumbnail_url: null,
            delete_url: null,
            delete_type: "DELETE"};

        return $.extend(file, stats);
    }




    $(function() {

        var basic = {

            //qunit destroys fixture dom on teardown.  need to re-register fileupload widget
            setup: function() {
                var helper = $("#metadataForm").data("fileuploadHelper");
                if(!helper) {
                    helper = TDAR.fileupload.registerUpload({
                        informationResourceId: -1,
                        acceptFileTypes: /\.[a-z]+$/i,
                        formSelector:"#metadataForm",
                        inputSelector: '#fileAsyncUpload'
                    });
                }
                basic.validator = new FileuploadValidator("metadataForm");
                basic.helper = helper;
            },
            teardown: function() {
                basic.validator.clearErrors();
                basic.validator = null;
            }
        }


        module("required files", basic);
        test("required file missing", function() {
            var validator = basic.validator;
            validator.addRule("required");
            var valid = validator.validate();
            ok(!valid, "form should be invalid");
        });


        test("suggested files", function() {
            basic.validator.addSuggestion("required");
            ok(basic.validator.validate(),  "form should be valid");
            equal(basic.validator.suggestions.length, 1, "we should have one suggestion after validation");
            _mockUpload(basic.helper, "foo.txt");
            ok(basic.validator.validate(), "form is still valid");
            equal(basic.validator.suggestions.length, 0, "now that we uploaded a file,  suggestion should go away");
        });

        test("required file extension", function(){
            var validator = basic.validator,
                helper = basic.helper;
            validator.addRule("required", {extension: "foo"}, "you must upload at least one .foo file");
            _mockUpload(helper, "foo.baz");
            ok(!validator.validate(), "form should be invalid because user hasn't uploaded file with the right type yet.");
            _mockUpload(helper, "foo.foo");
            ok(validator.validate(), "user uploaded a .foo file, so the file group should be valid");

        });



    });

})()