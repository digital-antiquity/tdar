
//mock a file upload - similar process as how we render previously uploaded files
function _mockUpload(helper, filename) {
    var mockFile = _mockFile(filename);
    var ctx = helper.context;
    var fnDone = $(ctx).fileupload('option', 'done');
    fnDone.call(ctx, null, {result: {files: [mockFile]}});
};

//jtd: structure of this mock file simulates what you'd get from the 'done' event arguments, which may or may not
//be related to the json structure returned from the result of the uploadcontroller response json (I can't remember)
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

$(function() {
    "option explicit";

    //init the fileupload (using our init funcition)
    QUnit.begin = function() {
        //TODO: TDAR.uri() backed by getBaseUrl() which is global function in ftl. get it out of ftl and into tdar.core.js
        TDAR.uri = function(relativePath) {
            var pathArray = window.location.pathname.split( '/' );
            var host = pathArray[2];        
            return host + relativePath;
        };

    };

    QUnit.done = function() {
        $('#uploadForm').fileupload("destroy");
        //TODO:  add a way to destroy the extra stuff that we add in registerUpload()
    };

    //basic setup, teardown
    var basic = {

        setup: function() {
            var helper = TDAR.fileupload.registerUpload({
               informationResourceId: -1, 
               acceptFileTypes: /\.(aaa|bbb|ccc|jpg|jpeg|tif|tiff)$/i, 
               formSelector:"#uploadform",
               inputSelector: '#fileAsyncUpload'
            });
            basic.helper = helper;


        },

        teardown: function() {
            $(basic.helper.context).fileupload("destroy");

            var options = null;
            //exception expected here
            try {
                options = $(basic.helper.context).fileupload('option');
            } catch(err) {}
            equal(options, null, "confirm teardown destroyed fileupload widget");

        }
    };

    module("basic validation", basic);
    test("initialization sanity check", function() {
        ok(basic.helper, "helper defined");
        ok($(basic.helper.context).fileupload('option'), "fileupload widget defined");
    });

    test("test valueRequiresAsyncUpload rule", function() {
        ok(false, "implement me, pleease");
    } );


    module("fileupload validation", basic);
    test("test validator initialization", function() {
        var fv = new  FileuploadValidator("uploadform", {registerJqueryValidateMethod: false});
        ok(fv, "validator exists");
    });

    //create custom validation rule that says that filename must contain "foo". 
    test("add custom rule", function() {
        var fv = new  FileuploadValidator("uploadform");
        fv.addMethod("must-have-foo", function(file, files) {
            return file.filename.indexOf("foo") > -1;
        }, "This file does not contain the word 'foo'");
        fv.addRule("must-have-foo");
        equal(Object.keys(fv.methods).length, 1, "method should be added");
        equal(fv.rules.length, 1, "rule should be added");

        //'upload' file that doesn't conform to the rule
        _mockUpload(basic.helper, "flowers.jpg");
        ok(!fv.validate(), "validation should fail because file does not contain 'foo'" );
        equal(fv.errors.length, 1, "expecting validation error");

        //click delete button on the offending file
        var $btn = $(fv.errors[0].file.context).find(".delete-button");
        ok($btn.length >0, "sanity check that we found the delete button");
        $btn.click();
        ok(fv.validate(), "fileupload widget should be valid again because we've deleted the offending file");
    });


});