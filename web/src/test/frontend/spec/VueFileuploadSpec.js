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
            fixture = fixture.replace("${vueFilesFallback!'{{ files }}'}","{{files}}");
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

            window.console.log("--------------------bbb---------------------");
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.JPG',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(false);
            expect(vapp.files).toHaveLength(1);

            
            window.console.log("--------------------ccc---------------------");
            vapp.$destroy();

        });
  

        function setupValidApp() {
            var conf = getBaseConfig();
            conf.validFormats.push('.tif');
            conf.validFormats.push('.jpg');
            conf.validFormats.push('.tiff');
            setConfig(conf);            
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });

            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var _files = [{name:'test.JPG',size:1000,type:'jpg/image',lastModified:-1}];
            var result = vapp.fileUploadAdd(undefined, {originalFiles:_files});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            var data = {
                    result: {
                        files: _files,
                        ticket: {id:100},
                        status:'success',
                        statusText:'success'
                    }
            };
            vapp.fileUploadAddDone({},data);
            return vapp;
        }
        
        it("modfies metadata", function() {
            var vapp = setupValidApp();
            Vue.nextTick(function() {
                var filePart = vapp.$children[0];
                console.log('ACTION', filePart.file.action);
                expect(filePart.file.action).toEqual("ADD");
    
                filePart.file.action= 'NONE';
    
                filePart.updateCreatedDate(new Date());
                console.log(filePart.file.action);
                expect(filePart.file.action).toEqual("MODIFY_METADATA");
                filePart.file.action = 'NONE';
                filePart.file.description = 'test';
                Vue.nextTick(function() {
                    expect(filePart.file.action).toEqual("MODIFY_METADATA");
                    filePart.file.action = 'NONE';
                    filePart.file.restriction = 'test';
                    Vue.nextTick(function() {
                        expect(filePart.file.action).toEqual("MODIFY_METADATA");
                        vapp.$destroy();

                    });
                });
            });
        });

        it("delete file", function() {
            var vapp = setupValidApp();
            Vue.nextTick(function() {
                var filePart = vapp.$children[0];

                filePart.deleteFile();
                console.log(filePart.file.action);
                expect(filePart.file.action).toEqual("DELETE");
                filePart.unDeleteFile();
                expect(filePart.file.action).toEqual("ADD");
                vapp.$destroy();
 
            });
        });

        it("replace file", function() {
            var vapp = setupValidApp();
            Vue.nextTick(function() {
                var filePart = vapp.$children[0];
                console.log("ERROR ABC");
                filePart.replaceFileChange({target : { files:[{name:'atest.JPG',size:1100,type:'jpg/image',lastModified:-1}]}});
                expect(filePart.file.action).toEqual('REPLACE');
                expect(filePart.file.name).toEqual('atest.JPG');
                expect(filePart.file.replaceFile).toEqual('atest.JPG');
                
                filePart.undoReplace();
                expect(filePart.file.action).toEqual('ADD');
                vapp.$destroy();

            });
        });

        
        it("uploads progress", function() {
            var vapp = setupValidApp();

            Vue.nextTick(function() {
                vapp.updateFileProgress({}, {files: [{name:'test.JPG',size:1000,type:'jpg/image',lastModified:-1}], loaded:40, total:100});
                console.log("ALERT:");
                vapp.$destroy();
            });
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

            window.console.log("--------------------bbb---------------------");
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(2);

            
            window.console.log("--------------------ccc---------------------");
            vapp.$destroy();

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

            window.console.log("--------------------bbb---------------------");
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'tast.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(false);
            expect(vapp.files).toHaveLength(1);

            
            window.console.log("--------------------ccc---------------------");
            vapp.$destroy();

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

            window.console.log("--------------------bbb---------------------");
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
//            expect(vapp.validatePackage()).toBe(false);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jgw',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(2);
            expect(vapp.validatePackage()).toBe(true);

            
            window.console.log("--------------------ccc---------------------");
            vapp.$destroy();

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

            window.console.log("--------------------bbb---------------------");
            var vapp = TDAR.vuejs.uploadWidget.init("#uploadWidget");
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jgw',size:1000,type:'jgw/object',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            expect(vapp.validatePackage()).toBe(false);
            result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(2);
            expect(vapp.validatePackage()).toBe(true);

            
            window.console.log("--------------------ccc---------------------");
            vapp.$destroy();

        });
  
});
