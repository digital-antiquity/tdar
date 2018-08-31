/* global describe, it, xit, expect, beforeEach, afterEach, loadFixtures */

const Vue = require("vue/dist/vue.esm.js").default;

xdescribe("BalkSpec.js: fileupload suite - root", function(){
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
            "<script id='accountJson'>"+JSON.stringify([
                    {name:"full service", id:1, fullService:true, initialReview:true, detailUrl:'/account/1'},
                    {name:"normal", id:2, fullService:false, detailUrl:'/account/1'}
                    ])+"</script>";
            
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
            vapp.selectedAccount = vapp.accounts[0];
            vapp.accountId = vapp.selectedAccount.id;
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
                "responseText": '[]',
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
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
        
        
        function initDirComponent() {
            // initialize the app
            var __vapp = TDAR.vuejs.balk.init("#filesTool");
            // get a constructor
            var Constructor = Vue.extend(__vapp.dir);
            // count the contructor (get back to the app from the router)
            var vapp = new Constructor().$mount();
            expect(vapp == undefined).toBe(false);
            expect(_fix == undefined).toBe(false);
            // return the constructed component so we can execute methods on it
            return vapp;
        }
        

        function initPentryComponent() {
            // initialize the app
            var __vapp = TDAR.vuejs.balk.init("#filesTool");
            // get a constructor
            var Constructor = Vue.extend(__vapp.pentry);
            // count the contructor (get back to the app from the router)
            var vapp = new Constructor().$mount();
            expect(vapp == undefined).toBe(false);
            expect(_fix == undefined).toBe(false);
            // return the constructed component so we can execute methods on it
            return vapp;
        }
        

        function initCommentComponent() {
            // initialize the app
            var __vapp = TDAR.vuejs.balk.init("#filesTool");
            // get a constructor
            var Constructor = Vue.extend(__vapp.comments);
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
            
            vapp.comment = {id:2, comment:'test', resolved:false };
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
            
            expect(vapp._createRecordFromSelected()).toEqual('/resource/createRecordFromFiles?&fileIds=2&fileIds=3');

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
            
            vapp.toggleSelect(vapp.files[0]);
            expect(vapp.selectedFiles).toHaveLength(1);
            console.log("selected" ,vapp.selectedFiles[0].id);
            expect(vapp.selectedFiles[0].id == vapp.files[0].id).toBe(true);
            vapp.toggleSelect(vapp.files[1]);
            expect(vapp.selectedFiles).toHaveLength(2);

            vapp.toggleSelect(vapp.files[0]);
            expect(vapp.selectedFiles[0].id == vapp.files[1].id).toBe(true);
            expect(vapp.selectedFiles).toHaveLength(1);
            vapp.toggleSelect(vapp.files[1]);
            expect(vapp.selectedFiles).toHaveLength(0);
            
            
            vapp.$destroy();
        });
        
        it("handles adding a comment", function(){
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();

            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false},
                ];
            
            vapp.commentFile = vapp.files[0];
            vapp.comment = 'abcd';
            vapp.addComment();
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/comment/add');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"id" : ["2"], "comment": ["abcd"]});
        });
        
        it("handles show comment", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();

            jasmine.Ajax.stubRequest('/api/file/comment/list?id=2').andReturn({
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK',
                "responseText": JSON.stringify([])
              });
              

            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];
            vapp.commentFile = vapp.files[0];
            
            vapp.showComments(vapp.files[0]);
            // FIXME get AJAX to work and get RESULT
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/comment/list?id=2');
            expect(request.method).toBe('GET');
            console.log("comments", vapp.comments);
            vapp.$destroy();
        });

        it("uploads progress", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();

            Vue.nextTick(function() {
                vapp.updateFileProgress({}, {files: [{name:'test.JPG',size:1000,type:'jpg/image',lastModified:-1}], loaded:40, total:100});
                console.log("ALERT:");
                vapp.$destroy();
            });
        });

        it("cds root", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();
            vapp.cd(undefined);
        });

        
        it("handles unmark", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();

            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];
            
            // role, name , initial, date, file

            vapp.unmark('ROLE','NAME','NAME-INITIAL', 'NAME-DATE',vapp.files[0]);
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/unmark');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"ids[0]" : ["2"], "role": ["ROLE"]});
            vapp.$destroy();
        });

        
        it("handles routing", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();
            jasmine.Ajax.stubRequest('/api/file/listDirs?accountId=2').andReturn({
                "responseText": '[{name:"test",id:5}]',
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
              });

            // role, name , initial, date, file
            // $.post("/api/file/unmark", {"ids[0]": id,"role":role}).done(function(files){
            vapp.dirs = [];
            vapp._routeAccounts({params:{accountId: 2, dir: 'test'}}, {});
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/listDirs?accountId=2');
            expect(request.method).toBe('GET');
            vapp.$destroy();
        });


        it("handles search", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();
            jasmine.Ajax.stubRequest('/api/file/list?term=test?accountId=1').andReturn({
                "responseText": '[{name:"test",id:5}]',
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
              });

            vapp.searchFiles("test");
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/list?term=test&accountId=1');
            expect(request.method).toBe('GET');
            vapp.$destroy();
        });

        
        it("handles deleteFile", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();
            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false},
                {id:3, name:'test2.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];

            jasmine.Ajax.stubRequest('/api/file/delete').andReturn({
                "responseText": JSON.stringify(vapp.files[0]),
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
            });
            vapp.deleteFile(vapp.files[0]);
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/delete');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"id" : ["2"]});
            vapp.$destroy();
        });

        
        it("creates directory", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();
            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false},
                {id:3, name:'test2.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];

            $("#dirName").val(undefined);
            vapp.mkdir();
            var request = jasmine.Ajax.requests.mostRecent();
            // expect that it's a NO-OP
            expect(request.url).toBe('/api/file/list?accountId=1');
            expect(request.method).toBe('GET');
            
            $("#dirName").val("");
            vapp.mkdir('');
            request = jasmine.Ajax.requests.mostRecent();
            // expect that it's a NO-OP
            expect(request.url).toBe('/api/file/list?accountId=1');
            expect(request.method).toBe('GET');
            
            vapp.dirName = "testdir";
            vapp.parentId = 100;
            vapp.mkdir();
            request = jasmine.Ajax.requests.mostRecent();
            // expect that it's a NO-OP
            expect(request.url).toBe('/api/file/mkdir');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"name" : ["testdir"],"accountId":['1'],"parentId":['100']});

//            expect(request.data()).toEqual({"id" : ["2"]});
            vapp.$destroy();
        });


        it("moves files to", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initController();
            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false},
                {id:3, name:'test2.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];
            
            vapp.selectedFiles = vapp.files;
            vapp.moveSelectedFilesTo({id:1000, name:'test'});
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/move');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"toId" : ["1000"], "ids[0]": ["2"], "ids[1]": ["3"]});

            vapp.$destroy();
        });


        it("cancels move", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initController();
            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false},
                {id:3, name:'test2.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];
            
            vapp.selectedFiles = vapp.files;
            vapp.cancelMove();
            expect(vapp.selectedFiles.length).toBe(0);

            vapp.$destroy();
        });

        
        
        it("moves files to account", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initController();
            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false},
                {id:3, name:'test2.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];
            
            vapp.selectedFiles = vapp.files;
            vapp.moveSelectedFilesToAccount(1000);
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/moveToAccount');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"toAccountId" : ["1000"], "ids[0]": ["2"], "ids[1]": ["3"]});

            vapp.$destroy();
        });
        
        
        it("lists directories", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initController();

            jasmine.Ajax.stubRequest('/api/file/listDirs?accountId=1').andReturn({
                "responseText": JSON.stringify([]),
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
            });
            
            vapp.listDirs(function(){});
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/listDirs?accountId=1');
            expect(request.method).toBe('GET');

            vapp.$destroy();
        });

        // don't know how to call 'computed'
        xit("moves up one  directory", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initController();
            expect(vapp.upOne()).toBe(undefined);
            var dirstack = [{name:'top'},{name:'middle'}, {name:'bottom'}];
            vapp.dirStack = dirstack;
            expect(vapp.upOne()).toBe({name:'middle'});

            vapp.$destroy();
        });

        // doesn't work
        xit("rename dir", function() {
            $("#rename").val("test");
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initController();
            vapp.showRename();
            vapp.renameDir();
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/list?accountId=1&sortBy=NAME');
            expect(request.method).toBe('POST');
            vapp.$destroy();
        });

        it("doesn't rename dir", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initController();
            vapp.showRename();
            vapp.renameDir();
            var request = jasmine.Ajax.requests.mostRecent();
            //rename does nothing
            expect(request.url).toBe('/api/file/list?accountId=1');
            expect(request.method).toBe('GET');

            vapp.$destroy();
        });

        it("sort dir", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initController();
            vapp.sortBy('NAME');
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/list?accountId=1&sortBy=NAME');
            expect(request.method).toBe('GET');
            // expect(request.data()).toEqual({"ids[0]" : ["2"], "role": ["CURATED"]});

            vapp.$destroy();
        });
        

        it("handles create record selection", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initFileComponent();
            // vapp.select();
            vapp.$destroy();
        });


        
        it("curate/uncurate", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();
            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastfModified:-1, extension:'tif', selected:false},
                {id:3, name:'test2.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                ];
            // assign resourceId
            vapp.commentFile = vapp.files[0];
            var file = vapp.files[0];
            file.resourceId=123;
            file.dateCurated = new Date();
            file.curatedBy = "test user";
            
            
            jasmine.Ajax.stubRequest('/api/file/editMetadata').andReturn({
                "responseText": JSON.stringify({curation:'NOT_CURATE'}),
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
            });

            vapp.unCurate();
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/editMetadata');
            expect(request.method).toBe('POST');
        });


        it("unmarks comment", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initController();
            vapp.files = [
                {id:2, name:'test.tif',size:1000,type:'tif/image',lastfModified:-1, extension:'tif', selected:false}
            ];
            // assign resourceId
            vapp.commentFile = vapp.files[0];
            var file = vapp.files[0];
            file.resourceId=123;
            file.dateCurated = new Date();
            file.curatedBy = "test user";

            jasmine.Ajax.stubRequest('/api/file/unmark').andReturn({
                "responseText": JSON.stringify({}),
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
            });

            vapp.unMarkComment({undoAction:'REVIEW', name:'test ', initial:'aa',date:new Date()})
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/unmark');
            expect(request.method).toBe('POST');
        });
        
        it("workflow progression", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initFileComponent();
            expect(vapp._canInitialReview()).toBe(false);
            expect(vapp._canCurate()).toBe(false);
            expect(vapp._cannotCurate()).toBe(false);
            expect(vapp._canReview()).toBe(false);
            expect(vapp._canExternalReview()).toBe(false);

            // assign resourceId
            vapp.file.resourceId=123;

            expect(vapp._canInitialReview()).toBe(false);
            expect(vapp._canCurate()).toBe(true);
            expect(vapp._cannotCurate()).toBe(false);
            expect(vapp._canReview()).toBe(false);
            expect(vapp._canExternalReview()).toBe(false);

            // assign resourceId
            vapp.file.resourceId = 123;

            expect(vapp._canInitialReview()).toBe(false);
            expect(vapp._canCurate()).toBe(true);
            expect(vapp._cannotCurate()).toBe(false);
            expect(vapp._canReview()).toBe(false);
            expect(vapp._canExternalReview()).toBe(false);

            // test that you cannot curate
            vapp.file.curation = "WONT_CURATE";

            expect(vapp._canInitialReview()).toBe(false);
            expect(vapp._canCurate()).toBe(false);
            expect(vapp._cannotCurate()).toBe(true);
            expect(vapp._canReview()).toBe(false);
            expect(vapp._canExternalReview()).toBe(false);

            // test that you can review
            vapp.file.curation = "CURATE";
            vapp.file.dateCurated = new Date();
            
            expect(vapp._canInitialReview()).toBe(false);
            expect(vapp._canCurate()).toBe(false);
            expect(vapp._cannotCurate()).toBe(true);
            expect(vapp._canReview()).toBe(true);
            expect(vapp._canExternalReview()).toBe(false);
            
            // test that you can inital review
            vapp.initialReviewed = true;
            expect(vapp._canInitialReview()).toBe(true);
            expect(vapp._canCurate()).toBe(false);
            expect(vapp._cannotCurate()).toBe(true);
            expect(vapp._canReview()).toBe(false);
            expect(vapp._canExternalReview()).toBe(false);
            
            // test that you can review
            vapp.initialReviewed = true;
            vapp.externalReviewed = true;
            vapp.file.dateInitialReviewed = new Date();
            expect(vapp._canInitialReview()).toBe(false);
            expect(vapp._canCurate()).toBe(false);
            expect(vapp._cannotCurate()).toBe(true);
            expect(vapp._canReview()).toBe(true);
            expect(vapp._canExternalReview()).toBe(false);

            // test that you can review
            vapp.file.dateInitialReviewed = new Date();
            expect(vapp._canInitialReview()).toBe(false);
            expect(vapp._canCurate()).toBe(false);
            expect(vapp._cannotCurate()).toBe(true);
            expect(vapp._canReview()).toBe(true);
            expect(vapp._canExternalReview()).toBe(false);

            // test that you can review
            vapp.file.dateReviewed = new Date();
            expect(vapp._canInitialReview()).toBe(false);
            expect(vapp._canCurate()).toBe(false);
            expect(vapp._cannotCurate()).toBe(true);
            expect(vapp._canReview()).toBe(false);
            expect(vapp._canExternalReview()).toBe(true);

            
            // vapp.select();
            vapp.$destroy();
        });


        it("handles _mark", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initFileComponent();
            vapp.file = {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false};

            jasmine.Ajax.stubRequest('/api/file/comment/mark').andReturn({
                "responseText": JSON.stringify([{role:'role', date:'date', name:"name", initials:'initials'}]),
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
            });
            
            vapp._mark("role", 'date', "name", "initials");
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/mark');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"ids[0]" : ["2"], "role": ["role"]});
        });
        
        it("handles marking", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initFileComponent();
            jasmine.Ajax.stubRequest('/api/file/comment/mark').andReturn({
                "responseText": JSON.stringify([{}]),
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
            });
            
            vapp.markCurated();
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/mark');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"ids[0]" : ["2"], "role": ["CURATED"]});

            vapp.markInitialCurated();
            request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/mark');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"ids[0]" : ["2"], "role": ["INITIAL_CURATED"]});

            vapp.markReviewed();
            request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/mark');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"ids[0]" : ["2"], "role": ["REVIEWED"]});
            
            vapp.markExternalReviewed();
            request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/mark');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"ids[0]" : ["2"], "role": ["EXTERNAL_REVIEWED"]});

            vapp.$destroy();
        });

        it("handles edits", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initFileComponent();
            vapp.file.note = '';
            expect(vapp._noteChanged()).toBe(false);
            expect(vapp._noteChanged()).toBe(false);
            vapp.updateNote();
            vapp.file.note = 'test';
            expect(vapp._noteChanged()).toBe(true);
            vapp.file.needsOcr = true;
            vapp.file.curation = 'CHOOSE';
            
            jasmine.Ajax.stubRequest('/api/file/editMetadata').andReturn({
                "responseText": JSON.stringify({}),
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
            });
            
            vapp.updateNote();
            var request = jasmine.Ajax.requests.mostRecent();
            expect(request.url).toBe('/api/file/editMetadata');
            expect(request.method).toBe('POST');
            expect(request.data()).toEqual({"id" : ["2"], "note": ["test"], "needOcr":['true'], 'curate':['CHOOSE']});
            vapp.wontCurate();
            vapp.$destroy();
        });

        it("handles parts", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initFileComponent();
            vapp.file.parts = [                {id:2, name:'test.tif',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false},
                                               {id:1, name:'test.tfw',size:1000,type:'tif/image',lastModified:-1, extension:'tif', selected:false}
                            ];
            
            console.log(vapp._partNames());
            expect(vapp._partNames()).toBe("test.tif; test.tif; test.tfw");
            vapp.$destroy();
        });

        it("handles delete", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initFileComponent();
            vapp.deleteFile();
            vapp.$destroy();
        });

        
        it("resolve comments", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initCommentComponent();
            jasmine.Ajax.stubRequest('/api/file/comment/resolve').andReturn({
                "responseText": JSON.stringify({resolverName:'a a', resolverInitals:'aa',dateResolved: new Date()}),
                status: 200,
                contentType: 'application/json',
                statusText: 'HTTP/1.1 200 OK'
            });

            vapp.resolveComment();
            vapp.$destroy();
        });

        it("undo comment", function() {
            var conf = getBaseConfig();
            setConfig(conf);

            var vapp = initCommentComponent();
            vapp.comment.undoAction = "UNDO_WONT_CURATE";
            vapp.undo();
            // FIXME: get "events" to test

            vapp.comment.undoAction = "REVIEWED";
            vapp.undo();
            // FIXME: get "events" to test
            vapp.$destroy();
        });

        it("formats dates", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            var vapp = initCommentComponent();
            var date = 'Wed Dec 29 2010 17:00:00 GMT-0700 (MST)';
            console.log(date);
            var formatted = vapp.formatDate(date);
            console.log("formatted",formatted);
            
            expect(formatted).toEqual('12/29/10');
            var long = vapp.formatLongDate(date);
            console.log("formatted long",long);
            expect(long).toEqual('12/29/2010, 5:00 PM');


            var blank = vapp.formatDate();
            console.log("formatted",blank);
            
            expect(blank).toEqual('');
            
            vapp.$destroy();
        });

        it("handles null dates", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initPentryComponent();
            expect(vapp.formatDate(undefined)).toBe('');
            expect(vapp.formatLongDate(undefined)).toBe('');

            vapp.$destroy();
        });

        
        it("moves dirs", function() {
            var conf = getBaseConfig();
            setConfig(conf);
            
            var vapp = initDirComponent();
            vapp.moveSelectedFilesTo({});
            vapp.$destroy();
        });
        
});
