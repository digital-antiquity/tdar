/**
 * $Id$
 *
 * jQuery Validator Plugin extension for jQuery File Upload Plugin
 */ 
var FileuploadValidator;
(function(console) {
    "option explicit";
    console.log("this is a test");

    function _id(id) {
        return document.getElementById(id);
    }

    var _defaults = {
        errorContainer: "#fileuploadErrors",
        errorWrapper: "<li class='fileupload-error'></li>",
        errorClass: "fileupload-error",
        okayClass:  "fileupload-okay"
    };

    FileuploadValidator = Class.extend({
        errors: [],
        rules: [],
        methods: {},
        messages: {},
        helper: null,

        init: function(fileuploadId, settings) {
            console.log("init");
            var errs = [];
            $.extend(this, _defaults, settings);
            this.fileupload = _id(fileuploadId);
            this.helper = $(this.fileupload).data("fileuploadHelper");
            if(!this.fileupload) errs.push("fileupload element not found");
            if(!this.helper) errs.push("fileupload helper not found - did you call registerFileUpload yet?");
            errs.forEach(function(err){
                console.error(err)
            });
            console.dir(this);
        },

        validate: function() {
            console.log("validating %s   rulecount:%s", this.fileupload, this.rules.length);
            var files = this.helper.validFiles();
            for(var i = 0; i < this.rules.length; i++) {
                var rule = this.rules[i];
                var method = this.methods[rule.methodName];
                var message = this.messages[rule.methodName];
                var self = this;
                this.errors = $.map(files, function(file, idx) {
                    var valid = !method(file, files, rule.settings)
                    console.log("validate  rule:%s   method:%s   valid:%s", rule, typeof method, valid);
                    if(valid) {
                        self.highlight(file);
                        return {
                            "file": file,
                            "message": (self.messages[rule.methodName])(file.filename, idx)
                        };
                    } else {
                        self.unhighlight(file);
                    }
                });

                this.clearErrors();
                if(this.errors.length) {
                    this.showErrors();
                }
            }
            return this.errors === 0;
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

        //unlike $.validator, it doesn't make sense to tie rules to elements
        addRule: function(methodName, settings){
            console.log("add rule: %s", methodName);
            this.rules.push({
                "methodName": methodName,
                "settings": settings
            });
        },

        highlight: function(file) {
            console.log("highlighting: %s", file.filename);
            file.context.removeClass(this.okayClass).addClass(this.errorClass);
        },

        unhighlight: function(file) {
            console.log("unhighlighting: %s", file.filename);
            file.context.removeClass(this.errorClass).addClass(this.okayClass);
        }

    });
    
})(console)

        
