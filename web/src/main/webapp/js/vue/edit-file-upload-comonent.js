TDAR.vuejs.uploadWidget = (function(console, $, ctx, Vue, jqueryFileUpload, timePicker) {
    "use strict";
    var url = TDAR.uri('upload/upload');
    var ERROR_TIMEOUT = 5000;

    var __matching = function(arr1, arr2, name) {
        var ret = new Array();
        arr1.forEach(function(p1) {
            arr2.forEach(function(p2) {
                // console.log(p1[name], p2[name]);
                if (p1[name] == p2[name]) {
                    ret.push([ p1, p2 ]);
                }
            });
        });
        return ret;
    }

    var _setProgress = function(progress) {
        $('#progress .progress-bar').css('width', progress + '%');

    }

    var _init = function(widgetId) {

        Vue.component('fpart', {
            template: "#fpart-template",
            props: ["file","index"],
            data() {
                return {
                    previousDeleteState : '',
                    xhr:undefined,
                    previousReplaceState: ''
                }
            },
            computed: {
            },
            methods: {
                markModified:function() {
                    if (this.file.action == 'NONE' || this.file.action == undefined) {
                        Vue.set(this.file,"action","MODIFIED");
                    }
                },
                deleteFile() {
                    if (this.file.action == 'DELETE') {
                        return;
                    }
                    Vue.set(this,"previousDeleteState", this.file.action);
                    Vue.set(this.file,"action","DELETE");
                },
                unDeleteFile() {
                    if (this.file.action == 'DELETE') {
                        Vue.set(this.file,"action",this.previousDeleteState);
                    }
                },
                undoReplace(e) {
                    Vue.set(this.file,"action",this.previousReplaceState);
                    console.log($("#fileupload" + this.index));
                    this.xhr.abort();
                    Vue.set(this.file,"replaceFile",undefined);
                    _setProgress(0);
                    Vue.set(this.file,"progress", undefined);
                    // $("#fileupload" + this.index).reset();
                },
                replaceFileChange(e){
                    var files = e.target.files || e.dataTransfer.files;
                    if (!files.length){
                      return;
                    }
                    Vue.set(this,"previousReplaceState", this.file.action);
                    Vue.set(this.file,"action","REPLACE");
                    Vue.set(this.file,"replaceFile",files[0].name);
                    files[0].dontCreate = true;
                    var xhr = $('#fileupload').fileupload('send', {files: files});
                    Vue.set(this,"xhr", xhr);
                    
                }
            },
            watch: {
                "file.description" : function (val, old) {
                    this.markModified();
                },
                "file.dateCreated": function (val, old) {
                    this.markModified();
                },
                "file.restriction": function (val, old) {
                    this.markModified();
                }
            }
        });
////https://github.com/blueimp/jQuery-File-Upload/wiki/API

   var app = new Vue({
     el: widgetId,
     data: {
          files:[],
          ticketId: -1,
          resourceId: -1,
          userId: -1,
          validFormats: [],
          sideCarOnly: false,
          maxNumberOfFiles: 50,
         requiredOptionalPairs: [{required:[], optional:[]}]
     },
     mounted: function(){
         // do things
     },
     methods: {
         validatePackage: function() {
             // validates that once all of the files are added that the entire file package is valid
         },
         validateAdd: function(file) {
             // valdiate the file can be added to the resource/existing type
             var validExt = undefined;
             // for valid extensions check if we match
             this.validFormats.forEach(function(ext){
                 if (file.endsWith(ext)) {
                     validExt = ext
                 }
             });
             if (validExt == undefined) {
                 return false;
             }
             
             // check number of files
             if (this.files.length >= this.maxNumberOfFiles) {
                 return false;
             }
             
             
             // check if all files have to be connected (sidecar)
             if (this.sideCarOnly) {
                 var base = file.substring(file.length - validExt.length);
                 if (this.files.length > 0 ) {
                     var validSidecar = true;
                     this.files.forEach(function(f) {
                         // if we don't have the same basename, or the file is a dup
                         if (!f.startsWith(base) || f == file) {
                             validSidecar = false;
                         }
                     });
                     
                     if (!validSidecar) {
                         return false;
                     }
                 }
             }
             
         },
         updateFileProgress: function(e,data) {
             // update the progress of uploading a file
             var _app = this;
             if (data.files != undefined ) {
                 var active = __matching(data.files, _app.files, "name");
                 active.forEach(function(pair) {
                     var file = pair[0];
                     var fileContainer = pair[1];
                     fileContainer.progress =  parseInt(data.loaded / data.total * 100, 10);
                     _app.$forceUpdate();
                 });
             }
         },
         fileUploadAdd: function (e, data) {
             // add a file
           console.log('fileUploadAdd:',e, data);
           var validFiles = new Array();
           var _app = this;
           data.originalFiles.forEach(function(file){
               if (_app.validateAdd(file)) {
                   validFiles.push(file);
               }
           });
           // data.originalFiles = validFiles;
           console.log(validFiles);

           var jqXHR = $('#fileupload').fileupload('send', {files: validFiles, ticketId: this.ticketId});

           validFiles.forEach(function(file){
               var f = {
                   test: true,
                   name:file.name,
                   size:file.size,
                   type:file.type,
                   lastModified:file.lastModified,
                   status: 'queued',
                   xhr: jqXHR
               };
               if (valid && file.dontCreate == undefined || file.dontCreate == false) {
                   _app.files.push(f);
               }
           });
           if (validFiles.length > 0) {
               return true;
           }
           return false;
       },
       fileUploadAddDone : function (e, data) {
           // complete the add action
           var _app = this;
          var active = __matching(data.result.files, _app.files, "name");
          if (!data.result.ticket) {
              return;
          }
          if (!data.result.ticket.id) {
              console.log("no ticket in results");
          } else {
              var ticket = data.result.ticket;
              console.log("ticket received: %s", JSON.stringify(ticket));
              Vue.set(this,"ticketId", data.result.ticket.id);
          }

          active.forEach(function(pair) {
              var file = pair[0];
              var fileContainer = pair[1];
              if (file.error == undefined) {
                  fileContainer.status = data.textStatus;
              } else {
                  fileContainer.status = 'error';
              }
              fileContainer.xhr = undefined;
              fileContainer.error = file.error;
          });
          console.log(data.status, data.textStatus);
      }
     },
     mounted: function() {
         // setup
         var up =  $('#fileupload').fileupload({
              url: url,
              dataType: 'json',
              progressall: function (e, data) {
                  var progress = parseInt(data.loaded / data.total * 100, 10);
                  _setProgress(progress);
              }
          }).prop('disabled', !$.support.fileInput)
              .parent().addClass($.support.fileInput ? undefined : 'disabled');
              var _app = this;

      up.bind('fileuploadadd', _app.fileUploadAdd)
          .bind('fileuploaddone', _app.fileUploadAddDone)
          .bind('fileuploadprogress', _app.updateFileProgress);
          // .bind('fileuploadfail', function (e, data) {console.log('fileUploadFail:',e);})
          // .bind('fileuploadstart', function (e) {console.log('fileUploadStart:',e);})
          // .bind('fileuploadstop', function (e) {console.log('fileUploadStop:',e);})
          // .bind('fileuploadchange', function (e, data) {console.log('fileUploadChange:',e);});
          // .bind('fileuploadsubmit', function (e, data) {console.log('fileUploadSubmit:',e);})
          // .bind('fileuploadsend', function (e, data) {console.log('fileUploadSend:',e);})
          // .bind('fileuploadalways', function (e, data) {console.log('fileUploadAlways:',e);})
          // .bind('fileuploaddrop', function (e, data) {/* ... */})
          // .bind('fileuploadchunksend', function (e, data) {/* ... */})
          // .bind('fileuploadchunkdone', function (e, data) {/* ... */})
          // .bind('fileuploadchunkfail', function (e, data) {/* ... */})
          // .bind('fileuploadchunkalways', function (e, data) {/* ... */});
     },
   });
return app;
    }

    return {
        init : _init,
        collectionSelectizeOptions : _getSelectizeOpts,
        main : function() {
            var appId = '#uploadWidget';
            if ($(appId).length > 0) {
                TDAR.vuejs.uploadWidget.init(appId);
            }
        }
    }
})(console, jQuery, window, Vue, jqueryFileUpload, timePicker);
