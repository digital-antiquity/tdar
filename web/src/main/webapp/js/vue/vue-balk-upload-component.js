TDAR.vuejs.balk = (function(console, $, ctx, Vue) {
    "use strict";

    var _init = function(appId) {
        Vue.component('fileEntry', {
            template : "#file-entry-template",
            props : [ "file", "index", "editable" ],
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
                markCurated: function() {
                    var id = this.file.id;
                    var _file= this.file;
                    $.post("/api/file/markCurated", {"ids[0]": id}).done(function(files){
                        var file= files[0];
                        _file.dateCurated = file.dateCurated;
                        _file.curatedByName= file.curatedByName;
                    });

                },
                markReviewed: function() {
                    var id = this.file.id;
                    var _file= this.file;

                    $.post("/api/file/markReviewed", {"ids[0]": id}).done(function(files){
                    var file= files[0];
                    console.log(file);
                        _file.dateReviewed = file.dateReviewed;
                        _file.reviewedByName= file.reviewedByName;
                    });

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
                rowId : function() {
                    return "files-row-" + this.index;
                }
            }
        });

        var app = new Vue({
            el : appId,
            data : {
                listUrl : "/api/file/listFiles",
                url : "/api/file/upload",
                validFormats : ['doc','pdf','docx','png','tif','tiff','jpg','jpeg'],
                ableToUpload : true,
                parentId: undefined,
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
                        console.log(msg);
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
                        console.log(data);
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
