TDAR.vuejs.upload = (function(console, $, ctx, Vue) {
    "use strict";

    var __normalize = function(filename) {
        var basename = filename.replace(/[^\w\-\.\+\_]/g, "-");
        basename = basename.replace( "-.", ".");
        return basename;
    }
    
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
        if (progress == 100) {
            $("#uploadstatus").html("Complete");
        } else if (progress < 1) {
            $("#uploadstatus").html("");
        } else {
            $("#uploadstatus").html("Uploading...");
        }

    }

    var _fileUploadAddDone = function(data, files, _app) {
        var active = __matching(data.result.files, files, "name");
        if (!data.result.ticket) {
            return;
        }
        if (!data.result.ticket.id) {
            console.log("no ticket in results");
        } else {
            var ticket = data.result.ticket;
            console.log("ticket received:", JSON.stringify(ticket));
            Vue.set(_app, "ticketId", data.result.ticket.id);
            $("#ticketId").val(data.result.ticket.id);
        }

        active.forEach(function(pair) {
            var file = pair[0];
            var fileContainer = pair[1];
            if (file.error == undefined) {
                fileContainer.status = data.textStatus;
            } else {
                fileContainer.status = 'error';
            }
            console.log("file<-->container",file,fileContainer);
            Vue.set(fileContainer, "dateCreated", file.dateCreated);
            Vue.set(fileContainer, "id", file.id );
            Vue.set(fileContainer , "createdByName" , file.createdByName);
            Vue.set(fileContainer, "uploaderInitials", file.uploaderInitials);
            Vue.set(fileContainer, "uploaderName", file.uploaderName);
            Vue.set(fileContainer, "xhr" , undefined);
            Vue.set(fileContainer, "error" ,file.error);
            Vue.set(fileContainer, "extension" , file.extension);
        });
        console.log("status-->",data.status, data.textStatus);

        
    }
    
    var _validatePackage = function(files, requiredOptionalPairs, messageListener) {
        if (requiredOptionalPairs == undefined || requiredOptionalPairs.length == 0 || files == undefined || files.length ==0) {
            return true;
        }
        console.log("computing overall validity");
        var exts = new Array();
        var pairs = new Array();
        // convert to a set
        files.forEach(function(file) {
            if (file.action != 'DELETE') {
                var ext = file.name.substring(file.name.indexOf(".") ).toLowerCase();
                exts.push(ext);
                var seen = false;
                requiredOptionalPairs.forEach(function(pair) {
                    console.log(seen, ext, pair.required, pair.optional);
                    if (!seen && ($.inArray(ext, pair.required) > -1 || $.inArray(ext, pair.optional) > -1)) {
                        pairs.push(pair);
                        seen = true;
                    }
                });
            }
        });
        messageListener.clearPackageMessages();

        if (pairs.length < 1) {
            console.error("we have requirements, but these files are too confusing");
            messageListener.addPackageMessage("we have requirements, but these files are too confusing");
            return false;
        }

        console.log("exts:", exts);
        console.log("pairs:", pairs);
        var valid = false;
        pairs.forEach(function(pair) {
            if (!valid) {
                var required = pair.required.slice(0);
                var optional = pair.optional.slice(0);
                // remove required
                exts.forEach(function(ext) {
                    console.log($.inArray(ext, required), required, ext);
                    // if we're in "required" then remove it
                    if ($.inArray(ext, required) > -1) {
                        required.splice($.inArray(ext, required), 1);
                    }
                    
                    // special cases for JPEG and TIFF
                    if (ext == 'tiff' && $.inArray('tif', required) > -1) {
                        required.splice($.inArray('tif', required), 1);
                    }
                    if (ext == 'jpeg' && $.inArray('jpeg', required) > -1) {
                        required.splice($.inArray('jpeg', required), 1);
                    }

                    // if we're in "optional" then remove it
                    if ($.inArray(ext, optional) > -1) {
                        optional.splice($.inArray(ext, optional), 1);
                    }
                });
                console.log("required:", required);
                console.log("optional:", optional);
                if (required.length > 0) {
                    console.error("required format(s) remain", required);
                    messageListener.addPackageMessage("you must also upload files with the following extensions: " + required)
                } else {
                    valid = true;
                }
            }
        });
        return valid;
    }

    var _validateAdd = function(file, files, replace, validFormats, currentNumberOfFiles, maxNumberOfFiles, sideCarOnly, errorListener) {
        console.log("validating file:", file);
        // valdiate the file can be added to the resource/existing type
        var validExt = undefined;
        // for valid extensions check if we match
        if (file.name == undefined && file.filename != undefined) {
            file.name = file.filename; 
        }
        // make sure that we're ok when data is coming from Vue via the file object vs. the FileProxy
        var fileName = file.name;

        validFormats.forEach(function(ext) {
            if (fileName.toLowerCase().indexOf(ext, fileName.length- ext.length) !== -1) {
                validExt = ext;
            }
        });
        if (validExt == undefined) {
            console.error("extension is not valid:", file, validFormats);
            errorListener.addError("file extension is not valid for file " + file.name);
            return false;
        }

        
        var normalName = __normalize(fileName);
        console.log(normalName);
        var dupFound = false;
        files.forEach(function(existing) {
            console.log(existing);
           if (normalName == existing.name && existing.action != 'DELETE') {
               console.log(replace, normalName);
               if (replace != undefined && replace == normalName) {
                   // ignore
               } else {
                   console.log("duplicateFilename");
                   dupFound = true;
               }
           } 
        });
        if (dupFound) {
            errorListener.addError("file is a duplicate of " + file.name);
            return false;
        }
        // check number of files

        if (replace == undefined && currentNumberOfFiles >= maxNumberOfFiles) {
            console.error("too many files", maxNumberOfFiles);
            errorListener.addError("you have exceeded the number of files you can add");
            return false;
        }

        // check if all files have to be connected (sidecar)
        if (sideCarOnly) {
            var base = fileName.substring(0, fileName.length - validExt.length);
            console.log("files:", files);
            if (files.length > 0) {
                var validSidecar = true;
                files.forEach(function(f) {
                    console.log("base:", base, f.name, fileName);
                    if (f.action != 'DELETE') {
                        // if we don't have the same basename, or the file is a dup
                        if (f.name.indexOf(base) != 0 || f.name == fileName) {
                            validSidecar = false;
                        }
                    }
                });
                if (!validSidecar) {
                    console.error("file is not valid sidecar", base);
                    errorListener.addError("this file cannot be added to this record: " + file.name);
                    return false;
                }
            }
        }
        return true;
    }
    
    var _fileUploadAdd = function($upload, data, _app, unfiled ) {
        var validFiles = new Array();
        data.originalFiles.forEach(function(file) {
            if (_app.validateAdd(file)) {
                validFiles.push(file);
            } else {
                console.log("INVALID FILE: ", JSON.stringify(file));
            }
        });
        // data.originalFiles = validFiles;
        console.log("VALID FILES: ", JSON.stringify(validFiles));

        var extra = {
            uploadFile : validFiles
        };
        
        var jqXHR = $upload.fileupload('send', extra);

        validFiles.forEach(function(file) {
            var name = file.name;

            var f = {
                test : true,
                name : name,
                size : file.size,
                type : file.type,
                lastModified : file.lastModified,
                status : 'queued',
                action : 'ADD',
                restriction : 'PUBLIC',
                xhr : jqXHR
            };
            if (file.dontCreate == undefined || file.dontCreate == false) {
                _app.addFile(f);
            }
        });
        if (validFiles.length > 0) {
            return true;
        }
        _app._enable();
        return false;
    }

    return {
        _matching : __matching,
        setProgress : _setProgress,
        validatePackage : _validatePackage,
        fileUploadAddDone: _fileUploadAddDone,
        fileUploadAdd : _fileUploadAdd,
        validateAdd : _validateAdd
    }

})(console, jQuery, window, Vue);
