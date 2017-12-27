/* global describe, it, xit, expect, beforeEach, afterEach, loadFixtures */
describe("FileuploadSpec.js: fileupload suite - root", function(){
    "use strict";



        beforeEach(function() {
            // change the fixture path to point to our template
            jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/resource/";
            // read the fixture as a string
            var fixture = jasmine.getFixtures().read("vue-file-upload-template.html");
            
            // set some IDs
            fixture = fixture.replace("${editable?c}","false");
            fixture = fixture.replace("${resource.id?c}","111");

            // apply the fixxture
            var fix = jasmine.getFixtures().set(fixture);
        });

        afterEach(function() {

        });

        it("sanity checks", function() {

            window.console.log("--------------------bbb---------------------")
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            console.log(vapp);
            window.console.log("--------------------ccc---------------------")
        });
  
});
