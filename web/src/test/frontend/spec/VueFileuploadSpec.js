/* global describe, it, xit, expect, beforeEach, afterEach, loadFixtures */
describe("FileuploadSpec.js: fileupload suite - root", function(){
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
            jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/resource/";
            // read the fixture as a string
            var fixture = jasmine.getFixtures().read("vue-file-upload-template.html");
            
            // set some IDs
            // apply the fixxture
            fixture = fixture.replace("${uploadConfigId}","uploadConfig");
            fixture = fixture.replace("${uploadConfigId}","uploadConfig");
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

            window.console.log("--------------------bbb---------------------")
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'test.JPG',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'test.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(false);
            expect(vapp.files).toHaveLength(1);

            
            window.console.log("--------------------ccc---------------------")
        });
  
        it("check valid sidecar", function() {
            var conf = getBaseConfig();
            conf.validFormats.push('.tif');
            conf.validFormats.push('.jpg');
            conf.validFormats.push('.tfw');
            conf.validFormats.push('.jgw');
            conf.sideCarOnly = true;
            setConfig(conf);            
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });

            window.console.log("--------------------bbb---------------------")
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'test.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(2);

            
            window.console.log("--------------------ccc---------------------")
        });

        it("check invalid sidecar", function() {
            var conf = getBaseConfig();
            conf.validFormats.push('.tif');
            conf.validFormats.push('.jpg');
            conf.validFormats.push('.tfw');
            conf.validFormats.push('.jgw');
            conf.sideCarOnly = true;
            setConfig(conf);            
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });

            window.console.log("--------------------bbb---------------------")
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'tast.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(false);
            expect(vapp.files).toHaveLength(1);

            
            window.console.log("--------------------ccc---------------------")
        });
        

        it("check complexPairs sidecar", function() {
            var conf = getBaseConfig();
            conf.validFormats.push('.tif');
            conf.validFormats.push('.jpg');
            conf.validFormats.push('.tfw');
            conf.validFormats.push('.jgw');
            conf.requiredOptionalPairs.push({required:['.jpg'],optional:[".jgw"]});
            conf.sideCarOnly = true;
            setConfig(conf);            
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });

            window.console.log("--------------------bbb---------------------")
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
//            expect(vapp.validatePackage()).toBe(false);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'test.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(2);
            expect(vapp.validatePackage()).toBe(true);

            
            window.console.log("--------------------ccc---------------------")
        });
  

        it("check complexPairs sidecar invalid", function() {
            var conf = getBaseConfig();
            conf.validFormats.push('.tif');
            conf.validFormats.push('.jpg');
            conf.validFormats.push('.tfw');
            conf.validFormats.push('.jgw');
            conf.requiredOptionalPairs.push({required:['.jpg'],optional:[".jgw"]});
            conf.sideCarOnly = true;
            setConfig(conf);            
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });

            window.console.log("--------------------bbb---------------------")
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'test.jgw',size:1000,type:'jgw/object',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            expect(vapp.validatePackage()).toBe(false);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{filename:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(2);
            expect(vapp.validatePackage()).toBe(true);

            
            window.console.log("--------------------ccc---------------------")
        });
  
});
