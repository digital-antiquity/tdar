TDAR.validate = (function($, ctx) {
    "use strict";


    /**
     * Default settings for form validation in tDAR forms
     *
     * @type {{errorLabelContainer: (*|jQuery|HTMLElement), wrapper: string, highlight: highlight, unhighlight: unhighlight, showErrors: showErrors, invalidHandler: invalidHandler}}
     * @private
     */
    var _defaultValidateOptions = {
        errorLabelContainer: $("#error ul"),
        wrapper: "li",
        highlight: function (element, errorClass, validClass) {
            $(element).addClass("error");
            $(element).closest("div.control-group").addClass("error");
        },
        unhighlight: function (element, errorClass, validClass) {
            $(element).trigger("unhighlight", [errorClass, validClass]);
            $(element).removeClass("error");
            //highlight this div until all visible controls in group are valid
            var $controlGroup = $(element).closest("div.control-group");
            if ($controlGroup.find('.error:visible').length === 0) {
                $controlGroup.removeClass("error");
            }
        },
        showErrors: function (errorMap, errorList) {
            this.defaultShowErrors();
        },
        //send validation errors to the server  TODO: cap the total number of errors
        invalidHandler: function (event, validator) {
            var form = event.target;
            var $form = $(form);
            var errors = validator.errorList;
            var submitCount = $form.data("submitCount") || 0;
            var totalErrors = $form.data("totalErrors") || 0;

            submitCount++;
            //cap the included errors, but show the correct total number of errors
            totalErrors += errors.length;
            $form.data("submitCount", submitCount).data("totalErrors", totalErrors);
            errors = errors.slice(0 - TDAR.common.maxJavascriptValidationMessages);

            //todo: we really only need to build the dom when the submit is finally successful
            $form.find("#divClientValidationInfo").remove();
            var $clientInfo = $('<div id="divClientValidationInfo"></div>');

            var template = $.validator.format('<input type="hidden" name="clientValidationInfo[\'{0}\']" value="{1}">');

            $.each(errors, function (idx, error) {
                var key = "error_" + idx;
                var msg = TDAR.common.htmlEncode("" + error.element.id + ": " + error.message);
                var $input = $(template(key, msg));
                $input.val(msg);
                $clientInfo.append($input);
            });

            //now tack on the total errors and submission attempts
            $clientInfo.append($(template("totalErrors", totalErrors)));
            $clientInfo.append($(template("submitCount", submitCount)));
            $form.append($clientInfo);
        }
    };
    
    /**
     * Validate a single form, called out and returning the validator to enable testing 
     */
    var _initForm = function (form) {
        var $form = $(form);
        console.log($form.attr('id'));
        var method = $form.data('validate-method');
        var validator;
        console.log(method);
        if (method != undefined) {
            var method_ = window[method];
            // FIXME: There has to be a better way to bind these
            if ($.isFunction(window['TDAR']['validate'][method])) {
                method_ = window['TDAR']['validate'][method];
            }
            if ($.isFunction(window[method])) {
                method_ = window[method];
            }               
            if (method_ != undefined) {
                var options  = method_($form);
                var allValidateOptions = $.extend({}, _defaultValidateOptions, options);
                validator = $form.validate(allValidateOptions);
                console.log("validate: " + method);
                $form.data("tdar-validate-status","valid-custom");
                if (method == 'initBasicForm') {
                    _postValidateBasic($form, validator);
                }
            } else {
                console.log("validate method specified, but not a function");
                $form.data("tdar-validate-status","failed-invalid-method");
            }
        }


        var allValidateOptions = $.extend({}, _defaultValidateOptions);
        validator = $form.validate(allValidateOptions);
        $form.data("tdar-validate-status","valid-default");
        return validator;
    };
    
    /**
     * if there's a data attribute to associate with valdiateMethod, then see if it's a function. if it's not a function, then call validate() plain.
     */
    var _init = function() {
        $("form.tdarvalidate").each(function() {
            _initForm($(this));
        });
    };

    var _postValidateBasic =  function($form, validator) {
        $('.coverageTypeSelect', "#coverageDateRepeatable",$form).each(function (i, elem) {
            _prepareDateFields(elem);
        });
        $("#coverageDateRepeatable", $form).delegate(".coverageTypeSelect", "change", function () {
            _prepareDateFields(this);
        });

        var $uploaded = $( '_uploadedFiles', $form);
        if ($uploaded.length > 0) {
            var _validateUploadedFiles = function () {
                if ($uploaded.val().length > 0) {
                    $("#reminder").hide();
                }
            };
            $uploaded.change(_validateUploadedFiles);
            _validateUploadedFiles();
        }

        //fixme:  confirm that input[file] elements actually use this validation rule - I'm pretty sure it's overridden by jquery-file-upload
        if ($form.data("valid-extensions")) {
            var validExtensions = $form.data("valid-extensions");
            console.log(validExtensions);
            var msg = "Please enter a valid file (" + validExtensions.replace("|", ", ")+ ")";
            $(".validateFileType",$form).each(function(i, elem) {
                $(elem).rules("add", {
                    extension: validExtensions,
                    messages: {
                        extension: msg
                    }
                });
            });
        }

        var fileValidator;
        if ($form.data("multiple-upload")) {
            fileValidator = new TDAR.fileupload.FileuploadValidator($form);
            fileValidator.addRule("nodupes");
    
            //fixme: (TDAR-4722) prohibit file replacements on 'add' pages. Due to bug, UI may display 'replace' option even when it shouldn't.
            // Until bug is fixed, we use this additional workaround to prevent the user from submitting if the UI allowed an invalid replacement.
            var path = window.location.pathname
            if(path.length && path.match(/(document|dataset|image).add$/)) {
                fileValidator.addRule("noreplacements");
            }
    
            TDAR.fileupload.validator = fileValidator;
        }
        
        
        if ($form.data("datatable")) {
            if (fileValidator) {
                TDAR.fileupload.addDataTableValidation(TDAR.fileupload.validator);
            } else {
                console.error("no file validator defined");
            }
        }
        
        
        var _type = $form.data("type");
        if (_type == 'GEOSPATIAL') {
            TDAR.fileupload.addGisValidation(fileValidator);
        }
        

        if (!$form.data("multiple-upload") && ($form.data("total-files") == 0 || $form.data("total-files") > 0)) {
            var rtype = $form.data("resource-type");
            var $textarea = $('#fileInputTextArea',$form);
            var $uploadfield = $('#fileUploadField',$form);
            // both must exist...
            if ($textarea.length > 0 && $uploadfield.length  > 0) {
                $textarea.rules("add", {
                            required: {
                                depends: _isSupportingFileFieldRequired
                            },
                            messages: {
                                required: "No " + rtype + " data entered. Please enter " + rtype + " manually or upload a file."
                            }
                        });
                $uploadfield.rules("add", {
                    required: {
                        depends: _isSupportingFileFieldRequired
                    },
                    messages: {
                        required: "No " + rtype + " file selected. Please select a file or enter " + rtype + " data manually."
                    }
                });
            }


        }
    }
    
    var _isSupportingFileFieldRequired = function(elem) {
        var totalNumberOfFiles = $(elem.form).data("total-files");
        var noRulesExist = !((totalNumberOfFiles > 0) || ($("#fileInputTextArea").val().length > 0) || ($("#fileUploadField").val().length > 0));
        return noRulesExist && $(elem).is(":visible");
    }
    
    var _initBasicForm = function(form) {
        var options = {
                onkeyup: function () {
                    return;
                },
                onclick: function () {
                    return;
                },
                onfocusout: function (element) {
                    return;
                },
                showErrors: function (errorMap, errorList) {
                    this.defaultShowErrors();
                    //spawn a modal widget and copy the errorLabelContainer contents (a separate div) into the widget's body section
                    //TODO: docs say this is only called when errorList is not empty - can we remove this check?
                    if (typeof errorList !== "undefined" && errorList.length > 0) {
                        $('#validationErrorModal .modal-body p').empty().append($("<ul></ul>").append($('#error ul').html()));
                        $('#validationErrorModal').modal();

                    }
                    $('#error').show();
                },
                submitHandler: function (f) {
                    //prevent double submit and dazzle user with animated gif
                    _submitButtonStartWait();

                    /* Creator entry controls display one of two field sets if the creator is "person" or
                     institution". Disable the hidden set so the set's input vals aren't sent to server.
                     */
                    $(f).find(".creatorPerson.hidden, .creatorInstitution.hidden").find(":input").prop("disabled", true);

                    $('#error').hide();
                    $(f).FormNavigate("clean");
                    f.submit();
                }
            };
        return options;
    }

    
    /**
     * Disable any submit buttons on a form, and display a "please wait" graphic beside the submit buttons.
     * Useful for  preventing double-submits.
     * @private
     */
    var _submitButtonStartWait = function () {
        var $submitDivs = $('#editFormActions, #fakeSubmitDiv');
        var $buttons = $submitDivs.find(".submitButton");
        $buttons.prop("disabled", true);

        //fade in the wait icon
        $submitDivs.find(".waitingSpinner").show();
    };


    
    // called whenever date type changes
    //FIXME: I think we can improve lessThanEqual and greaterThenEqual so that they do not require parameters, and hence can be
//         used via $.validator.addClassRules.  The benefit would be that we don't need to register these registration rules each time a date
//         gets added to the dom.
    //FIXME: this might be duplicated in tdar.formValidateExtensions.  If not, it should probably be migrated there.
    /**
     * Add specific rules to a the text fields associated with a "coverage date" control.
     *
     * @param selectElem the select input element associated with the "fromYear" and "toYear" text inputs (must be a
     *          sibling element in the same container)
     * @private
     */
    var _prepareDateFields = function (selectElem) {
        var startElem = $(selectElem).siblings('.coverageStartYear');
        var endElem = $(selectElem).siblings('.coverageEndYear');
        $(startElem).rules("remove");
        $(endElem).rules("remove");
        switch ($(selectElem).val()) {
            case "CALENDAR_DATE":
                $(startElem).rules("add", {
                    range: [ -99900, 2100 ],
                    lessThanEqual: [endElem, "Calender Start", "Calendar End"],
                    required: function () {
                        return $(endElem).val() != "";
                    }
                });
                $(endElem).rules("add", {
                    range: [ -99900, 2100 ],
                    required: function () {
                        return $(startElem).val() != "";
                    }
                });
                break;
            case "RADIOCARBON_DATE":
                $(startElem).rules("add", {
                    range: [ 0, 100000 ],
                    greaterThanEqual: [endElem, "Radiocarbon Start", "Radiocarbon End"],
                    required: function () {
                        return $(endElem).val() != "";
                    }
                });
                $(endElem).rules("add", {
                    range: [ 0, 100000 ],
                    required: function () {
                        return $(startElem).val() != "";
                    }
                });
                break;
            case "NONE":
                $(startElem).rules("add", {
                    blankCoverageDate: {"start": startElem, "end": endElem}
                });
                break;
        }
    };
    
    var _initRegForm = function(form) {

        var $form = form;
        //disable double-submit protection if user gets here via backbutton
        var $submit = $form.find(".submitButton").prop("disabled", false);
        var options = {
            //FIXME: allow for error label container to be specified from options,
            wrapper: "",
            showErrors: null, //use default showErrors
            //errorLabelContainer: $("#error"),
            errorClass: 'help-block',
            highlight: function (label) {
                $(label).closest('.control-group').addClass('error');
            },
            submitHandler: function (f) {
                var $submit = $(f).find(".submitButton").prop("disabled", true);
                //prevent doublesubmit for certain amount of time.
                $submit.prop("disabled", true);
                setTimeout(function () {
                    $submit.prop("disabled", false);
                }, 10 * 1000)
                f.submit();
            }
        };
        return options;
    };


    return {
        "init" : _init,
        "initForm" : _initForm,
        "initRegForm" : _initRegForm,
        "initBasicForm": _initBasicForm,
        "prepareDateFields": _prepareDateFields
    }
})(jQuery, window);

//FIXME: inline onload binding complicates testing and makes it harder to discover the total number of initializers (and their sequence)
$(function() {
    TDAR.validate.init();
});
