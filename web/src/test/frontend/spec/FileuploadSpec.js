/* global describe, it, xit, expect, beforeEach, afterEach, loadFixtures */
xdescribe("FileuploadSpec.js: fileupload suite - root", function(){
    "use strict";

    var helper, fileValidator;

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


    function _mockUpload(filename) {
        var mockFile = _mockFile(filename);
        var ctx = helper.context;
        var fnDone = $(ctx).fileupload('option', 'done');
        fnDone.call(ctx, null, {result: {files: [mockFile]}});
    }



    describe ("validation rules", function() {
        "use strict";


        beforeEach(function() {
            loadFixtures("document-add-form.html", "fileupload-templates.html");

            //register the fileupload widget & validator
            helper = TDAR.fileupload.registerUpload({
                informationResourceId: -1,
                acceptFileTypes: /\.[a-z]+$/i,
                formSelector: "#metadataForm",
                inputSelector: '#fileAsyncUpload'
            });

            fileValidator = new TDAR.fileupload.FileuploadValidator($("#metadataForm"));
        });

        afterEach(function() {

        });

        it("sanity checks", function() {
            //sanity check: did we set up the fixtures right?
            expect($j('#metadataForm')).toHaveLength(1);
            expect($j('#fileAsyncUpload')).toHaveLength(1);
            expect(helper).toBeDefined();
            expect(fileValidator).toBeDefined();
        });

        it("validation rule: required w/ no files", function() {
            fileValidator.addRule("required");
            expect(fileValidator.validate()).toBe(false);
        });

        it("validation rule: required w/ file", function() {
            fileValidator.addRule("required");
            _mockUpload("foo.doc");
            expect(fileValidator.validate()).toBe(true);
        });

        it("should validate when required-files is a 'suggestion', not a rule ", function() {
            fileValidator.addSuggestion("required");
            expect(fileValidator.validate()).toBe(true);
            expect(fileValidator.suggestions.length).toBe(1);

            // now add a file and see if suggestion goes away
            _mockUpload("foo.doc");
            fileValidator.validate();
            expect(fileValidator.suggestions.length).toBe(0);
        });

        it("should require at least one .foo file", function() {
            fileValidator.addRule("required", {extension: "foo"}, "you must upload at least one .foo file");
            _mockUpload("foo.baz");
            expect(fileValidator.validate()).toBe(false);

            _mockUpload("foo.foo");
            expect(fileValidator.validate()).toBe(true);
        });

        it("ignores validation rule if when-condition is false", function() {
            // if the when-condition is false,  validator shouldn't apply 'required' rule
            fileValidator.addRule("required", {
                extension: "jpg", when: function() {
                    return false
                }
            });
            expect(fileValidator.validate()).toBe(true);
        })

        it("applies validation rule if when-condition is true", function() {
            // if the when-condition is false,  validator shouldn't apply 'required' rule
            fileValidator.addRule("required", {
                extension: "jpg", when: function() {
                    return true
                }
            });
            expect(fileValidator.validate()).toBe(false);

            _mockUpload("image.jpg");
            expect(fileValidator.validate()).toBe(true);
        })

        it("disallows dupes when dupes rule applied", function() {
            fileValidator.addRule("nodupes");
            _mockUpload("foo.tiff");
            _mockUpload("foo.tiff");
            expect(fileValidator.validate()).toBe(false);
        });

        //module("gis scenarios", gis);

        //create custom validation rule that says that filename must contain "foo".
        it("add custom rule", function() {
            var oldRuleCount = Object.keys(fileValidator.rules).length;
            var oldMethodCount = Object.keys(fileValidator.methods).length;

            fileValidator.addMethod("must-have-foo", function(file, files) {
                return file.filename.indexOf("foo") > -1;
            }, "This file does not contain the word 'foo'");

            fileValidator.addRule("must-have-foo");

            // method should be added
            expect(Object.keys(fileValidator.methods).length).toBe(oldMethodCount + 1);

            // rule should be added
            expect(fileValidator.rules.length).toBe(oldRuleCount + 1);

            //'upload' file that doesn't conform to the rule
            _mockUpload("flowers.jpg");

            // validation should fail because file does not contain 'foo'" );
            expect(fileValidator.validate()).toBe(false);

            // expecting validation error;
            expect(fileValidator.errors.map(function(err){return err.message})).toContain("This file does not contain the word 'foo'");

            //click delete button on the offending file
            var $btn = $(fileValidator.errors[0].file.context).find(".delete-button");
            // sanity check that we found the delete button
            expect($btn.length).toBeGreaterThan(0);
            $btn.click();
            // fileupload widget should be valid again because we've deleted the offending file
            expect(fileValidator.validate()).toBe(true);
        });
    });

    describe("gis tests", function() {

        beforeEach(function() {
            loadFixtures("document-add-form.html", "fileupload-templates.html");

            //register the fileupload widget & validator
            helper = TDAR.fileupload.registerUpload({
                informationResourceId: -1,
                acceptFileTypes: /\.[a-z]+$/i,
                formSelector: "#metadataForm",
                inputSelector: '#fileAsyncUpload'
            });

            fileValidator = new TDAR.fileupload.FileuploadValidator($("#metadataForm"));
            fileValidator.addRule("nodupes");
            TDAR.fileupload.addGisValidation(fileValidator);
        });

        it("no files whatsoever", function () {
            it(fileValidator.validate());
        });

        it("multiple base filenames", function () {
            _mockUpload("foo.shp");
            _mockUpload("foo.shx");
            _mockUpload("foo.dbf");
            _mockUpload("foo.tiff");
            _mockUpload("bar.tfw");
            // should be invalid because files do not have the same basename
            expect(fileValidator.validate()).toBe(false);
        });

        it("adf files excluded from basename rule", function () {
            _mockUpload("chunky-peanut-butter.shp");
            _mockUpload("chunky-peanut-butter.shx");
            _mockUpload("chunky-peanut-butter.dbf");
            _mockUpload("chunky-peanut-butter.tiff");
            _mockUpload("chunky-peanut-butter.tfw");
            _mockUpload("grape-jelly.adf");
            // should be valid because adf files are excluded from the same-basename requirement
            expect(fileValidator.validate()).toBe(true);
        });

        it("multiple image files should be invalid because only one GIS image file allowed", function () {
            _mockUpload("fancyfile.tiff");
            _mockUpload("fancyfile.jpeg");
            expect(fileValidator.validate()).toBe(false);
        });




        //construct tests for all optional/required file types
        describe("supplemental files that require essential files", function () {
            var files = {
                optional: {
                    shapefile: ["sbn", "sbx", "fbn", "fbx", "ain", "aih", "atx", "ixs", "mxs", "prj", "xml", "cpg"],
                    jpeg: ["jpw"],
                    tiff: ["tfw"],
                    image: ["aux", "aux.xml"]
                },

                required: {
                    shapefile: [
                        ["shp", "shx", "dbf"]
                    ],
                    jpeg: ["jpg", "jpeg"],
                    tiff: ["tif", "tiff"],
                    image: ["jpg", "jpeg", "tif", "tiff"]
                }
            };
            
            it("sbn file requires esential shapefiles", function(){
                //expect(helper.validFiles().length).toBe(0);
                //_mockUpload("base.sbn");
                
            });

            // $.each(files.optional, function (gistype, extensions) {

            //     var optexts = files.optional[gistype];
            //     var reqexts = files.required[gistype];
            //     $.each(optexts, function (oidx, optext) {
            //         $.each(reqexts, function (ridx, reqext) {
            //             //if reqext is array,  add all elements to the form
            //             var exts = typeof reqext === "string" ? [reqext] : reqext;

            //             it("validator should return false if ." + optext + " file present without " + exts.join(", ") + " file present", function () {
            //                 expect(helper.validFiles().length).toBe(0);

            //                 var filename = "basename." + optext;

            //                 //now add an optional file
            //                 _mockUpload(filename);

            //                 expect(fileValidator.validate()).toBe(false);

            //                 //now add the required file(s)
            //                 $.each(exts, function (idx, ext) {
            //                     _mockUpload("basename." + ext);
            //                 });

            //                 // form should be valid because all required files present
            //                 expect(fileValidator.validate()).toBe(true);
            //             });

            //         });
            //     });

            // });
            
            
        });
    });
});
