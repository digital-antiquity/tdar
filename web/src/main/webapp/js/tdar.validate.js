(function(TDAR, $) {
    'use strict';


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
            var form = event.target, errors = validator.errorList, submitCount = $(form).data("submitCount") || 0, totalErrors = $(form).data("totalErrors") || 0;

            submitCount++;
            //cap the included errors, but show the correct total number of errors
            totalErrors += errors.length;
            $(form).data("submitCount", submitCount).data("totalErrors", totalErrors);
            errors = errors.slice(0 - TDAR.common.maxJavascriptValidationMessages);

            //todo: we really only need to build the dom when the submit is finally successful
            $(form).find("#divClientValidationInfo").remove();
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
            $(form).append($clientInfo);
        }
    };
    
    /**
     * if there's a data attribute to associate with valdiateMethod, then see if it's a function. if it's not a function, then call validate() plain.
     */
    var _init = function() {
        $("form.tdarvalidate").each(function() {
            var $t = $(this);
            var method = $t.data('validate-method');
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
                    var options  = method_(this);
                    var allValidateOptions = $.extend({}, _defaultValidateOptions, options);
                    $t.validate(allValidateOptions);
                    console.log("validate: " + method);
                    $t.data("tdar-validate-status","valid-custom");
                    if (method == 'initBasicForm') {
                    	_postValidateBasic();
                    }
                } else {
                    console.log("validate method specified, but not a function");
                    $t.data("tdar-validate-status","failed-invalid-method");
                }
            } else {
                var allValidateOptions = $.extend({}, _defaultValidateOptions);
                $t.validate(allValidateOptions);
                $t.data("tdar-validate-status","valid-default");
            }
        });
    };

    var _postValidateBasic =  function(form) {
        $('.coverageTypeSelect', "#coverageDateRepeatable").each(function (i, elem) {
            _prepareDateFields(elem);
        });
        $("#coverageDateRepeatable").delegate(".coverageTypeSelect", "change", function () {
            _prepareDateFields(this);
        });

        $(".profileImage").each(function(i, profileElement){
            $(profileElement).rules("add", {
                extension: "jpg,tiff,jpeg,png",
                messages: {
                    extension: "please upload a JPG, TIFF, or PNG file for a profile image"
                }
            });
        });

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

        var $form = $(form);
        //disable double-submit protection if user gets here via backbutton
        var $submit = $form.find(".submitButton").prop("disabled", false);
        var options = {
            errorLabelContainer: $("#error"),
            rules: {
                confirmEmail: {
                    equalTo: "#emailAddress"
                },
                password: {
                    minlength: 3
                },
                username: {
                    minlength: 5
                },
                confirmPassword: {
                    minlength: 3,
                    equalTo: "#password"
                },
                'contributorReason': {
                    maxlength: 512
                }
            },
            messages: {
                confirmEmail: {
                    email: "Please enter a valid email address.",
                    equalTo: "Your confirmation email doesn't match."
                },
                password: {
                    required: "Please enter a password.",
                    minlength: $.validator.format("Your password must be at least {0} characters.")
                },
                confirmPassword: {
                    required: "Please confirm your password.",
                    minlength: $.validator.format("Your password must be at least {0} characters."),
                    equalTo: "Please make sure your passwords match."
                }
            }, submitHandler: function (f) {
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


    TDAR.validate = {
        "init" : _init,
        "initRegForm" : _initRegForm,
        "initBasicForm": _initBasicForm
    }

})(TDAR, jQuery);
$(function() {
    TDAR.validate.init();
});
