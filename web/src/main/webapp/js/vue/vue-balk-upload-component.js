TDAR.vuejs.balk = (function(console, $, ctx, Vue) {
    "use strict";

    var _init = function(appId) {
        Vue.component('fileEntry', {
            template : "#file-entry-template",
            props : [ "file", "index", "editable" , "studentreviewed", "externalreviewed", "fullservice"],
            data : function() {
                return {
                    previousDeleteState : '',
                    xhr : undefined,
                    previousReplaceState : ''
                }
            },
            methods: {
                cd : function(file) {
                    this.$parent.cd(file);
                },
                _mark: function(role) {
                    var id = this.file.id;
                    var _file= this.file;
                    var ret = {};
                    $.post("/api/file/mark", {"ids[0]": id,"role":role}).done(function(files){
                        ret = files[0];
                    });
                    return ret;
                },
                markStudentCurated: function() {
                    var ret = this._mark("STUDENT_CURATED");
                    Vue.set(file, "dateStudentReviewed", ret.dateReviewed);
                    Vue.set(file, "studentReviewedByName", ret.studentReviewedByName);
                    Vue.set(file, "studentReviewedByInitials", ret.studentReviewedByInitials);
                },
                markReviewed: function() {
                    var ret = this._mark("REVIEWED");
                    Vue.set(file, "dateReviewed", ret.dateReviewed);
                    Vue.set(file, "reviewedByName", ret.reviewedByName);
                    Vue.set(file, "reviewedByInitials", ret.reviewedByInitials);
                },
                markCurated: function() {
                    var ret = this._mark("CURATED");
                    Vue.set(file, "dateCurated", ret.dateCurated);
                    Vue.set(file, "curatedByName", ret.curatedByName);
                    Vue.set(file, "curatedByInitials", ret.curatedByInitials);
                },
                wontCurate: function() {
                    
                },
                markExternalReviewed: function() {
                    var ret = this._mark("EXTERNAL_REVIEWED");
                    Vue.set(file, "dateExternalReviewed", ret.dateExternalReviewed);
                    Vue.set(file, "externalReviewedByName", ret.rxternalReviewedByName);
                    Vue.set(file, "externalReviewedByInitials", ret.externalReviewedByInitials);
                },
                formatDate: function(date) {
                if (date == undefined ) {
                return "";
                }
                  return new Date(date).toLocaleString(['en-US'], {month: '2-digit', day: '2-digit', year: 'numeric'}); // new
                                                                                                                        // Date.parse(date).format('MM/DD/YYYY
                                                                                                                        // hh:mm');
                },
                moveUI : function() {
                },
                deleteFile: function(){
                    this.$parent.deleteFile(file);
                }
            },
            computed: {
                fileLink : function() {
                    return "/document/add?fileIds=" + this.file.id; 
                },
                downloadUrl : function() {
                    return "/file/download/" + this.file.id; 
                },
                rowId : function() {
                    return "files-row-" + this.index;
                },
                canStudentReview: function () {
                    if (this.dateStudentReviewed != undefined) {
                        return false;
                    }

                    if (this.studentReviewed && this.dateCurated != undefined) {
                        return true;
                    }
                    return false;
                },
                canCurate: function () {
                    if (this.dateCurated != undefined) {
                        return false;
                    }
                    if (this.file.resourceId != undefined) {
                        return true;
                    }
                    return false;
                },
                canReview: function () {
                    if (this.dateReviewed != undefined) {
                        return false;
                    }
                    if (this.studentReviewed && this.file.dateStudentReviewed != undefined || 
                            this.studentReviewed == false && this.dateCurated != undefined) {
                        return true;
                    }
                    return false;
                },
                canExternalReview: function () {
                    if (this.dateExternalReviewed != undefined) {
                        return false;
                    }
                    if (this.externalReviewed && this.file.dateReviewed != undefined) {
                        return true;
                    }
                    return false;
                },
                
            }
        });

        var config = {
                fullService : true,
                studentReviewed : false,
                externalReviewed : true
        };
        var app = new Vue({
            el : appId,
            data : {
                listUrl : "/api/file/listFiles",
                url : "/api/file/upload",
                validFormats : ['doc','pdf','docx','png','tif','tiff','jpg','jpeg'],
                ableToUpload : true,
                parentId: undefined,
                studentReviewed: config.studentReviewed,
                fullService: config.fullService,
                externalReviewed: config.externalReviewed,
                files : [],
                errors: [],
                path : "" 
            },
            computed : {
                inputDisabled : function() {
                    return !this.ableToUpload;
                }
            },
            methods : {
                loadFiles: function (parentId, path) {
                    var _app = this;
                    var accountId = undefined;
                    var $acc = $("#accountId");
                    if ($acc.length > 0) {
                       accountId = $acc.val();
                    }
                    
                    $.get(this.listUrl, {"parentId": parentId, "accountId":accountId}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        Vue.set(_app,"files", msg);
                    });
                    Vue.set(this, "parentId", parentId);
                    Vue.set(this, "path", path);
                },
                deleteFile: function(file) {
                    var id = file.id;
                    var _file= file;
                    $.post("/api/file/deleteFile", {"id": id}).done(function(files){
                        console.log(file);
                        var index = $.inArray(file, this.files);
                        if (index != -1) {
                            this.files.splice(index, 1);
                        }
                    });

                },
                mkdir: function() {
                  var dir = this.dir;
                  var _app = this;
                  $.post("/api/file/mkdir", {"parentId": _app.parentId, "name": $("#dirName").val() , accountId: $("#accountId").val()}
                    ).done(function(msg) {
                        _app.files.push(msg);
                    });
                },
                cd : function(file) {
                    console.log(JSON.stringify(file));
                    var id = undefined;
                    var displayName = "";
                    if (file != undefined) {
                        id = file.id;
                        displayName = file.displayName;
                    }
                    console.log(id, displayName);
                    this.loadFiles(id, displayName);
                },
                validateAdd : function(file, replace) {
                
                    return TDAR.vuejs.upload.validateAdd(file, this.files, replace, ['doc','docx','pdf'], 0 , 100000 , false ,this )
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
                if (this.ableToUpload == undefined || this.ableToUpload == false) {
                    console.log('file upload disabled');
                    return;
                }
                this.loadFiles(undefined,"/");
                                var _app = this;
                var up = $('#fileupload').fileupload({
                    url : this.url,
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
                            value : $("#accountId").val()
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
    };
    
    return {
        init : _init,
        main : function() {
            var appId = "#filesTool";
            if ($(appId).length == 1) {
                TDAR.vuejs.balk.init(appId);
            }
        }
    }

})(console, jQuery, window, Vue);
