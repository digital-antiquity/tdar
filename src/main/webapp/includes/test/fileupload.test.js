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


var basic = {
    setup: function() {
        var helper = TDAR.fileupload.registerUpload({
           informationResourceId: -1, 
           acceptFileTypes: /\.(aaa|bbb|ccc|jpg|jpeg|tif|tiff)$/i, 
           formSelector:"#uploadform",
           inputSelector: '#fileAsyncUpload'
        });

        ok(helper, "helper defined");

        ok($(helper.context).fileupload('option'), "fileupload widget defined");

    },

    teardown: function() {
        console.log("hi mom");
    }
};

module("basic", basic);

test("sample test", function() {
    ok( 1==1, "this test does nothing");
});


});