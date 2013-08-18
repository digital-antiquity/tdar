/**
 * $Id$
 *
 * jQuery Validator Plugin extension for jQuery File Upload Plugin
 */
var FileuploadValidator;
(function(console) {
    "use strict";

    //creates when-callback that returns true when file list has at least file with provided extension (in varargs)
    var _hasFileWithExtension = function() {
        //copy current scope's arguments
        var varargs = Array.prototype.slice.call(arguments, 0);
        return function(files) {
            return $.grep(files, function(file) {
                return $.inArray(file.ext, varargs) > -1;
            }).length > 0;
        }
    };

    var _updateHighlighting = function(validator, files) {
        $.each(files, function(idx, file) {
            validator.unhighlight(file);
        });

        $.each(validator.errors, function(idx, error){
           if(error.file) {
               validator.highlight(error.file);
           }
        });
    };

    var _defaults = {
        errorContainer: "#fileuploadErrors",
        errorWrapper: "<li class='fileupload-error'></li>",
        errorClass: "fileupload-error",
        okayClass:  "fileupload-okay",

        //execute validate() whenever the user updates the fileupload list
        validateOnChange: true,
        registerJqueryValidateMethod: true,

        methods: {
            "nodupes": function(file, files) {
                var dupes = $.grep(files, function(_file){
                    return file.filename === _file.filename;
                });
                return dupes.length < 2;
            },

            "nodupes-ext": function(file, files) {
                var dupes = $.grep(files, function(_file) {
                    return file.ext === _file.ext;
                });
                return dupes.length < 2;
            },

            "required": function(files, settings) {
                var _files = files;
                if(settings.extension) {
                    var exts = (typeof settings.extension === "string") ? [settings.extension] : settings.extension;
                    _files = $.grep(files, function(file){
                        return $.inArray(file.ext, exts) > -1;
                    });
                }
                return _files.length > 0;
            },

            //Fileupload component already has maxFileUpload feature.  Use this method only if you want to limit
            //consideration to a certain set of file extensions.
            "filecount": function(file, files, settings) {
                var opts = $.extend({min:0, max:100, extension:[]}, settings);
                var filecount = $.grep(files, function(file){
                    return $.inArray(file.ext, opts.extension) > -1;
                }).length;
                return filecount >= opts.min && filecount <= opts.max;
            }

        },

        groupMethods: ["required"],

        rules: [],

        messages: {
            "nodupes": $.validator.format("Files with duplicated filenames are not allowed."),
            "required": $.validator.format("A file attachment is required."),
            "nodupes-ext": $.validator.format("You may only attach one file with this extension"),
            "filecount": $.validator.format("Filecount exceeded")
        }
    };

    FileuploadValidator = Class.extend({
        //errors resulting from the last validate()
        errors: null,

        //suggestions are a subset of errors that, when present, will show error  messages but not cause uploud to be 'invalid'
        suggestions: null,

        //similar to $.validator methods. defines for given context whether a file is valid, e.g. 'duplicate-name'
        methods: null,

        //group methods are applied once for all files 
        groupMethods: null,

        //rules designate which methods are applied to the fileupload container.
        rules: null,

        //default error messages, keyed by method name
        messages: null,

        //fileupload helper class we created in tdar.upload.js
        helper: null,

        //element representing the 'container' of the fileupload widget (usually top-level form)
        fileupload: null,

        init: function(formId, settings) {
            var self = this;
            console.log("init");
            var errs = [];
            this.fileupload = $("#" + formId)[0];

            //note the deep copy of defaults is necessary
            $.extend(true, this, _defaults);
            $.extend(this, settings);

            this.helper = $(this.fileupload).data("fileuploadHelper");
            if(!this.fileupload) errs.push("fileupload element not found");
            if(!this.helper) errs.push("fileupload helper not found - did you call registerFileUpload yet?");
            this.inputSelector = this.helper.inputSelector;
            $.each(errs, function(idx, err){
                console.error(err);
            });

            if(this.validateOnChange) {
                //validate on the fileupload custom events
                $(this.fileupload).bind("fileuploadcompleted fileuploaddestroyed", function() {
                    self.validate();
                });

                //revalidate when the user deletes or "undeletes" a file
                $(this.fileupload).on("click", "button.delete-button", function() {
                    self.validate();
                });
            }

            if(this.registerJqueryValidateMethod) {
                this.registerValidiatorMethod();
            }
        },

        validate: function() {
            console.log("validating %s   rulecount:%s", this.fileupload, this.rules.length);
            var self = this;
            this.suggestions = [];
            this.errors = [];

            var files = this.helper.validFiles();

            for(var i = 0; i < this.rules.length; i++) {
                var rule = this.rules[i];

                //optionally only apply a rule if settings has a when-callback
                var when = rule.settings.when || function() {return true;}
                if(when(files)) {

                    var method = rule.method;
                    var message = rule.message;

                    //if this is a group method,  execute just once
                    if($.inArray(rule.methodName, self.groupMethods) > -1) {
                        var valid = method(files, rule.settings);
                        console.log("applying rule, method:%s   valid:%s", rule.methodName, valid);
                        if(!valid) {
                            var error = {
                                "file": null,
                                "message": message()
                            };
                            console.dir(error);
                            self.errors.push(error);
                            if(rule.suggestion) {
                                self.suggestions.push(error);
                            }
                        }

                    //otherwise execute the method once-per-file
                    } else {
                        $.each(files, function(idx, file) {
                            var valid = method(file, files, rule.settings);
                            console.log("validate  rule:%s   method:%s   valid:%s", rule, typeof method, valid);
                            if(!valid) {
                                var error = {
                                    "file": file,
                                    "message": message(file.filename, file.base, file.ext, idx)
                                };
                                console.dir(error);
                                self.errors.push(error);
                                if(rule.suggestion) {
                                    self.suggestions.push(error);
                                }
                            } else {
                            }
                        });


                    }
                }

            }

            this.clearErrorDisplay();
            if(this.errors.length) {
                this.showErrors();
            }
            _updateHighlighting(self, files);

            //if we have no errors, or the only errors are mere suggestions,  then the uploads are valid
            return this.errors.length === 0 || this.errors.length === this.suggestions.length;
        },

        clearErrorDisplay: function() {
            $(this.errorContainer)
                .find("ul").empty()
                .end().hide();
        },

        showErrors: function() {
            var self = this;
            var $container = $(this.errorContainer);
            var $ul = $container.find("ul");
            $.each(this.errors, function(idx, error) {
                var $error = $(self.errorWrapper);
                if(error.file) {
                    $error.append("<b>" + error.file.filename + ": </b>");
                }
                $error.append("<span>" + error.message + "</span>");
                $ul.append($error);
            });
            $container.show();
        },

        addMethod: function(name, method, message)  {
            console.log("addMethod name:%s   method:%s    message:%s", name, typeof method, message);
            this.methods[name] = method;
            if(message) {
                this.messages[name] = $.validator.format(message);
            } else {
                this.messages[name] = $.validator.format("This file is invalid");
            }
        },

        addGroupMethod: function(name, method, message) {
            this.groupMethods.push(name);
            this.addMethod(name, method, message);
        },

        addRule: function(methodName, settings, customMessage){
            console.log("add rule: %s", methodName);
            var message = this.messages[methodName];
            if(customMessage) {
                message = $.validator.format(customMessage);
            }
            var rule  = {
                "methodName": methodName,
                "method": this.methods[methodName],
                "settings": settings || {},
                "message": message,
                "suggestion": false
            };
            this.rules.push(rule);
            return rule;
        },

        addSuggestion: function(methodName, settings, customMessage) {
            var rule = this.addRule( methodName, settings, customMessage);
            rule.suggestion = true;
        },

        highlight: function(file) {
            console.log("highlighting: %s", file.filename);
            file.context.removeClass(this.okayClass).addClass(this.errorClass);
        },

        unhighlight: function(file) {
            console.log("unhighlighting: %s", file.filename);
            file.context.removeClass(this.errorClass).addClass(this.okayClass);
        },

        /**
         * Register this fileupload validator as a jQuery Validation Plugin (aka "$.validator") validation method.
         * 
         * This method allows the fileupload validator to hook into the $.validator validation process.  It does this
         * by creating a  new $.validator method (named "valid-fileupload"), and then adding a new $.validator rule
         * that binds this method  to the file input form element used by the fileupload widget.
         */
        registerValidiatorMethod: function () {
            var self = this,
                methodName = "valid-fileupload";
            $.validator.addMethod(
                methodName,
                function() {
                    return self.validate();
                },
                "There was a problem with your uploaded files.  See details in the file upload section"
            );

            //we've added the method, now we need the specific rule that binds the fileinput element to the method
            $(self.inputSelector).addClass(methodName)
        }
    });



    
    TDAR.fileupload.addDataTableValidation = function(validator) {

        //only one image metadata file
        validator.addRule("filecount",
            {max: 1, extension:["accdb", "mdb", "xls", "xlsx","gdb"]},
            "You may only upload one spreadsheet or access database (MDB, ACCDB, XLS, GDB, XLSX)");

    };


    //FIXME: move me to a component that only gets added to GIS
    TDAR.fileupload.addGisValidation = function(validator) {
        var fileinfo = {
            shapefile: ["shp", "shx", "dbf", "sbn", "sbx", "fbn", "fbx", "ain", "aih", "atx", "ixs", "mxs", "prj", "xml", "cpg"],
            jpeg:["jpg", "jpeg", "jpw"],
            tiff:["tif", "tiff", "tfw"],
            image: ["jpg", "jpeg", "jpw", "tfw", "aux", "aux.xml"]
        };

        var requiredFiles = {
            shapefile: ["shp", "shx", "dbf"]
        }

        //require image files if image metadata file is present
        validator.addRule("required", {
                extension: ["jpg", "jpeg"],
                when: _hasFileWithExtension("jpw")
            },
            "A jpg file must accompany a jpw file"
        );
        validator.addRule("required", {
                extension: ["tif", "tiff"],
                when: _hasFileWithExtension("tfw")
            },
            "A tiff file must accompany a tfw file"
        );

        //aux and aux.xml files can apply to either jpg or tiff
        validator.addRule("required", {
                extension: ["tif", "tiff", "jpg", "jpeg"],
                when: _hasFileWithExtension("aux", "aux.xml")
            },
            "an image metadata file must be paired with a JPEG or TIFF file");

        //add suggestions for the image metadata files.
        validator.addSuggestion("required", {
                extension: ["jpw", "aux", "aux.xml"],
                when: _hasFileWithExtension("jpg", "jpeg")
            },
            "consider including an image metadata file such as .jpw, .aux, or .aux.xml"
        );
        validator.addSuggestion("required", {
            extension: ["tfw", "aux", "aux.xml"],
            when: _hasFileWithExtension("tiff", "tif")
        }, "consider including an image metadata file such as .tfw, .aux, or .aux.xml");

        //require the mandatory shapefiles if any shapefiles are present
        $.each(["shp", "shx", "dbf"], function(idx, ext) {
            validator.addRule("required", {
                extension: ext,
                when: function(files) {
                    return $.map(files, function(file){
                        if($.inArray(file.ext, fileinfo.shapefile) > -1) {
                            return file;
                        }
                    }).length > 0
                }
            },
            "A " + ext + " file must be present when uploading shapefiles");
        });

        //all files must have the same base name
        validator.addGroupMethod("same-basename", function(files) {
            var basenames = [];
            $.each(files, function(idx, file){
                if($.inArray(file.base, basenames) === -1) {
                    basenames.push(file.base);
                }
            });
            return basenames.length <= 1;
        }, "all files must have the same base filename");
        validator.addRule("same-basename");

        //only one image metadata file
        validator.addRule("filecount",
            {max: 1, extension:["jpg", "jpeg", "tif", "tiff"]},
            "You may only upload one image record (JPG or TIFF)");

        TDAR.fileupload.addDataTableValidation(validator);
    };

})(console);

        
