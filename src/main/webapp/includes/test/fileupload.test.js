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
}


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
        console.log("hi mom");
    }
};

//the first test asserts that our basic setup/teardown works
test("basic.setup", function() {
    basic.setup();
    ok(basic.helper, "helper defined");
    ok($(basic.helper.context).fileupload('option'), "fileupload widget defined");
    basic.teardown();
});

test("basic.teardown", function() {
    basic.setup();
    basic.teardown();
    var options = null;
    //exception expected here
    try {
        options = $(basic.helper.context).fileupload('option');
    } catch(err) {}
    equal(options, null, "fileupload widget should be destroyed after teardown");
});


module("basic", basic);

test("required file rule", function() {
    var fv = new  FileuploadValidator("uploadform", {registerJqueryValidateMethod: false});
    ok(fv, "validator exists");

});


});