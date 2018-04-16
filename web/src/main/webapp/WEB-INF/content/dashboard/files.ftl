<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "common-dashboard.ftl" as dash />
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "/${config.themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>

</head>


<div id="titlebar" parse="true">
    <h1>Dashboard &raquo; <span class="red">My Scratch Space</span></h1>

</div>
<div class="row">
<div class="span2">
    <@dash.sidebar current="files" />
</div>
<div class="span10" id="filesTool">

<form class="form-horizontal">
<div class="span4">
<div class="control-group">
  <label class="control-label" for="inputInfo">Select Billing Account</label>
  <div class="controls">
    <select name="account" id="account">
        <option>Veterans Curation Program</option>
    </select>
  </div>
</div>
</div>
<div class="span4">
<input type="search" class="search input" placeholder="search"/>
</div>

<table class="table">
<thead>
 <tr>
    <th>file</th>
    <th>status</th>
    <th>action</th>
 </tr>
</thead>
<tr>
 <td colspan="3"><p style='text-align:center;font-weight:bold'>Note: files in this space expire after 1 month.</td>
</tr>
        <tbody v-for="(file,index) in files">
            <tr v-if="index == 0 && parentId != undefined">
            <td></td>
            <td> <span class="link" @click="cd()">.. </span>  </td>
            </tr>
            <tr class="template-download fade existing-file in"
                is="fileEntry" :index="index" :file="file" :editable="ableToUpload"></tr>
        </tbody>
        <tbody v-if="files.length == 0 && parentId != undefined">
        <tr>
            <td></td>
            <td> <span class="link" @click="cd()">.. </span>  </td>
            </tr>
        </tbody>
</table>

    <label class="btn btn-success fileinput-button" :disabled="inputDisabled" for="fileupload" id="fileuploadWrapper"> <i
        class="glyphicon glyphicon-plus"></i> <span>Upload 
            files...</span> <!-- The file input field used as target for the file upload widget -->
        <input id="fileupload" type="file" name="files[]" multiple :disabled="inputDisabled" >
    </label>
    <div class="input-append">
    <input class="span2" id="dirName" name="dirName" type="text">
    <button class="btn" type="button" id="mkdir" @click="mkdir">Add Folder</button>
    </div>

     <br> <br>
    <!-- The global progress bar -->
    <div id="progress" class="progress">
        <div class="progress-bar progress-bar-success"></div>
    </div>
    <div id="uploadstatus"></div>


    </div>
</div>

</div>


</div>

<template id="file-entry-template">
<tr v-bind:id="rowId">
    <td>{{ 1 + index}}</td>
    <td> <span v-if="file.size == undefined " class="link" @click="cd(file)">{{file.filename}} </span> <span v-if="file.size != undefined ">{{file.filename }}  </span> </td>
    </tr>
</template>

<script>
    $(document).ready(function () {
        TDAR.notifications.init();
        
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
                }
            },
            computed: {
                rowId : function() {
                    return "files-row-" + this.index;
                }
            }
        });
        
        var app = new Vue({
            el : "#filesTool",
            data : {
                listUrl : "/api/file/listFiles",
                url : "/api/file/upload",
                validFormats : ['doc','pdf'],
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
                    $.get(this.listUrl, {"parentId": parentId}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        console.log(msg);
                        Vue.set(_app,"files", msg);
                    });
                    Vue.set(this, "parentId", parentId);
                    Vue.set(this, "path", path);
                },
                mkdir: function() {
                  var dir = this.dir;
                  var _app = this;
                  $.post("/api/file/mkdir", {"parentId": _app.parentId, "name": $("#dirName").val() }
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
                
                    return TDAR.vuejs.upload.validateAdd(file, this.files, replace, ['doc','docx','pdf'], 0	, 100000 , false  )
                },
                updateFileProgress : function(e, data) {
                    // update the progress of uploading a file
                    var _app = this;
                    if (data.files != undefined) {
                        var active = TDAR.vuejs.upload._matching(data.files, _app.files, "filename");
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
    });
    
    
    
    
</script>



</#escape>