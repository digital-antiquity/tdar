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
            
            var extra = "<script id='validFormats'>"+JSON.stringify(["tif","pdf","doc"])+"</script>" + 
            "<script id='accountJson'>"+JSON.stringify([{name:"full service", id:1, fullService:true, studentReview:true}, {name:"normal", id:2, fullService:false}])+"</script>";
            
            fixture = fixture.replace("<script id=\"insert\"></script>",extra);
            var fix = jasmine.getFixtures().set(fixture);
            jasmine.Ajax.install();

        });

        afterEach(function() {
            jasmine.Ajax.uninstall();
        });

        it("check valid file extension", function() {
            var conf = getBaseConfig();
            setConfig(conf);            
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });
            var __vapp = TDAR.vuejs.balk.init("#filesTool");
            var Constructor = Vue.extend(__vapp);
            var vapp = new Constructor().$mount();
            expect(vapp == undefined).toBe(false);
             var result = vapp.fileUploadAdd({}, {originalFiles:[{name:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(false);
            expect(vapp.files).toHaveLength(0);
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.tif',size:1000,type:'tif/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
//            result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
//            expect(result).toBe(false);
//            expect(vapp.files).toHaveLength(1);
});
  
});
