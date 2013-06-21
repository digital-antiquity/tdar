/**
 * $Id$
 *
 * jQuery Validator Plugin extension for jQuery File Upload Plugin
 */
//TODO: implement 'suggestions'.  treat them like rules but don't prevent submit.
var FileuploadValidator;
(function(console) {
    "option explicit";

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
                var dupes = $.map(files, function(_file){
                    return file.name === _file.name;
                });
                return dupes.length < 2;
            }
        },


        messages: {
            "nodupes": $.validator.format("Files with duplicated filenames are not allowed.")
        }
    };

    FileuploadValidator = Class.extend({
        //errors resulting from the last validate()
        errors: [],

        //suggestions are a subset of errors that, when present, will show error  messages but not cause uploud to be 'invalid'
        suggestions: [],

        //similar to $.validator methods. defines for given context whether a file is valid, e.g. 'duplicate-name'
        methods: {},

        //rules designate which methods are applied to the fileupload container.
        rules: [],

        //default error messages, keyed by method name
        messages: {},

        //fileupload helper class we created in tdar.upload.js
        helper: null,

        //element representing the 'container' of the fileupload widget (usually top-level form)
        fileupload: null,

        init: function(formId, settings) {
            var self = this;
            console.log("init");
            var errs = [];
            this.fileupload = $("#" + formId)[0];
            $.extend(this, _defaults, settings);
            this.helper = $(this.fileupload).data("fileuploadHelper");
            if(!this.fileupload) errs.push("fileupload element not found");
            if(!this.helper) errs.push("fileupload helper not found - did you call registerFileUpload yet?");
            this.inputSelector = this.helper.inputSelector;
            errs.forEach(function(err){
                console.error(err);
            });

            if(this.validateOnChange) {
                //validate on the fileupload custom events (and the fileuploadreplaced event we added)
                $(this.fileupload).bind("fileuploadcompleted fileuploaddestroyed fileuploadreplaced", function() {
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
            var self = this;
            console.log("validating %s   rulecount:%s", this.fileupload, this.rules.length);
            var files = this.helper.validFiles();
            for(var i = 0; i < this.rules.length; i++) {
                var rule = this.rules[i];

                //optionally only apply a rule if settings has a when-callback
                var when = rule.settings.when || function() {return true;}

                var method = rule.method;
                var message = rule.message;
                this.suggestions = [];
                this.errors = [];
                if(when(files)) {
                    $.each(files, function(idx, file) {
                        var valid = method(file, files, rule.settings);
                        console.log("validate  rule:%s   method:%s   valid:%s", rule, typeof method, valid);
                        if(!valid) {
                            self.highlight(file);
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
                            self.unhighlight(file);
                        }
                    });
                }

                this.clearErrors();
                if(this.errors.length) {
                    this.showErrors();
                }
            }

            //if we have no errors, or the only errors are mere suggestions,  then the uploads are valid
            return this.errors.length === 0 || this.errors.length === this.suggestions.length;
        },

        clearErrors: function() {
            $(this.errorContainer)
                .find("ul").empty()
                .end().hide();
        },

        showErrors: function() {
            var self = this;
            var $container = $(this.errorContainer);
            var $ul = $container.find("ul");
            this.errors.forEach(function(error, idx) {
                var $error = $(self.errorWrapper);
                $error.append("<b>" + error.file.filename + ": </b>")
                    .append("<span>" + error.message + "</span>");
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

        //TODO: (maybe) It doesn't make sense to tie rules to elements, but it might make sense to tie to extensions.
        addRule: function(methodName, settings, customMessage){
            console.log("add rule: %s", methodName);
            var message = this.messages[methodName];
            if(customMessage) {
                message = $.validator.format(customMessage);
            }
            var rule  = {
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


    /** shapefile notes
     *
     * - we need a "required" method
     * - we need to have a dependency mechanism. For example,  .shp is only required when another shapefile is present
     * - in addition to methods that apply to individual files,  we need methods that apply to all files.  what to call them? how to define them?
     *
     */
    
})(console);

        
