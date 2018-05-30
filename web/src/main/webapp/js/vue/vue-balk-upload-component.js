TDAR.vuejs.balk = (function(console, $, ctx, Vue) {
    "use strict";

    var _app;
    var _pp;
    
    var _getApp = function () {
        return _app;
    }

    var _getPp = function () {
        return _pp;
    }

    var MAX_SELECTED_FILES = 50;
    /**
     * this tool handles the file-first upload management
     */
    
    /** format the data to a yy/mm/dd
     * 
     */
    var _formatDate = function(date) {
        if (date == undefined ) {
            return "";
        }
      return new Date(date).toLocaleString(['en-US'], {month: '2-digit', day: '2-digit', year: '2-digit'});
    }

    /**
     * format date with time
     */
    var _formatLongDate = function(date) {
        if (date == undefined ) {
            return "";
        }
      return new Date(date).toLocaleString(['en-US'], {month: '2-digit', day: '2-digit', year: 'numeric',
          hour:'2-digit', minute:'2-digit'});
    }

    var _init = function(appId) {

        /**
         * list a directory for the "move interface"
         */
        Vue.component("dir", {
            template:"#move-entry-template",
            props: ["dir", "index"],
            data: function() {return {
            }},
            mounted: function() {
            },
            methods: {
                moveSelectedFilesTo: function(dir) {
                    this.$parent.moveSelectedFilesTo(dir);
                }
            },
            computed: {
            }
        });

        /**
         * List a "Person" (Initial and URL)
         */
        Vue.component("pentry", {
            template:"#part-entry-template",
            props: ['date','initials','name', 'url'],
            data: function() {return {}},
            methods: {
                formatDate: function(date) {
                    return _formatDate(date);
                },
                formatLongDate: function(date) {
                    return _formatLongDate(date);
                }
            }
        });
        
        /**
         * Display a comment
         */
        Vue.component("comment", {
            template:"#comment-entry-template",
            props: ["comment", "fileid"],
            data: function() {return {
                resolved :false,
            }},
            mounted: function() {
                Vue.set(this,"resolved", this.comment.resolved);
            },
            methods: {
                formatDate: function(date) {
                    return _formatDate(date);
                },
                formatLongDate: function(date) {
                    return _formatLongDate(date);
                },
                resolveComment: function() {
                    // mark comment resolved on server and update the data locally
                    var _comment = this.comment;
                    console.log(_comment);
                    var _app = this;
                    var fileId = this.fileid;
                    console.log(_comment.id, fileId);
                    $.post("/api/file/comment/resolve", {"id": fileId,"commentId":_comment.id}).done(function(comments){
                        Vue.set(_comment, "resolverName", comments.resolverName);
                        Vue.set(_comment, "resolverInitals", comments.resolverInitials);
                        Vue.set(_comment, "dateResolved", comments.dateResolved);
                        Vue.set(_comment, "resolved", true);
                        Vue.set(_app, "resolved", true);
                    });
                },
                undo: function() {
                    if (this.comment.undoAction == 'UNDO_WONT_CURATE') {
                        var file = this.$parent.commentFile;
                        $.post("/api/file/editMetadata", {"id": file.id,"note":file.note, "needOcr":file.requiresOcr, "curate":"CHOOSE"}).done(function(_file){
                            Vue.set(file,"curation",_file.curation);
//                            Vue.set(file,"curation",file.curation);
                        });
                    } else {
                        this.$parent.unmark(this.comment.undoAction, this.comment.name, this.comment.initial, this.comment.date, this.$parent.commentFile);
                    }
                },
                assignComment: function() {
                    
                }
            },
            computed: {
                commentClass: function() {
                    // comment.type is manually defined as "internal" for "fake" comments created for actions
                    // like record created, reviewed, etc.
                    if (this.comment.type == undefined) {
                        return "comment";
                    }
                    return this.comment.type + " comment";
                }
            }
        });

        /**
         * A "row" in the file tool for 1 entry
         */
        Vue.component('fileEntry', {
            template : "#file-entry-template",
            props : [ "file", "index", "editable" , "initialreviewed", "externalreviewed", "fullservice"],
            data : function() {
                return {
                    previousDeleteState : '',
                    xhr : undefined,
                    previousReplaceState : ''
                }
            },
            mounted: function() {
            },
            methods: {
                cd : function(file) {
                        this.$parent.cd(file);
                },
                _mark: function(role, date, name, initials) {
                    /**
                     * mark a file as reviewed or curated based on role
                     */
                    var id = this.file.id;
                    var _file= this.file;
                    var ret = {};
                    $.post("/api/file/mark", {"ids[0]": id,"role":role}).done(function(files){
                        ret = files[0];
                        console.log(ret);
                        Vue.set(_file, date, ret[date]);
                        Vue.set(_file, name, ret[name]);
                        Vue.set(_file, initials, ret[initials]);
                    });
                    return ret;
                },
                select: function() {
                    this.$parent.toggleSelect(this.file.selected, this.file);
                },
                markCurated: function() {
                    var ret = this._mark("CURATED","dateCurated", "curatedByName", "curatedByInitials");
                },
                markInitialCurated: function() {
                    var ret = this._mark("INITIAL_CURATED", "dateInitialReviewed", "initialReviewedByName", "initialReviewedByInitials");
                },
                markReviewed: function() {
                    var ret = this._mark("REVIEWED", "dateReviewed", "reviewedByName","reviewedByInitials");
                },
                markExternalReviewed: function() {
                    var ret = this._mark("EXTERNAL_REVIEWED", "dateExternalReviewed", "externalReviewedByName", "externalReviewedByInitials");
                },
                _editMetadata: function (note, ocr, curate) {
                    /**
                     * for OCR, note, and curation, there is no "mark" so we send the metadata directly
                     */
                    var id = this.file.id;
                    var _file= this.file;
                    var _app = this;
                    var ret = {};
                    console.log("_editMetaadata", note,ocr,curate);
                    $.post("/api/file/editMetadata", {"id": id,"note":note, "needOcr":ocr, "curate":curate}).done(function(file){
                        ret = file;
                        Vue.set(_app.file,"initialNote",file.note);
                        Vue.set(_app.file,"curation",file.curation);
                        Vue.set(_app.file,"requiresOcr",file.requiresOcr);
                        Vue.set(_file,"initialNote",file.note);
                    });

                },
                wontCurate: function() {
                    this._editMetadata(this.file.note, this.file.needsOcr, "WONT_CURATE");
                },
                updateNote: function() {
                    this._editMetadata(this.file.note, this.file.needsOcr, this.file.curation);
                },
                showComments: function() {
                    // load the comments and show the comment modal
                    this.$parent.showComments(this.file);
                },
                deleteFile: function(){
                    // delet the file
                    if (confirm("Are you sure you want to delete: "+this.file.name+"?")) {
                        console.log('delete');
                        this.$parent.deleteFile(this.file);
                    } 

                }
            },
            computed: {
                partNames: function() {
                    var ret = "";
                    if (this.file.parts == undefined || this.file.parts.length == 0) {
                        return "";
                    }
                    ret = this.file.name;
                    for (var i =0; i < this.file.parts.length; i++) {
                        ret = ret + "; " + this.file.parts[i].name;
                    }
                    return ret;
                },
                noteChanged: function() {
                    // watch the note property and show a "save" button when there are differenes
                    var _note = this.file.note;
                    var note = this.file.initialNote;
//                    console.log(this.file.id, _note, note);
                    if ((_note == undefined || _note == '') && (note == undefined || note == '')) {
                        return false;
                    }
                  if (note == _note) {
                      return false;
                  }
                  return true;
                },
                fileLink : function() {
                    // compute the link to create a record from the file
                    return "/resource/createRecordFromFiles?fileIds=" + this.file.id; 
                },
                downloadUrl : function() {
                    // downlaod URL for a file
                    return "/file/download/" + this.file.id; 
                },
                rowId : function() {
                    return "files-row-" + this.index;
                },
                canInitialReview: function () {
                    // if initial hasn't reviewed
                    if (this.file.dateInitialReviewed != undefined) {
                        return false;
                    }

                    // and it's been curated and the account supports  review...
                    if (this.file.initialReviewed && this.file.dateCurated != undefined) {
                        return true;
                    }
                    return false;
                },
                canCurate: function () {
                    // if it hasn't been curated and we have a resource id, then yes
                    if (this.file.dateCurated != undefined) {
                        return false;
                    }
                    if (this.file.curation == "WONT_CURATE") {
                        return false;
                    } 
                    if (this.file.resourceId != undefined) {
                        return true;
                    }
                    return false;
                },
                cannotCurate: function() {
                    // if we're a dir, curated is false (not undefined)  
                    if (this.file.size == 0 || this.file.size == undefined) {
                        return true;
                    }
                    
                    if (this.file.curation == "WONT_CURATE") {
                        return true;
                    }

                    if (this.file.dateCurated == undefined ) {
                        return false;
                    }
                    return true;
                },
                canReview: function () {
                    // either (a) done  review, or curated
                    if (this.file.dateReviewed != undefined) {
                        return false;
                    }
                    if (this.initialReviewed && this.file.dateInitialReviewed != undefined || 
                            this.initialReviewed != true && this.file.dateCurated != undefined) {
                        return true;
                    }
                    return false;
                },
                canExternalReview: function () {
                    // if externally reviewed and has gone through normal review
                    if (this.file.dateExternalReviewed != undefined) {
                        return false;
                    }
                    if (this.externalReviewed && this.file.dateReviewed != undefined) {
                        return true;
                    }
                    return false;
                },
                
            }
        });

        /** config for widget
         * 
         */
        var config = {
                fullService : true,
                initialReviewed : false,
                externalReviewed : true,
                validFormats: JSON.parse($("#validFormats").text()),
                accounts: JSON.parse($("#accountJson").text())
        };

        
        /**
         * the main app
         */
        var app = Vue.component("balk",{
            template : '#balk-template',
            data : function() {
                return {
                validFormats : config.validFormats,
                ableToUpload : true,
                parentId: undefined,
                parent: undefined,
                initialReviewed: false,
                externalReviewed: false,
                daysFilesExpireAfter: 60,
                fullService: false,
                accounts: config.accounts,
                accountId: undefined,
                moveAccountId: undefined,
                files : [],
                errors: [],
                comment: "",
                comments: [],
                dirTree: [],
                selectedFiles: [],
                dirStack: [],
                search: "",
                commentFile:undefined,
                path : "" }
            },
            computed : {
                inputDisabled : function() {
                    return !this.ableToUpload;
                },
                upOneId: function() {
                  if (this.dirStack.length < 2) {
                      return undefined;
                  }
                  return this.dirStack[this.dirStack.length -2].id;
                },
                _cannotSelect: function() {
                    if (this.selectedFiles == undefined || this.selectedFiles.length == 0 || this.selectedFiles.length > MAX_SELECTED_FILES) {
                        return true;
                    }
                    return false;
                },
                cannotMoveSelected: function() {
                    return this._cannotSelect;
                },
                selectedFileNames: function() {
                    var ret = "";
                    if (this.selectedFiles == undefined || this.selectedFiles.length == 0) {
                        return "";
                    }
                    
                    for (var i =0; i < this.selectedFiles.length; i++) {
                        if (i > 0) {
                            ret = ret + "; ";
                        }
                        ret = ret + this.selectedFiles[i].name;
                    }

                    return ret;
                },
                cannotCreateRecordfromSelected: function() {
                    if (this._cannotSelect) {
                        return true;
                    }
                    var ext = "";
                    this.selectedFiles.forEach(function(file){
                        var _ext = "BAD";
                        
                        if (file.resourceId != undefined) {
                            ext = "BAD";
                            return;
                        }
                        
                        if (file != undefined && file.extension != undefined) {
                            _ext = file.extension.toLowerCase();
                        }
                        if (_ext == undefined || _ext == '') {
                            ext = "BAD";
                            return;
                        }
                        
                        if (ext == '' || ext == _ext) {
                            ext = _ext;
                        } else {
                            ext = "BAD";
                        }
                    });
                    if (ext == "BAD") {
                        return true;
                    }
                    return false;
                }

            },
            watch: {
                search: function(after, before) {
                   // when the search variable changes, then send the search to the server 
                    console.log(before, after);
                    if (after == undefined || after == "") {
                        this.loadFiles(this.parentId, this.path);
                    } else {
                        this.searchFiles(after);
                    }
                },
                accountId: function(after,before) {
                    // watch the account id and change account
                    this.switchAccount(after);
                },
                moveAccountId: function(after,before) {
                    this.moveSelectedFilesToAccount(after);
                },
                '$route': function(to, from) {
                    console.log(">>> changed route to: ", to);
//                    console.log(to.params.accountId , this.accountId);
                    var _path = this.fullPath().substring(1);
                    if (to.params.accountId != this.accountId) {
                        Vue.set(this,"accountId", to.params.accountId);
                        this.switchAccount(to.params.accountId);
                        _path = undefined;
                    }
                    
//                    console.log(_path, to.params.dir, _path == to.params.dir);
                    if (to.params.dir != _path) {
                        var _app = this;
                        var dirName = to.params.dir;
                        if (dirName != undefined && dirName.lastIndexOf("/") != -1) {
                            dirName = dirName.substring(dirName.lastIndexOf("/") +1)
                        }
                        $.get("/api/file/listDirs", {accountId: _app.accountId}).done(function(dirs) {
                             // create parent map
                            var match = undefined;
                            var dirMap = {};
                             dirs.forEach(function(dir) {
                                 dirMap[dir.id] = dir;
                                 if (dir.name == dirName) {
                                     match = dir;
                                 } 
                             });
                             var stack = [];
                             // get all parent directories and rebuild dirStack
                             if (match != undefined) {
                                 var d = match;
                                 while (d.parentRef != undefined) {
                                     var p = dirMap[d.parentRef.replace("TdarDir:","")];
                                     stack.unshift(p);
                                     d = p;
                                 }
                                 }
                             Vue.set(_app,"dirStack", stack);
                             console.log("redirecting to : ", match, stack);
                             _app.cd(match, false);
                             return;
                        });
//                        this.switchAccount(to.params.accountId);
                    }

                }
            },
            methods : {
                fullPath: function() {
                    var ret = "";
                    this.dirStack.forEach(function(d){
                        if (d != undefined) {
                            ret = ret + "/" + d.name; 
                        }
                    });
                    return ret;
                },
                switchAccount: function(accountId) {
                    router.push({ path: '/' + accountId })
                    // reset the dir tree, and other state variables
                    Vue.set(this,"commentFile",undefined);
                    Vue.set(this,"dirTree",[]);
                    Vue.set(this,"moveAccountId",undefined);
                    Vue.set(this,"dirStack",[]);
                    Vue.set(this,"selectedFiles",[]);
                    this.loadFiles(undefined,undefined);
                    var _app = this;

                    // load state for accounts from properties  
                    this.accounts.forEach(function(account){
                        if (account.id == accountId) {
                            Vue.set(_app,"fullService",account.fullService);
                            Vue.set(_app,"daysFilesExpireAfter",account.daysFilesExpireAfter);
                            Vue.set(_app,"initialReviewed",account.initialReviewed);
                            Vue.set(_app,"externalReviewed",account.externalReviewed);
                        }
                    });
                 },
                 parentDir: function() {
                     if (this.dirStack.length > 1) {
                         return this.dirStack[this.dirStack.length -2];
                     }
                     return undefined;
                 },
                loadFiles: function (parentId, path) {
                    // load all of the files for the dir and account
                    router.push({ path: '/' + this.accountId  + this.fullPath()})
                    var _app = this;
                    $.get("/api/file/list", {"parentId": parentId, "accountId":_app.accountId}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        console.log('LIST FILES:',JSON.stringify(msg));
                        Vue.set(_app,"files", msg);
                    });
                    Vue.set(this, "parentId", parentId);
                    Vue.set(this, "path", path);
                },
                searchFiles: function (search) {
                    // send a search
                    var _app = this;
                    console.log(search);
                    $.get("/api/file/list", {"term":search, "accountId":_app.accountId}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        console.log('SEARCH FILES:',JSON.stringify(msg));
                        Vue.set(_app,"files", msg);
                    });
                },
                unmark: function(role, name , initial, date,  file) {
                    // undo the mark action (curated, reviewed, etc.)
                    var id = file.id;
                    var _file= file;
                    var ret = {};
                    $.post("/api/file/unmark", {"ids[0]": id,"role":role}).done(function(files){
                        ret = files[0];
                        console.log(ret);
                        Vue.set(_file, date, undefined);
                        Vue.set(_file, name, undefined);
                        Vue.set(_file, initial, undefined);
                    });
                    return ret;
                },
                toggleSelect: function(existing, incoming) {
                    var id = incoming.id;
//                    console.log("toggle select", existing, id);
                    var seen = false;
                    var indexToRemove = -1;
                    for (var i =0; i < this.selectedFiles.length; i++) {
                        if (id == this.selectedFiles[i].id) {
                            seen = true;
                            indexToRemove = i;
                        }
                    }
                    
                    if (existing == true) { // add
                        if (seen == false)  {
                            this.selectedFiles.push(incoming);
                            incoming.selected = true;
                        }
                    } else { // remove
                        if (indexToRemove > -1) {
                            var ret = this.selectedFiles.splice(indexToRemove,1);
                            ret.selected = false;
                        }
                    }
                },
                showComments: function(file) {
                    // show the comments modal, and load it with some "fabricated" comments that are actually "mark" actions
                    var _app = this;
                    Vue.set(_app,"commentFile",file);
                    console.log(this.commentFile);
                    $.get('/api/file/comment/list', {"id": file.id}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        msg.push({comment:'uploaded', dateCreated: file.dateCreated, commentorName: file.uploaderName, commentorInitials: file.uploaderInitials, type:'internal'});
                        if (file.dateCurated != undefined) {
                            msg.push({comment:'curated', undoAction:"CURATED", initial: 'curatedByInitials', date:'dateCurated', name:'curatedBy', dateCreated: file.dateCurated, commentorName: file.curatedByName, commentorInitials: file.curatedByInitials, type:'internal'});
                        }
                        if (file.dateReviewed != undefined) {
                            msg.push({comment:'reviewed',undoAction:"REVIEWED", initial: 'reviewedByInitials', date:'dateReviewed', name:'reviewedBy', dateCreated: file.dateReviewed, commentorName: file.reviewedByName, commentorInitials: file.reviewedByInitials, type:'internal'});
                        }
                        if (file.dateInitialReviewed != undefined) {
                            msg.push({comment:'initiial reviewed', undoAction:"INITIAL_REVIEWED", initial: 'initialReviewedByInitials', date:'dateInitialReviewed', name:'initialReviewedBy', dateCreated: file.dateInitialReviewed, commentorName: file.initialReviewedByName, commentorInitials: file.initialReviewedByInitials, type:'internal'});
                        }
                        if (file.dateExternalReviewed != undefined) {
                            msg.push({comment:'external reviewed', undoAction:"EXTERNAL_REVIEWED",initial: 'externalReviewedByInitials', date:'dateExternalReviewed', name:'externalReviewedBy', dateCreated: file.dateExternalReviewed , commentorName: file.externalReviewedByName, commentorInitials: file.externalReviewedByInitials, type:'internal'});
                        }

                        if (file.curation == "WONT_CURATE") {
                            msg.push({comment:'declined curation', undoAction:"UNDO_WONT_CURATE", type:'internal'});
                        }

                        if (file.resourceId != undefined) {
                            msg.push({comment:'tDAR Resource Created id:' + file.resourceId, dateCreated: file.dateResourceCreated , commentorName: file.resourceCreatedByName, commentorInitials: file.resourceCreatedByInitials, type:'internal'});
                        }

                        // sort by date ascending
                        msg.sort(function(a,b){
                            // Turn your strings into dates, and then subtract them
                            // to get a value that is either negative, positive, or zero.
                            return new Date(a.dateCreated) - new Date(b.dateCreated);
                          });
                        Vue.set(_app,"comments", msg);
                        $("#comments-template-modal").modal('show');
                    });
                    
                },
                addComment: function() {
                    // add a comment to a file
                    console.log('add comment', this.comment);
                    var _app = this;
                    if (this.comment == undefined || this.comment == '') {
                        return;
                    }
                    $.post("/api/file/comment/add", {"id": this.commentFile.id,"comment": this.comment}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        _app.comments.push(msg);
                        Vue.set(_app,"comment","");
                    });
                },
                deleteFile: function(file) {
                    // delete a file (not including UI, so we assume the user has been asked)
                    console.log('delete file!');
                    var id = file.id;
                    var _file= file;
                    var _app = this;
                    $.post("/api/file/delete", {"id": id}).done(function(file){
                        console.log(file);
                        var index = $.inArray(_file, _app.files);
                        if (index != -1) {
                            _app.files.splice(index, 1);
                        }
                    });

                },
                mkdir: function() {
                    // create a directory
                  var dir = this.dir;
                  var _app = this;
                  $.post("/api/file/mkdir", {"parentId": _app.parentId, "name": $("#dirName").val() , accountId: _app.accountId}
                    ).done(function(msg) {
                        _app.files.push(msg);
                        $("#dirName").val("");
                    });
                },
                moveUI: function() {
                    // show the move UI
                    var _app = this;
                  var dirs = this.listDirs(function(){
                      console.log(_app.dirTree);
                      $("#move-template-modal").modal('show');
                  });
                },
                createRecordFromSelected: function() {
                    this.createRecordFromFiles(this.selectedFiles);
                },
                createRecordFromFiles: function(files) {
                    //var data = this._expandIds({},files);
                    var url = "/resource/createRecordFromFiles?";
                    files.forEach(function(file){
                        url = url +  "&fileIds=" + file.id; 
                    });
                    console.log(files, url);
                    window.location.href = url;
                },

                moveSelectedFilesTo: function(dir) {
                    // called by UI 
                    this.moveFiles(this.selectedFiles, dir);
                },
                moveFiles: function(files, dir) {
                    // move selected files to directory
                    var _app = this;
                    var data = this._expandIds({"toId": dir.id},files);
                    $.post("/api/file/move", data
                      ).done(function(msg) {
                          _app.cancelMove();
                          _app.loadFiles(_app.parentId, _app.path);
                      });

                },
                _expandIds: function(data, files) {
                  for (var i=0; i < files.length; i++) {
                      data['ids['+i+']'] = files[i].id;
                  }
                  return data;
                },
                moveSelectedFilesToAccount: function(accountId) {
                    // called by UI 
                    this.moveFilesToAccount(this.selectedFiles, accountId);
                },
                moveFilesToAccount: function(files, accountId) {
                    // move selected files to directory
                    var _app = this;
                    var data = this._expandIds({"toAccountId": accountId},files);
                    $.post("/api/file/moveToAccount", data
                      ).done(function(msg) {
                          _app.cancelMove();
                          _app.loadFiles(_app.parentId, _app.path);
                      });

                },
                cancelMove: function() {
                    // close modal
                    $("#move-template-modal").modal('hide');
                    Vue.set(this, "dirTree", []);
                    this.selectedFiles.forEach(function(file) {
                        if (file != undefined && file.selected != undefined) {
                            file.selected = false;
                        }
                    });
                    Vue.set(this, "selectedFiles", []);
                },
                listDirs: function(callback) {
                    // list all directories for an account (used by move UI)
                    var _app = this;
                   $.get("/api/file/listDirs", {accountId: _app.accountId}).done(function(dirs) {
                        var rootDirs = [];
                        var dirMap = {};
                        // create parent map
                        dirs.forEach(function(dir) {
                            dirMap[dir.id] = dir; 
                        });
                        dirs.forEach(function(dir) {
                            if (dir.parentRef != undefined) {
                                var parent = dirMap[dir.parentRef.replace("TdarDir:","")];
                                if (parent.children == undefined) {
                                    parent.children = [];
                                }
                                parent.children.push(dir);
                            } else {
                                rootDirs.push(dir);
                            }
                         });
                        Vue.set(_app, "dirTree", rootDirs);
                        console.log(_app.dirTree);
                        callback(rootDirs);
                        return rootDirs;
                    });
                },
                cdUp: function(parentId) {
                    if (parentId == undefined) {
                        this.dirStack.splice(0, 100);
                        this.cd(undefined, true);
                        return;
                    }
                    var index = -1;
                    var parent = undefined;
                    for (var i = 0; i < this.dirStack.length; i++) {
                        if (this.dirStack[i].id == parentId) {
                            index = i;
                            parent = this.dirStack[i];
                        }
                    }
//                    console.log(parent, index, this.dirStack);
                    if (parent == undefined ) {return;}
                    this.dirStack.splice(index + 1, 100);
                    this.cd(parent, true);
                },
                cd : function(file, up) {
                    // move into a directory
//                    console.log(JSON.stringify(file));
                    var id = undefined;
                    var displayName = "";
                    if (file != undefined) {
                        id = file.id;
                        displayName = file.displayName;
                    }
//                    console.log(id, displayName);
                    if ((up == undefined || up == false) && file != undefined) {
                        this.dirStack.push(file);
                    }
                    this.loadFiles(id, displayName);
                },
                validateAdd : function(file, replace) {
                    // validate a file that's been added
                    console.log(this.validFormats);
                        return TDAR.vuejs.upload.validateAdd(file, this.files, replace, this.validFormats, 0 , 100000 , false ,this )
                },
                updateFileProgress : function(e, data) {
                    // update the progress of uploading a file
                    var _app = this;
                    if (data.files != undefined) {
                        var active = TDAR.vuejs.upload._matching(data.files, _app.files, "name");
                        active.forEach(function(pair) {
                            var file = pair[0];
                            var fileContainer = pair[1];
                            fileContainer.progress = parseInt(data.loaded / data.total * 100, 10);
                            _app.$forceUpdate();
                        });
                    }
                },
                addError : function(error) {
                    this.errors.push(error);
                },
                append: function(a, b) {
                  return a + "" + b;  
                },
                fileUploadSubmit : function(e, data) {
                },
                fileUploadAdd : function(e, data) {
                    console.log('fileUploadAdd:', e, data);
                    this._disable();
                    var $upload = $('#fileupload');
                    return TDAR.vuejs.upload.fileUploadAdd($upload, data, this);
                },
                _enable: function() {
                    $(".submitButton, #fileAsyncUpload").prop("disabled", false);
                    $(".fileinput-button").removeClass("disabled");
                },
                _disable: function() {
                    $(".submitButton, #fileAsyncUpload").prop("disabled", true);
                    $(".fileinput-button").addClass("disabled");
                },
                fileUploadAddDone : function(e, data) {
                    // complete the add action
                    var _app = this;
                    this._enable();
                    return TDAR.vuejs.upload.fileUploadAddDone(data,_app.files, _app);
                },
                addFile: function(file) {
                    this.files.push(file);
                }
            },
            mounted : function() {
                // setup
                this.accountId = this.accounts[0].id;
                if (this.ableToUpload == undefined || this.ableToUpload == false) {
                    console.log('file upload disabled');
                    return;
                }
                this.loadFiles(undefined,"/");
                var _app = this;
                var up = $('#fileupload').fileupload({
                    url : "/api/file/upload",
                    dataType : 'json',
                    paramName : "uploadFile",
                    dropZone: "#filesTool",
                    // required to ensure that we don't send two files separately and not use the same ticket
                    singleFileUploads: false,
                    formData : function(form) {
                        // override formData
                        var data = [];
                        if (_app.ticketId == undefined || _app.ticketId == -1) {
                            data.push({
                                name : "ticketRequested",
                                value : true
                            });
                        } else {
                            data.push({
                                name : "ticketId",
                                value : _app.ticketId
                            });
                        }
                        if (_app.parentId != undefined) {
                            data.push({
                                name : "parentId",
                                value : _app.parentId
                            });
                        }
                        data.push({
                            name : "accountId",
                            value : _app.accountId
                        });
//                        console.log(data);
                        return data;
                    },
                    progressall : function(e, data) {
                        var progress = parseInt(data.loaded / data.total * 100, 10);
                        TDAR.vuejs.upload.setProgress(progress);
                    }
                }).prop('disabled', !$.support.fileInput).parent().addClass($.support.fileInput ? undefined : 'disabled');
                var _app = this;

                up.bind('fileuploadadd', _app.fileUploadAdd).bind('fileuploaddone', _app.fileUploadAddDone).bind('fileuploadsubmit', _app.fileUploadSubmit)
                        .bind('fileuploadprogress', _app.updateFileProgress);
            }
        });
        
        var router = new VueRouter({
            routes: [
                { path: '/:accountId(\\d+)/:dir*', component: app },
              ]
        });

        var pp = new Vue({
            router:router
          }).$mount(appId);

        _app = app;
        _pp = pp;
        return app;
    };
    
    return {
        init : _init,
        getApp: _getApp,
        getPp: _getPp,
        main : function() {
            var appId = "#filesTool";
            if ($(appId).length == 1) {
                TDAR.vuejs.balk.init(appId);
            }
        }
    }

})(console, jQuery, window, Vue);
