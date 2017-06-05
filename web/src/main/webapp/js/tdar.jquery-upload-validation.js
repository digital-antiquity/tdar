/**
 * The FileuploadValidator attempts to mimic the API and workflow of the jQuery Validation Plugin for the Blueimp
 * jQuery File Upload Plugin.
 *
 * The jQuery Fileupload Plugin comes with a simple validation callback.  However, this validation does not integrate
 * with well the jQuery Validation Plugin. For example,  Fileupload validation failures
 * do not prevent form submissions, and file upload validation error messages do not appear alongside the jQuery Validation
 * error messages.
 *
 * That's where FileuploadValidator comes in: it mimics jQuery Validation's concepts of "methods" and "rules", and
 * allows them to be applied to async fileuploads using the Blueimp File Upload plugin.  Furthermore, the
 * FileuploadValidator can act part of the jQuery Validation Plugin workflow (this is helpful if you want invalid
 * files to prevent a form submission)
 *
 */
(function (TDAR, fileupload, $, console, window) {
    "use strict";

    /**
     * Creates a "when-callback" that returns true when file list has at least file with provided extension (in varargs)
     * @param {...String} var_args list of extensions
     * @returns {Function} callback function that accepts a list of files. The callback function returns true if any of
     *                      the files use an extension in the extension list.
     * @private
     */
    var _hasFileWithExtension = function () {
        //copy current scope's arguments
        var varargs = Array.prototype.slice.call(arguments, 0);
        return function (files) {
            return $.grep(files, function (file) {
                return $.inArray(file.ext, varargs) > -1;
            }).length > 0;
        }
    };

    /**
     * Unhighlight the files that no longer have errors (due to the last call to validate() )
     * @param validator
     * @param files
     * @private
     */
    var _updateHighlighting = function (validator, files) {
        $.each(files, function (idx, file) {
            validator.unhighlight(file);
        });

        $.each(validator.errors, function (idx, error) {
            if (error.file) {
                validator.highlight(error.file);
            }
        });
    };

    /**
     * If group method rule specifies ignored files, return trimmed list.  Otherwise, return ref to same files list
     * @param files
     * @param settings
     * @returns {*}
     * @private
     */
    var _trimIgnoredFiles = function (files, settings) {
        if (!settings.ignores) {
            return files
        }
        var _files = $.grep(files, function (file) {
            var ignoreThisFile = false;
            $.each(settings.ignores, function (idx, val) {
                //an ignorefile 'rule' can be a callback or a string ( if callback is true, we should ignore file)
                if (typeof val === "function") {
                    ignoreThisFile = val.call(settings, file);
                } else {
                    ignoreThisFile = val.toLowerCase() === file.filename.toLowerCase();
                }
                //stop iterating over ignoreFiles if we already know that we
                return !ignoreThisFile;
            });
            //if ignoring,  return false so that grep wont include it
            return !ignoreThisFile;
        });

        return _files;
    }

    /**
     *  Default settings for the FiluploadValidator constructor
     * @private
     */
    var _defaults = {
        /** selector that identifies the container element for the error messages  */
        errorContainer: "#fileuploadErrors",

        /** html snippet used as wrapper for each error message. Default wrapper is an LI element w/ css class of
         * "fileupload-error".  The error message is always a text node */
        errorWrapper: "<li class='validation-error'></li>",

        /** name of the css class the validator adds to a fileupload table row when the file has at least one
         * validation error.  The validator removes this class once the file for that row is error-free  */
        errorClass: "validation-error",

        /** name of the css class added to a fileupload that had errors but was 'fixed' (i.e subsequent call to validate() yielded no errors)  */
        okayClass: "validation-okay",

        /** implicitly execute validate() whenever the user updates the fileupload list. If false, handlers must call validate() explicitly */
        validateOnChange: true,

        /** When true, this setting alters the *jQuery Validation Plugin* validator so that it only considers the form valid if the
         *  Fileupload section contains no errors.
         *
         *  If false,   $.Validator.validate() will return true (and therefore submit the form) even if the Fileupload
         *  section contains errors.
         **/
        registerJqueryValidateMethod: true,

        /* built-in validation methods */
        methods: {

            /**
             * Returns false if supplied filename matches any other files. in the list (case-insensitive, does not
             * include file extension.
             *
             * @param file
             * @param files
             * @returns {boolean}
             */
            "nodupes": function (file, files) {
                var dupes = $.grep(files, function (_file) {
                    return file.filename.toLowerCase() === _file.filename.toLowerCase();
                });
                return dupes.length < 2;
            },

            /**
             * Returns false the fileupload sections contains two-or-more files with the same extension (even if
             * they have different base-names.
             *
             * @param file
             * @param files
             * @returns {boolean}
             */
            "nodupes-ext": function (file, files) {
                var dupes = $.grep(files, function (_file) {
                    //already lowecase
                    return file.ext === _file.ext;
                });
                return dupes.length < 2;
            },

            /**
             * Group method. Returns false  if not all of the specified file extensions is present in the list of uploaded
             * files.
             * @param files
             * @param settings settings object.  settings.extension specifies the required extension name (can also
             *          be an array of extension names)
             * @returns {boolean}
             */
            "required": function (files, settings) {
                var _files = files;
                if (settings.extension) {
                    var exts = (typeof settings.extension === "string") ? [settings.extension] : settings.extension;
                    _files = $.grep(files, function (file) {
                        return $.inArray(file.ext, exts) > -1;
                    });
                }
                return _files.length > 0;
            },

            /**
             * Fileupload init already has maxFileUpload setting.  Use this method only if you want to
             * limit the occurance number of files that have the same extension
             *
             * @param file
             * @param files
             * @param settings
             * @returns {boolean}
             */
            "filecount": function (file, files, settings) {
                var opts = $.extend({min: 0, max: 100, extension: []}, settings);
                var filecount = $.grep(files, function (file) {
                    return $.inArray(file.ext, opts.extension) > -1;
                }).length;
                return filecount >= opts.min && filecount <= opts.max;
            },

            /**
             * Disallow file replacements.
             *
             * fixme: This is a workaround for TDAR-4722.  If the UI state becomes inconsistent, the UI logic may show the 'Replace' button
             *          even when it is not appropriate (for example,  when adding a new resource instead of editing an existing resource).
             *
             * @param file
             * @param files
             * @param settings
             * @returns {boolean}
             */
            "noreplacements": function(file, files, settings) {
                var replaceFiles = $.grep(files, function(f){
                    return f.action === 'REPLACE';
                });
                return replaceFiles.length === 0;
            }

        },

        /**
         * designates which methods are "group-methods".  Whereas a normal validation method gets called once per
         * file in the file list,  the group-methods evaluate all of files as a whole and are called only once
         * per call to validate()  */
        groupMethods: ["required"],

        rules: [],

        /**
         * default error messages for the built-in validation methods. Can be over-ridden with optional parameter to addRule
         */
        messages: {
            "nodupes": $.validator.format("Files with duplicated filenames are not allowed."),
            "required": $.validator.format("A file attachment is required."),
            "nodupes-ext": $.validator.format("You may only attach one file with this extension"),
            "filecount": $.validator.format("Filecount exceeded"),
            "noreplacements": $.validator.format("You cannot replace a file for a new resource.  Please delete the file instead and upload a new file.")
        }
    };

    /**
     *
     * @type {Class|*}
     */

    function FileuploadValidator(form, settings) {
        this.init(form, settings)
    }

    FileuploadValidator.prototype = {

        /**
         * Note: most fields in this class are not "private" in that they may provide helpful information, but they
         * should  not be modified.
         */

        // list of error objects resulting from the most recent call to validate()
        // error object format is {message:string, file:{filename:string, base:string, ext:string, idx:number}}
        errors: null,

        // list of warnings (aka suggestions) resulting from most recent call to validate().  suggestions are displayed do not effect  whether an upload is considered "valid".
        suggestions: null,

        // similar to jQuery Validation Plugin methods. defines for given context whether a file is valid, e.g. 'duplicate-name'
        methods: null,

        // list of "group" methods group methods are invoked once for all files
        groupMethods: null,

        //list of validation rules. similar to jQuery validation Plugin,  a "rule" specifies a validation method, custom settings for that validation method, as well as custom error messages
        rules: null,

        // default error messages, keyed by method name
        messages: null,

        // fileupload helper class we created in tdar.upload.js
        helper: null,

        // element representing the 'container' of the fileupload widget (usually top-level form)
        fileupload: null,

        /**
         *
         * @param formId ID of the form associated with the Blueimp File Upload control
         * @param settings constructor settings - see _default settings above (FIXME: how do I document a 'settings' object?)
         * @constructs FileValidator
         */
        init: function (form, settings) {
            var self = this;
            //console.debug("init");
            var errs = [];
            this.fileupload = $(form);

            //note the deep copy of defaults is necessary - otherwise modifications to instance properties will change the defaults
            $.extend(true, this, _defaults);
            $.extend(this, settings);

            this.helper = $(this.fileupload).data("fileuploadHelper");
            if (!this.fileupload) {
                errs.push("fileupload element not found");
            }
            if (!this.helper) {
                errs.push("fileupload helper not found - did you call registerFileUpload yet?");
            }
            this.inputSelector = this.helper.inputSelector;
            $.each(errs, function (idx, err) {
                console.error(err);
            });

            if (this.validateOnChange) {
                //validate on the fileupload custom events
                $(this.fileupload).bind("fileuploadstopped fileuploaddestroyed", function () {
                    self.validate();
                });

                //revalidate when the user deletes or "undeletes" a file
                $(this.fileupload).on("click", "button.delete-button", function () {
                    self.validate();
                });
            }

            if (this.registerJqueryValidateMethod) {
                this.registerValidiatorMethod();
            }
        },

        /**
         * Perform validation on the files contained in the Fileupload table.  Display any error messages in the
         * errorContainer,  and highlight the invalid files in the Fileupload table.
         * @returns {boolean} true if all validation rules passed. Otherwise false.
         */
        validate: function () {
            //console.log("validating %s   rulecount:%s", this.fileupload, this.rules.length);
            var self = this;
            this.suggestions = [];
            this.errors = [];

            var files = this.helper.validFiles();

            for (var i = 0; i < this.rules.length; i++) {
                var rule = this.rules[i];

                //optionally only apply a rule if settings has a when-callback
                var when = rule.settings.when || function () {
                    return true;
                }
                files = _trimIgnoredFiles(files, rule.settings);
                if (when(files)) {

                    var method = rule.method;
                    var message = rule.message;

                    //if this is a group method,  execute just once
                    if ($.inArray(rule.methodName, self.groupMethods) > -1) {
                        var valid = method(files, rule.settings);
                        //console.log("applying rule, method:%s   valid:%s", rule.methodName, valid);
                        if (!valid) {
                            var error = {
                                "file": null,
                                "message": message(0)
                            };
                            //console.dir(error);
                            self.errors.push(error);
                            if (rule.suggestion) {
                                self.suggestions.push(error);
                            }
                        }

                        //otherwise execute the method once-per-file
                    } else {
                        $.each(files, function (idx, file) {
                            var valid = method(file, files, rule.settings);
                            //console.log("validate  rule:%s   method:%s   valid:%s", rule, typeof method, valid);
                            if (!valid) {
                                var error = {
                                    "file": file,
                                    "message": message(file.filename, file.base, file.ext, idx)
                                };
                                //console.dir(error);
                                self.errors.push(error);
                                if (rule.suggestion) {
                                    self.suggestions.push(error);
                                }
                            } else {
                            }
                        });

                    }
                }

            }

            this.clearErrorDisplay();
            if (this.errors.length) {
                this.showErrors();
            }
            _updateHighlighting(self, files);

            //if we have no errors, or the only errors are mere suggestions,  then the uploads are valid
            return this.errors.length === 0 || this.errors.length === this.suggestions.length;
        },

        /**
         * Remove messages from the errorContainer.  This does not reset the errors list.
         */
        clearErrorDisplay: function () {
            $(this.errorContainer).find("ul").empty().end().hide();
        },

        /**
         * Display error messages
         */
        showErrors: function () {
            var self = this;
            var $container = $(this.errorContainer);
            var $ul = $container.find("ul");
            $.each(this.errors, function (idx, error) {
                var $error = $(self.errorWrapper);
                if (error.file) {
                    $error.append("<b>" + error.file.filename + ": </b>");
                }
                $error.append("<span>" + error.message + "</span>");
                $ul.append($error);
            });
            $container.show();
        },

        /**
         * Register Fileupload Validation method. The validator invokes method for each file in the table,
         * for each 'rule' added to the validator. The arguments passed to the method are:
         *      file: the file object for the file the method should evaluate
         *      files: the complete list of file objects
         *      settings:  the settings object (if a settings object was provided via validator.addRule() )
         *
         * @param name unique identifier for the validation method.  must be unique.
         * @param method validation function. receives (file, files, settings) as arguments. Return true to indicate
         *              that the specified file is 'valid' for this method. return false or undefined to indicate
         *              an invalid file.
         * @param message  The default error message. Format parameters are:
         *                      "\{0\}": file.filename
         *                      "\{1\}": file.base (filename minus extension)
         *                      "\{2\}": file.ext (file extension not including '.')
         *                      "\{3\}": idx, index of the row in the file table
         */
        addMethod: function (name, method, message) {
            //console.log("addMethod name:%s   method:%s    message:%s", name, typeof method, message);
            this.methods[name] = method;
            if (message) {
                this.messages[name] = $.validator.format(message);
            } else {
                this.messages[name] = $.validator.format("This file is invalid");
            }
        },

        /**
         * Register Fileupload Validation "group" method.
         * @param name unique identifier for the validation method.  must be unique.
         * @param method validation function. receives (file, files, settings) as arguments. Return true to indicate
         *              that the specified file is 'valid' for this method. return false or undefined to indicate
         *              an invalid file.
         * @param message  The default error message. Format parameters are:
         *                      "{0\}": file.filename
         *                      "{1\}": file.base (filename minus extension)
         *                      "{2\}": file.ext (file extension not including '.')
         *                      "{3\}": idx, index of the row in the file table
         */
        addGroupMethod: function (name, method, message) {
            this.groupMethods.push(name);
            this.addMethod(name, method, message);
        },

        /**
         * Add a new validation rule.
         *
         * @param methodName name of the validation method that the validator will apply
         * @param settings custom settings for the method
         * @param customMessage customized error message (overrides the method's default error message)
         * @returns {{methodName: *, method: *, settings: (*|{}), message: *, suggestion: boolean}}
         */
        addRule: function (methodName, settings, customMessage) {
            var message = this.messages[methodName];
            if (customMessage) {
                message = $.validator.format(customMessage);
            }
            var rule = {
                "methodName": methodName,
                "method": this.methods[methodName],
                "settings": settings || {},
                "message": message,
                "suggestion": false
            };
            this.rules.push(rule);
            return rule;
        },

        /**
         * Add a 'suggestion'.  Similar to validator.addRule() but have no effect on the file table's 'validity' status.
         * In other words ,they will not prevent form submission.
         * @param methodName
         * @param settings
         * @param customMessage custom error message
         */
        addSuggestion: function (methodName, settings, customMessage) {
            var rule = this.addRule(methodName, settings, customMessage);
            rule.suggestion = true;
        },

        /**
         * Highlight the rows in the file table that have errors, using the css class name defined in
         * settings.errorClass.
         * @param file
         */
        highlight: function (file) {
            //console.log("highlighting: %s", file.filename);
            file.context.removeClass(this.okayClass).addClass(this.errorClass);
        },

        /**
         * Remove all highlighting from the file table.
         *
         * @param file
         */
        unhighlight: function (file) {
            //console.log("unhighlighting: %s", file.filename);
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
            var self = this, methodName = "valid-fileupload";
            $.validator.addMethod(methodName, function () {
                        return self.validate();
                    }, "There was a problem with your uploaded files.  See details in the file upload section");

            //we've added the method, now we need the specific rule that binds the fileinput element to the method
            $(self.inputSelector).addClass(methodName)
        }
    };
    TDAR.fileupload.FileuploadValidator = FileuploadValidator;

    //FIXME: move this function to tdar.dataintegration.js
    /**
     * Add validation dataset-specific validation rules to the specified validator.
     * @param FileuploadValidator the validator instance
     */
    TDAR.fileupload.addDataTableValidation = function (validator) {
        //only one image metadata file
        validator.addRule("filecount", {max: 1, extension: ["accdb", "mdb", "xls", "xlsx", "gdb"]}, "You may only upload one spreadsheet or access database (MDB, ACCDB, XLS, GDB, XLSX)");
    };

    //FIXME: move this function to a gis-specific js file (also, create a gis-specific js file)
    /**
     * Add GIS-specific validation rules to the specified validator.
     * @param validator the validator instance
     */
    TDAR.fileupload.addGisValidation = function (validator) {
        var fileinfo = {
            shapefile: ["shp", "shx", "dbf", "sbn", "sbx", "fbn", "fbx", "ain", "aih", "atx", "ixs", "mxs", "prj", "xml", "cpg"],
            jpeg: ["jpg", "jpeg", "jpw", "jgw"],
            tiff: ["tif", "tiff", "tfw"],
            image: ["jpg", "jpeg", "jpw", "jgw", "tfw", "aux", "ovr", "rrd", "aux.xml", "mxd"]
        };

        var requiredFiles = {
            shapefile: ["shp", "shx", "dbf","prj"]
        }

        //require image files if image metadata file is present
        validator.addRule("required", {
                    extension: ["jpg", "jpeg"],
                    when: _hasFileWithExtension("jpw")
                }, "A jpg file must accompany a jpw file");
        validator.addRule("required", {
                    extension: ["jpg", "jpeg"],
                    when: _hasFileWithExtension("jgw")
                }, "A jpg file must accompany a jgw file");
        validator.addRule("required", {
                    extension: ["tif", "tiff"],
                    when: _hasFileWithExtension("tfw")
                }, "A tiff file must accompany a tfw file");
        validator.addRule("required", {
            extension: ["tif", "tiff","jpg","jpeg",'accdb','mdb'],
            when: _hasFileWithExtension("mxd")
                }, "An image file or geodatabase must accompany a map file");

        //aux and aux.xml files can apply to either jpg or tiff
        validator.addRule("required", {
                    extension: ["tif", "tiff", "jpg", "jpeg"],
                    when: function (files) {
                        //if adf present, this is not a shape file, so ignore this rule
                        //TODO: confirm this is actually true
                        return _hasFileWithExtension("aux", "aux.xml", "ovr", "rrd")(files) && !_hasFileWithExtension("adf")(files);
                    }
                }, "an image metadata file must be paired with a JPEG or TIFF file");

        //add suggestions for the image metadata files (unless user is uploading an image raster + mxd file)
        validator.addSuggestion("required", {
                    extension: ["jpw", "aux", "aux.xml", "ovr", "rrd"],
                    when: function (files) {
                        return ( _hasFileWithExtension("jpg", "jpeg")(files) && !_hasFileWithExtension("mxd")(files) );
                    }
                }, "consider including an image metadata file such as .jpw, .aux, or .aux.xml");
        validator.addSuggestion("required", {
            extension: ["tfw", "aux", "aux.xml", "ovr", "rrd"],
            when: function (files) {
                return ( _hasFileWithExtension("tiff", "tif")(files) && !_hasFileWithExtension("mxd")(files) )
            }
        }, "consider including an image metadata file such as .tfw, .aux, or .aux.xml");

        //require the mandatory shapefiles if any shapefiles are present
        $.each(["shp", "shx", "dbf", "prj"], function (idx, ext) {
            validator.addRule("required", {
                        extension: ext,
                        when: function (files) {
                            return $.map(files, function (file) {
                                if ($.inArray(file.ext, fileinfo.shapefile) > -1) {
                                    return file;
                                }
                            }).length > 0
                        },
                        ignores: ["metadata.xml"]
                    }, "A " + ext + " file must be present when uploading shapefiles");
        });

        //all files must have the same base name
        //todo: move 'same-basename' to default group methods
        validator.addGroupMethod("same-basename", function (files, settings) {
            var basenames = [];
            $.each(files, function (idx, file) {
                if ($.inArray(file.base.toLowerCase(), basenames) === -1) {
                    basenames.push(file.base.toLowerCase());
                }
            });
            return basenames.length <= 1;
        }, "all files must have the same base filename");

        validator.addRule("same-basename", {
            ignores: [
                "metadata.xml", function (file) {
                    return file.ext === "adf"
                }]
        });

        //only one image metadata file
        validator.addRule("filecount", {max: 1, extension: ["jpg", "jpeg", "tif", "tiff"]}, "You may only upload one image record (JPG or TIFF)");

        TDAR.fileupload.addDataTableValidation(validator);
    };
})(TDAR, TDAR.fileupload, jQuery, console, window);
