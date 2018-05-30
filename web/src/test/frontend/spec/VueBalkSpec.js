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
            var Constructor = Vue.extend(__vapp);
            // count the contructor (get back to the app from the router)
            var vapp = new Constructor().$mount();
            expect(vapp == undefined).toBe(false);
            expect(_fix == undefined).toBe(false);
            // return the constructed component so we can execute methods on it
            return vapp;
        }

        it("accepts valid files", function() {
            var conf = getBaseConfig();
            setConfig(conf);            
            jasmine.Ajax.stubRequest('/upload/upload').andReturn({
                "responseText": 'success'
              });
              jasmine.Ajax.stubRequest('/api/file/list',/.+/).andReturn({
                "responseText": '{}'
              });
              jasmine.Ajax.stubRequest('/api/file/listDirs').andReturn({
                "responseText": '{}'
              });

            var vapp = initController();
            var result = vapp.fileUploadAdd({}, {originalFiles:[{name:'test.jpg',size:1000,type:'jpg/image',lastModified:-1}]});
            expect(result).toBe(false);
            expect(vapp.files).toHaveLength(0);
            var result = vapp.fileUploadAdd(undefined, {originalFiles:[{name:'test.tif',size:1000,type:'tif/image',lastModified:-1}]});
            expect(result).toBe(true);
            expect(vapp.files).toHaveLength(1);
            vapp.$destroy();
        });


        it("handles grouping and selection", function() {
            var conf = getBaseConfig();
            setConfig(conf);
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

            var vapp = initController();

            var result = vapp.fileUploadAdd({}, {originalFiles:[{name:'test.doc',size:1000,type:'doc/document',lastModified:-1},{name:'test.tif',size:1000,type:'tif/image',lastModified:-1}]});
            // make VueJS "tick" or re-render
            // Vue.nextTick(function() {
            expect(result).toBe(true);
            vapp.$forceUpdate();
            expect(vapp.files).toHaveLength(2);
            console.log("selected: ", vapp.selectedFiles);
            expect(vapp.selectedFiles).toHaveLength(0);
            // expect(vapp.collections.unmanaged[0].name).toBe("Test");
            // var sel = _fix.find("#files-row-0 input[type=checkbox]");
            // console.log("sel",sel);
            // expect(sel == undefined).toBe(false);
            // console.log(_fix.html());
            var sel = $("#files-row-0 input[type=checkbox]");
            console.log("sel",sel);
            sel.click();
            // expect(sel.length).toHaveLength(1);
            // // vapp.$nextTick(function(){
            // console.log("selected: ", vapp.selectedFiles);
            // console.log(vapp.selectedFiles);
            // console.log('fixture:',_fix);
            // expect(vapp.selectedFiles).toHaveLength(1);
            // done();
            //     Vue.nextTick(function() {
            // });
            // expect(vapp.selectedFiles).toHaveLength(1);
 
        // });
            vapp.$destroy();
        });
  
});
