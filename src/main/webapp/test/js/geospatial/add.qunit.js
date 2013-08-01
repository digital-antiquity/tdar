(function() {
    "use strict";

    //FIXME: the modules should have to do so much setup.  ideally they should just call whatever the page's "main()"
    //          function is.  but we don't have any convention like that.


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
        return {"size": size}
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
    }

    function _mockDelete(helper, filename) {
        var fnDelete =  $(helper.context).fileupload("option", "destroy");

        var files = $.grep(helper.validFiles(), function(file, idx){
            return file.filename === filename;
        });

        $.each(files, function(idx, file) {
            fnDelete.call(helper.context, null, {context: file.context, url:null, type:"DELETE", dataType:null});
        });
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
                basic.validator.addRule("nodupes");
                basic.helper = helper;
            },
            teardown: function() {
                basic.validator.clearErrorDisplay();
                basic.validator = null;
            }
        }

        module("basic validation rules", basic);
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

        test("conditional validation methods",  function() {
            var b = true,
                validator = basic.validator;

            //create a when-callback that is really just a wrapper around 'b'.  Validation method should be applied
            //whenever b === true
            var when = function() {return b};
            validator.addRule("required", {extension: "jpg", when: when});
            ok(!validator.validate(), "required method should be applied if 'when' callback evals to true");

            b = false;
            ok(validator.validate(), "when-callback returns false, so validator should not apply required method");
            equal(validator.errors.length, 0,  "error list should be empty if we didn't apply validation rules");
        });

        test("no dupes", function() {
            _mockUpload(basic.helper, "foo.tiff");
            _mockUpload(basic.helper, "foo.tiff");
            var rules = $.grep(basic.validator.rules, function(rule){
               return rule.methodName === "nodupes";
            });
            ok(rules.length === 1, "nodupes rule should be present");
            ok(!basic.validator.validate(), "should be invalid because we uploaded dupe file");
        });

        var gis = {
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
                gis.validator = new FileuploadValidator("metadataForm");
                gis.validator.addRule("nodupes");
                TDAR.fileupload.addGisValidation(gis.validator);
                gis.helper = helper;
            },
            teardown: function() {
                gis.validator.clearErrorDisplay();
                gis.validator = null;
            }
        };

        module("gis scenarios", gis);
        test("no files whatsoever", function() {
            ok(gis.validator.validate(), "no files are required");
        });

        test("multiple base filenames", function() {
            _mockUpload(gis.helper, "foo.tiff");
            _mockUpload(gis.helper, "bar.tfw");
            ok(!gis.validator.validate(), "should be invalid because files do not have the same basename");
        });

        test("multiple image files", function() {
            _mockUpload(gis.helper, "fancyfile.tiff");
            _mockUpload(gis.helper, "fancyfile.jpeg");
            ok(!gis.validator.validate(), "should be invalid because only one GIS image file allowed");
        });


        module("conditionally required files", gis);

        //construct tests for all optional/required file types
        (function() {
            var files = {
                optional:{
                    shapefile: ["sbn", "sbx", "fbn", "fbx", "ain", "aih", "atx", "ixs", "mxs", "prj", "xml", "cpg"],
                    jpeg:["jpw"],
                    tiff:["tfw"],
                    image: ["aux", "aux.xml"]
                },

                required: {
                    shapefile: [["shp", "shx", "dbf"]],
                    jpeg:["jpg", "jpeg"],
                    tiff:["tif", "tiff"],
                    image: ["jpg", "jpeg", "tif", "tiff"]
                }
            };



            $.each(files.optional, function(gistype, extensions){

                var optexts = files.optional[gistype];
                var reqexts = files.required[gistype];
                $.each(optexts, function(oidx, optext) {
                    $.each(reqexts, function(ridx, reqext){
                        //if reqext is array,  add all elements to the form
                        var exts = typeof reqext === "string" ? [reqext] : reqext;

                        test("validator should return false if ." + optext + " file present without " + exts.join(", ") + " file present", function() {
                            ok(gis.helper.validFiles().length === 0);

                            var filename = "basename." + optext;

                            //now add an optional file
                            _mockUpload(gis.helper, filename);

                            ok(!gis.validator.validate(), "form should be invalid because we included " + filename + " without also including required file");

                            //now add the required file(s)
                            $.each(exts, function(idx, ext) {
                               _mockUpload(gis.helper, "basename." + ext);
                            });

                            ok(gis.validator.validate(), "form should be valid because all required files present");
                        });

                    });
                });

            });
        })();






    });
//FIXME: we need some tests where we simulate file deletion/replacement
})()