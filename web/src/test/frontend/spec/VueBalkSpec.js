/* global describe, it, xit, expect, beforeEach, afterEach, loadFixtures */
describe("BalkSpec.js: fileupload suite - root", function(){
    "use strict";

       function getBaseConfig() {
            return {files:[],
                url:"/upload/upload",
                ticketId:-1,
                resourceId: -1,
                userId: -1,
                validFormats:[],
                sideCarOnly:false,
                ableToUpload:true,
                maxNumberOfFiles:50,
                requiredOptionalPairs:[]};
        }
       
       function setConfig(conf) {
           var c = JSON.stringify(conf);
           $("#uploadWidget").data("config","#uploadConfig");
           $("#uploadWidget").append("<script id='uploadConfig'></script>");
           $("#uploadConfig").text(c);
       }

        beforeEach(function() {
            // change the fixture path to point to our template
            jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/dashboard/";
            // read the fixture as a string
            var fixture = jasmine.getFixtures().read("balk.html");
            
            // set some IDs
            // apply the fixxture
            
            var extra = "<script id='validFormats'>['tif','pdf','doc']</script>" + 
            "<script id='accountJson'>[]</script>";
            
            fixture = fixture.replace("<script id=\"insert\"></script>",extra);
            var fix = jasmine.getFixtures().set(fixture);
            jasmine.Ajax.install();

        });

        afterEach(function() {
            jasmine.Ajax.uninstall();
        });

        it("check valid file extension", function() {
            var conf = getBaseConfig();
            conf.validFormats.push('.tif');
            conf.validFormats.push('.jpg');
            conf.validFormats.push('.tiff');
            setConfig(conf);            
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });
            var vapp = TDAR.vuejs.balk.init("#filesTool");
//            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.JPG',size:1000,type:'jpg/image',lastModified:-1}]});
//            expect(result).toBe(true);
//            expect(vapp.files).toHaveLength(1);
//            result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
//            expect(result).toBe(false);
//            expect(vapp.files).toHaveLength(1);
});
  
});
