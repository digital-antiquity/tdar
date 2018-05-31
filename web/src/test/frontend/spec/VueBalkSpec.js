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

       var _fix = undefined;
       
        beforeEach(function() {
            // change the fixture path to point to our template
            jasmine.getFixtures().fixturesPath = "base/src/main/webapp/WEB-INF/content/dashboard/";
            // read the fixture as a string
            var fixture = jasmine.getFixtures().read("balk.html");
            
            // set some IDs
            // apply the fixxture
            
            var extra = "<script id='validFormats'>"+JSON.stringify(["tif","pdf","doc"])+"</script>" + 
            "<script id='accountJson'>"+JSON.stringify([{name:"full service", id:1, fullService:true, initialReview:true}, {name:"normal", id:2, fullService:false}])+"</script>";
            
            fixture = fixture.replace('<script id="insert"></script>',extra);
            _fix = jasmine.getFixtures().set(fixture);
            jasmine.Ajax.install();

        });

        afterEach(function() {
            jasmine.Ajax.uninstall();
            
        });
        

        function initController() {
            // initialize the app
            var __vapp = TDAR.vuejs.balk.init("#filesTool");
            // get a constructor
            var Constructor = Vue.extend(__vapp.balk);
            // count the contructor (get back to the app from the router)
            var vapp = new Constructor().$mount();
            expect(vapp == undefined).toBe(false);
            expect(_fix == undefined).toBe(false);
            // return the constructed component so we can execute methods on it
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });
              
            jasmine.Ajax.stubRequest(/.+file\/list/,/.+/).andReturn({
                "responseText": '{}',
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
              });
              jasmine.Ajax.stubRequest('/api/file/listDirs').andReturn({
                "responseText": '{}'
              });
            return vapp;
        }
        

        function initFileComponent() {
            // initialize the app
            var __vapp = TDAR.vuejs.balk.init("#filesTool");
            // get a constructor
            var Constructor = Vue.extend(__vapp.files);
            // count the contructor (get back to the app from the router)
            var vapp = new Constructor().$mount();
            expect(vapp == undefined).toBe(false);
            expect(_fix == undefined).toBe(false);
            // return the constructed component so we can execute methods on it
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });
              
            jasmine.Ajax.stubRequest(/.+file\/list/,/.+/).andReturn({
                "responseText": '{}',
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
              });
              jasmine.Ajax.stubRequest('/api/file/listDirs').andReturn({
                "responseText": '{}'
              });
              vapp.file = {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false};

            return vapp;
        }

        it("accepts valid files", function() {
            var conf = getBaseConfig();
            setConfig(conf);            

            var vapp = initController();
            var result = vapp.fileUploadAdd({}, {originalFiles:[{name:'test.jpg',size:1000,type:'jpg/image',lastModified:-1, selected:false}]});
            expect(result).toBe(false);
            expect(vapp.files).toHaveLength(0);
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.tif',size:1000,type:'tif/image',lastModified:-1, selected:false}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            vapp.$destroy();
        });


        it("handles grouping and selection", function() {
            var conf = getBaseConfig();
            setConfig(conf);


            var vapp = initController();

            var result = vapp.fileUploadAdd({}, {originalFiles:[{id:1, name:'test.doc', extension:'doc', size:1000,type:'doc/document',lastModified:-1, selected:false},
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}]});

            expect(vapp.files).toHaveLength(2);
            expect(vapp._cannotSelect()).toBe(true);
            expect(vapp._cannotCreateRecordFromSelected()).toBe(true);
            console.log("selected: ", vapp.selectedFiles);
            expect(vapp.selectedFiles).toHaveLength(0);

            vapp.selectedFiles = vapp.files;
            expect(vapp.selectedFiles).toHaveLength(2);
            
            
            expect(vapp._cannotSelect()).toBe(false);
            expect(vapp._cannotCreateRecordFromSelected()).toBe(true);
            expect(vapp._selectedFileNames() == "test.doc; test.tif").toBe(true);
 
        // });
            vapp.$destroy();
        });
  

        it("handles create record selection", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();

            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false},
                {id:3, name:'test2.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];
            
            vapp.selectedFiles = vapp.files;
            expect(vapp.selectedFiles).toHaveLength(2);
            
            expect(vapp._cannotSelect()).toBe(false);
            expect(vapp._cannotCreateRecordFromSelected()).toBe(false);
            expect(vapp._selectedFileNames() == "test.tif; test2.tif").toBe(true);
            
            // set a resourceId and see it break
            vapp.files[0].resourceId = 1234;
            expect(vapp._cannotCreateRecordFromSelected()).toBe(true);
            
            vapp.$destroy();
        });

        it("handles toggle selection", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();

            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false},
                {id:3, name:'test2.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];
            
            vapp.toggleSelect([true,vapp.files[0]]);
            expect(vapp.selectedFiles).toHaveLength(1);
            expect(vapp.selectedFiles[0].id == vapp.files[0].id).toBe(true);
            vapp.toggleSelect([true,vapp.files[1]]);
            expect(vapp.selectedFiles).toHaveLength(2);

            vapp.toggleSelect([false,vapp.files[0]]);
            expect(vapp.selectedFiles[0].id == vapp.files[1].id).toBe(true);
            expect(vapp.selectedFiles).toHaveLength(1);
            vapp.toggleSelect([false,vapp.files[1]]);
            expect(vapp.selectedFiles).toHaveLength(0);
            
            
            vapp.$destroy();
        });


        it("handles create record selection", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initFileComponent();
//            vapp.select();
            vapp.$destroy();
        });


        it("handles marking", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initFileComponent();
            vapp.markCurated();
            console.log("aaaaaaaaaaaaaaaaa");
            vapp.markInitialCurated();
            vapp.markReviewed();
            vapp.markExternalReviewed();
            
            vapp.$destroy();
        });

});
