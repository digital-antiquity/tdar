TDAR.vuejs.upload = (function(console, $, ctx, Vue) {
    "use strict";

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
        var active = __matching(data.result.files, files, "filename");
        if (!data.result.ticket) {
            return;
        }
        if (!data.result.ticket.id) {
            console.log("no ticket in results");
        } else {
            var ticket = data.result.ticket;
            console.log("ticket received: %s", JSON.stringify(ticket));
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
            fileContainer.xhr = undefined;
            fileContainer.error = file.error;
        });
        console.log(data.status, data.textStatus);

        
    }
    
    var _validatePackage = function(files, requiredOptionalPairs) {
        if (requiredOptionalPairs == undefined || requiredOptionalPairs.length == 0) {
            return true;
        }
        console.log("computing overall validity");
        var exts = new Array();
        var pairs = new Array();
        // convert to a set
        files.forEach(function(file) {
            var ext = file.filename.substring(file.filename.indexOf(".")).toLowerCase();
            exts.push(ext);
            var seen = false;
            requiredOptionalPairs.forEach(function(pair) {
                if (!seen && ($.inArray(ext, pair.required) > -1 || $.inArray(ext, pair.optional) > -1)) {
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
        pairs.forEach(function(pair) {
            if (!valid) {
                var required = pair.required.slice(0);
                var optional = pair.optional.slice(0);
                // remove required
                exts.forEach(function(ext) {
                    console.log($.inArray(ext, required), required, ext);
                    if ($.inArray(ext, required) > -1) {
                        required.splice($.inArray(ext, required), 1);
                    }
                    if ($.inArray(ext, optional) > -1) {
                        optional.splice($.inArray(ext, optional), 1);
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
    }

    var _validateAdd = function(file, files, replace, validFormats, currentNumberOfFiles, maxNumberOfFiles, sideCarOnly) {
        console.log("validating file:", file);
        // valdiate the file can be added to the resource/existing type
        var validExt = undefined;
        // for valid extensions check if we match

        // make sure that we're ok when data is coming from Vue via the file object vs. the FileProxy
        if (file.name == undefined && file.filename != undefined) {
            file.name = file.filename;
        } else {
            file.filename = file.name;
        }
        var fileName = file.name;
        validFormats.forEach(function(ext) {
            if (fileName.toLowerCase().indexOf(ext, fileName.length - ext.length) !== -1) {
                validExt = ext;
            }
        });
        if (validExt == undefined) {
            console.error("extension is not valid:", file, validFormats);
            return false;
        }

        
        files.forEach(function(existing) {
            console.log(existing);
           if (file.name == existing.name) {
               console.log("duplicateFilename");
               return false;
           } 
        });
        
        // check number of files

        if ((replace == false || replace == undefined) && currentNumberOfFiles >= maxNumberOfFiles) {
            console.error("too many files", maxNumberOfFiles);
            return false;
        }

        // check if all files have to be connected (sidecar)
        if (sideCarOnly) {
            var base = fileName.substring(0, fileName.length - validExt.length);
            console.log("files:", files);
            if (files.length > 0) {
                var validSidecar = true;
                files.forEach(function(f) {
                    console.log("base:", base, f.filename, fileName);
                    // if we don't have the same basename, or the file is a dup
                    if (f.filename.indexOf(base) != 0 || f.filename == fileName) {
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
    }
    
    var _fileUploadAdd = function($upload, data, _app ) {
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
            if (name == undefined) {
                name = file.filename;
            }
            var f = {
                test : true,
                filename : name,
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
