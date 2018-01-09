TDAR.vuejs.uploadWidget = (function(console, $, ctx, Vue) {
    "use strict";
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
            data : {
                    previousDeleteState : '',
                    xhr:undefined,
                    previousReplaceState: ''
            },
            computed: {
                rowId: function() {
                    return "files-row-" + this.index;
                },
                restrictionFieldName: function() {
                    return "fileProxies["+this.index+"].restriction";
                },
                createdDateFieldName: function() {
                    return "fileProxies["+this.index+"].fileCreatedDate";
                },
                fileIdFieldName: function() {
                    return "fileProxies["+this.index+"].fileId";
                },
                filenameFieldName: function() {
                    return "fileProxies["+this.index+"].filename";
                },
                sequenceNumberFieldName: function() {
                    return "fileProxies["+this.index+"].sequenceNumber";
                },
                actionFieldName: function() {
                    return "fileProxies["+this.index+"].action";
                },
                descriptionFieldName: function() {
                    return "fileupload"+this.index+"";
                },
                wrapperId: function() {
                    return "fileupload"+this.index+"Wrapper";
                },
                fileuploadId: function() {
                    return "fileupload"+this.index;
                },
                
            },
            methods: {
                markModified:function() {
                    if (this.file.action == 'NONE' || this.file.action == undefined) {
                        Vue.set(this.file,"action","MODIFIED");
                    }
                },
                deleteFile: function() {
                    if (this.file.action == 'DELETE') {
                        return;
                    }
                    Vue.set(this,"previousDeleteState", this.file.action);
                    Vue.set(this.file,"action","DELETE");
                },
                unDeleteFile: function() {
                    if (this.file.action == 'DELETE') {
                        Vue.set(this.file,"action",this.previousDeleteState);
                    }
                },
                undoReplace: function(e) {
                    Vue.set(this.file,"action",this.previousReplaceState);
                    console.log($("#fileupload" + this.index));
                    this.xhr.abort();
                    Vue.set(this.file,"replaceFile",undefined);
                    _setProgress(0);
                    Vue.set(this.file,"progress", undefined);
                    // $("#fileupload" + this.index).reset();
                },
                replaceFileChange: function(e){
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
// //https://github.com/blueimp/jQuery-File-Upload/wiki/API

        var config = {
                files:[],
                url:TDAR.uri('upload/upload'),
                ticketId: -1,
                resourceId: -1,
                userId: -1,
                validFormats: [],
                sideCarOnly: false,
                maxNumberOfFiles: 50,
               requiredOptionalPairs: [{required:[], optional:[]}]
        };
        if ($(widgetId).data('config') != undefined) {
            console.log( $($(widgetId).data('config')).text() );
          $.extend(config,JSON.parse($($(widgetId).data('config')).text()));  
        }
        
   var app = new Vue({
     el: widgetId,
     data: {
          files:config.files,
          url:config.url,
          ticketId: config.ticketId,
          resourceId: config.resourceId,
          userId: config.userId,
          validFormats: config.validFormats,
          sideCarOnly: config.sideCarOnly,
          maxNumberOfFiles: config.maxNumberOfFiles,
          requiredOptionalPairs: config.requiredOptionalPairs
     },
     computed : {
         valid: function() {
             return this.validatePackage();
         }
     },
     methods: {
         validatePackage: function() {
             if (this.requiredOptionalPairs == undefined || this.requiredOptionalPairs.length == 0) {
                 return true;
             }
             var _app = this;
             console.log("computing overall validity");
             var exts = new Array(); 
             var pairs = new Array();
             // convert to a set
             this.files.forEach(function(file){
                 var ext = file.name.substring(file.name.indexOf("."));
                 exts.push(ext);
                 var seen = false;
                 _app.requiredOptionalPairs.forEach(function(pair){
                     if (!seen && ($.inArray(ext, pair.required) > -1 || $.inArray(ext, pair.optional) > -1 ))  {
                         pairs.push(pair);
                         seen = true;
                     }
                 });
             });
             
             if (pairs.length < 1) {
                 console.error("we have requirements, but these files are too confusing");
                 return false;
             }
             
             
             console.log("exts:", exts);
             console.log("pairs:", pairs);
             var valid = false;
             pairs.forEach(function(pair){
                 if (!valid) {
                     var required = pair.required.slice(0);
                     var optional = pair.optional.slice(0);
                     // remove required
                     exts.forEach(function(ext){
                         console.log($.inArray(ext, required),required,ext);
                         if ($.inArray(ext, required) > -1) {
                             required.splice($.inArray(ext, required), 1 );
                         }
                         if ($.inArray(ext, optional ) > -1) {
                             optional.splice($.inArray(ext, optional),1);
                         }
                     });
                     console.log("required:", required);
                     console.log("optional:", optional);
                     if (required.length > 0) {
                         console.error("required format(s) remain", required);
                     } else {
                         valid = true;
                     }
                 }
             });
             return valid;
         },
         validateAdd: function(file) {
             console.log("validating file:", file);
             // valdiate the file can be added to the resource/existing type
             var validExt = undefined;
             // for valid extensions check if we match
             var fileName = file.name;
             this.validFormats.forEach(function(ext){
                 console.log("eval:",fileName,ext);
                 if (fileName.indexOf(ext, fileName.length - ext.length) !== -1) {
                     validExt = ext
                 }
             });
             if (validExt == undefined) {
                 console.error("extension is not valid:", file, this.validFormats);
                 return false;
             }
             
             // check number of files
             // FIXME: add check/subtraction for deleted files
             if (this.files.length >= this.maxNumberOfFiles) {
                 console.error("too many files", this.maximumNumberOfFiles);
                 return false;
             }
             
             // check if all files have to be connected (sidecar)
             if (this.sideCarOnly) {
                 var base = fileName.substring(0, fileName.length - validExt.length);
                 console.log("files:",this.files);
                 if (this.files.length > 0 ) {
                     var validSidecar = true;
                     this.files.forEach(function(f) {
                         console.log("base:", base, f.name, fileName);
                         // if we don't have the same basename, or the file is a dup
                         if (f.name.indexOf(base) != 0 || f.name == fileName) {
                             validSidecar = false;
                         }
                     });
                     if (!validSidecar) {
                         console.error("file is not valid sidecar", base);
                         return false;
                     }
                 }
             }
             
             return true;
             
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
               if (file.dontCreate == undefined || file.dontCreate == false) {
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
              url: this.url,
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
     }});
   return app;
    }

    return {
        init : _init,
        main : function() {
            var appId = '#uploadWidget';
            if ($(appId).length > 0) {
                TDAR.vuejs.uploadWidget.init(appId);
            }
        }
    }
})(console, jQuery, window, Vue);
