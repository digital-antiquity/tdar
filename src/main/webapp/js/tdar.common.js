/*
 * $Id$
 * 
 * Common JS functions used in tDAR (with dependency on JQuery).  
 * Mostly have to do with adding new rows for multi-valued fields, etc.
 */

/**
 * Returns a copy of a string, terminated by ellipsis if input string exceeds max length
 * @param str input string
 * @param maxlength maximum length of the copy string.
 * @returns {*} copy of string no longer than maxlength.
 */
TDAR.ellipsify = function (str, maxlength) {
    if (!str) {
        return;
    }
    var newString = str;
    if (str.length > maxlength) {
        newString = str.substring(0, maxlength - 3) + "...";
    }
    return newString;
};

jQuery.extend({
    /**
     * Compare two arrays. return true if A and B contain same elements
     *
     * @param arrayA
     * @param arrayB
     * @param ignoreOrder if true, ignore order of the array contents (optional: default true)
     * @returns {boolean} true if equal, otherwise false.
     */
    compareArray: function (arrayA, arrayB, ignoreOrder) {
        //FIXME: break this into two functions (no bool args!)
        //FIXME: no need to extend jquery, just add to tdar.common.
        if (arrayA.length !== arrayB.length) {
            return false;
        }
        // ignore order by default
        if (typeof ignoreOrder === 'undefined') {
            ignoreOrder = true;
        }
        var a = arrayA, b = arrayB;
        if (ignoreOrder) {
            a = jQuery.extend(true, [], arrayA);
            b = jQuery.extend(true, [], arrayB);
            a.sort();
            b.sort();
        }
        for (var i = 0, l = a.length; i < l; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
});

/**
 * trying to move these functions out of global scope and apply strict parsing.
 */

TDAR.common = function () {
    "use strict";

    var self = {};

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
                var msg = _htmlEncode("" + error.element.id + ": " + error.message);
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
     * Specify the target element for any adhoc child windows spawned from the current page.
     *
     * Some forms fields allow the user to create a new resource in a child window.  This function allows caller to
     * specify the element on the parent window. When the user completes the child form,  the tdar will update
     * the target form field with the ID/name of the resource that the user created in the child window.
     *
     * @param elem  context root
     * @param selector  jqselector which contains the target (optional: default "div")
     */
    var _setAdhocTarget = function (elem, selector) {
        var _selector = selector;
        if (!_selector) {
            selector = "div";
        }
        var adhocTarget = $(elem).closest(_selector);
        $('body').data("adhocTarget", adhocTarget);
        //expose target for use by child window
        TDAR.common.adhocTarget = adhocTarget;
        //return false;
    }

    /**
     * Populate a coding sheet / ontology field (aka the adhoctarget) with the id/name of the object created via the
     * child page.
     *
     * Note: tdar cannot handle multiple, simultaneous adhoc child windows (though this is unlikely to happen)
     *
     * @param obj jsobject with id + title properties
     *
     */
    var _populateTarget = function (obj) {
        var $body = $("body");
        var adhocTarget = $body.data("adhocTarget");
        if (typeof(adhocTarget) == 'undefined') {
            return;
        }

        console.log("populateTarget called.   adHocTarget:%s", adhocTarget);
        $('input[type=hidden]', adhocTarget).val(obj.id);
        $('input[type=text]', adhocTarget).val(obj.title);
        $body.removeData("adhocTarget");
        TDAR.common.adhocTarget = null;
    }

    // FIXME: refactor.  needs better name and it looks brittle
    /**
     * Return a sort function that alphabetically sorts an object w/ specified property name.
     * @param property  name of the property in an object to evaluate when comparing two objects.
     * @param caseSensitive  true if the sort function should be case sensitive (optional: default false)
     * @returns {Function} function for use with Array.sort()
     * @private
     */
    var _dynamicSort = function (property, caseSensitive) {
        return function (a, b) {
            if (caseSensitive == undefined || caseSensitive == false) {
                return (a[property].toLowerCase() < b[property].toLowerCase()) ? -1 : (a[property].toLowerCase() > b[property].toLowerCase()) ? 1 : 0;
            } else {
                return (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
            }
        };
    }

    /**
     * Not implemented  TDAR-3495
     * @private
     */
    var _sortFilesAlphabetically = function () {
        //FIXME:  implement this and migrate to tdar.fileupload
    }

    /**
     * Update display of copyright licenses section when the radio button selection changes
     * @private
     */
    var _toggleLicense = function () {
        $("#license_section input[type='radio']").each(function (index) {
                    // show or hide the row depending on whether the corresponding radio button is checked
                    var $this = $(this);
                    var license_type_name = $this.val();
                    var license_details_reference = "#license_details_" + license_type_name;
                    var license_details = $(license_details_reference);
                    var $licenseText = $('#licenseText');
                    if ($this.is(":checked")) {
                        license_details.removeClass('hidden');
                    } else {
                        license_details.addClass('hidden');
                    }
                    if (!$licenseText.is(':hidden')) {
                        $licenseText.addClass("required");
                    } else {
                        $licenseText.removeClass("required");
                    }
                });
    }

    /**
     * Initialize jquery valiation for the specified tdar edit form
     *
     * @param form form element to apply validation rules
     */
    var _setupFormValidate = function (form) {
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

        var allValidateOptions = $.extend({}, _defaultValidateOptions, options);
        $(form).validate(allValidateOptions);
    };

    /**
     * Specific initialization for the user registration form
     * @param form
     */
    var _initRegformValidation = function (form) {

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
                'person.contributorReason': {
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
                    minlength: jQuery.format("Your password must be at least {0} characters.")
                },
                confirmPassword: {
                    required: "Please confirm your password.",
                    minlength: jQuery.format("Your password must be at least {0} characters."),
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
        $form.validate($.extend({}, _defaultValidateOptions, options));

    };

    //FIXME: wny is this broken out from  initEditPage?   If anything, break it out even further w/ smaller private functions
    /**
     * Further initialization for tdar "edit" pages
     *
     * @param form
     */
    var _setupEditForm = function (form) {
        var $form = $(form);
        //fun fact: because we have a form field named "ID",  form.id actually refers to this DOM element,  not the ID attribute of the form.
        var formid = $form.attr("id");

        // prevent "enter" from submitting
        $form.delegate('input,select', "keypress", function (event) {
            return event.keyCode != 13;
        });

        //initialize form validation
        _setupFormValidate(form);

        //prepwork prior to form submit (trimming fields)
        $form.submit(function (f) {
            try {
                $.each($('.reasonableDate, .coverageStartYear, .coverageEndYear, .date, .number'), function (idx, elem) {
                    if ($(elem).val() !== undefined) {
                        $(elem).val($.trim($(elem).val()));
                    }
                });
            } catch (err) {
                console.error("unable to trim:" + err);
            }

            var $button = $('input[type=submit]', f);
            $button.siblings(".waitingSpinner").show();

            //warn user about leaving before saving
            $("#jserror").val("");
            return true;
        });

        $('.coverageTypeSelect', "#coverageDateRepeatable").each(function (i, elem) {
            _prepareDateFields(elem);
        });

        var $uploaded = $(formid + '_uploadedFiles');
        if ($uploaded.length > 0) {
            var validateUploadedFiles = function () {
                if ($uploaded.val().length > 0) {
                    $("#reminder").hide();
                }
            };
            $uploaded.change(validateUploadedFiles);
            validateUploadedFiles();
        }

        Modernizr.addTest('cssresize', Modernizr.testAllProps('resize'));

        if (!Modernizr.cssresize) {
            $('textarea.resizable:not(.processed)').TextAreaResizer();
        }

        $("#coverageDateRepeatable").delegate(".coverageTypeSelect", "change", function () {
            _prepareDateFields(this);
        });
        _showAccessRightsLinkIfNeeded();
        $('.fileProxyConfidential').change(_showAccessRightsLinkIfNeeded);

        //FIXME: idea is nice, but default options produce more annoying UI than original browser treatment of 'title' attribute. also, bootstrap docs
        //       tell you how to delegate to selectors but I couldn't figure it out.
        //$(form).find('label[title]').tooltip();

        if ($('#explicitCoordinatesDiv').length > 0) {
            $('#explicitCoordinatesDiv').toggle($('#viewCoordinatesCheckbox')[0].checked);

        }
        $(".latLong").each(function (index, value) {
            $(this).hide();
            //copy value of hidden original to the visible text input
            var id = $(this).attr('id');
            $('#d_' + id).val($('#' + id).val());
        });

        $("#jserror").val("SAVE");

        // delete/clear .repeat-row element and fire event
        $('#copyrightHolderTable').on("click", ".row-clear", function (e) {
            var rowElem = $(this).parents(".repeat-row")[0];
            TDAR.repeatrow.deleteRow(rowElem);
        });

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

    /**
     * Initialize an unordered list element (with .tdar-treeview class) so that it renders as a "tree view" control
     */
    var _applyTreeviews = function () {
        //console.debug("applying tdar-treeviews v3");
        var $treeviews = $(".tdar-treeview");
        $treeviews.treeview({
            collapsed: true
        });
        // expand ancestors if any children are selected
        $treeviews.find("input:checked").parentsUntil(".treeview", "li").find("> .hitarea").trigger("click");
    };

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

    /**
     * re-enable buttons disabled by submitButtonStartWait()
     * @private
     */
    var _submitButtonStopWait = function () {
        var $submitDivs = $('#editFormActions, #fakeSubmitDiv');
        var $buttons = $submitDivs.find(".submitButton");
        $buttons.prop("disabled", false);

        //fade in the wait icon
        $submitDivs.find(".waitingSpinner").hide();
    }

    /**
     * Perform initialization and setup for a typical elements and functionality of a tdar "edit page".  This does not
     * include initialization tasks for specific edit pages with unique functionality.
     *
     * @param form the form to initialize
     * @private
     */
    var _initEditPage = function (form) {

        //Multi-submit prevention disables submit button, so it will be disabled if we get here via back button. So we explicitly enable it.
        _submitButtonStopWait();

        $("#fakeSubmitButton").click(function () {
            $("#submitButton").click();
        });

        $("#subnavbar .nav a").click(function () {
            $($(this).attr('href')).animate({
                backgroundColor: "#ffffee"
            }, 200).animate({
                backgroundColor: "transparent"
            }, 400);
            return true;
        });

        //init repeatrows
        TDAR.repeatrow.registerRepeatable(".repeatLastRow");

        //init person/institution buttons
        $(".creatorProxyTable").on("click", '.creator-toggle-button', function (event) {
            var $this = $(this);
            var $top = $this.closest(".repeat-row");
            if ($top == undefined) {
                $top = $this.closest(".control-group");
            }
            var $toggle = $(".creator-toggle-button input:hidden", $this);
            if ($(event.target).hasClass("personButton")) {
                $(".creatorPerson", $top).removeClass("hidden");
                $(".creatorInstitution", $top).removeClass("hidden").addClass("hidden");
                $toggle.val("PERSON");
            } else {
                $(".creatorPerson", $top).removeClass("hidden").addClass("hidden");
                $(".creatorInstitution", $top).removeClass("hidden");
                $toggle.val("INSTITUTION");
            }
        });

        //wire up autocompletes
        TDAR.autocomplete.delegateCreator("#authorshipTable", false, true);
        TDAR.autocomplete.delegateCreator("#creditTable", false, true);
        TDAR.autocomplete.delegateCreator("#divAccessRights", true, false);
        TDAR.autocomplete.delegateCreator("#divSubmitter", true, false);
        TDAR.autocomplete.delegateCreator("#copyrightHolderTable", false, true);
        TDAR.autocomplete.delegateAnnotationKey("#resourceAnnotationsTable", "annotation", "annotationkey");
        TDAR.autocomplete.delegateKeyword("#siteNameKeywordsRepeatable", "sitename", "SiteNameKeyword");
        TDAR.autocomplete.delegateKeyword("#uncontrolledSiteTypeKeywordsRepeatable", "siteType", "SiteTypeKeyword");
        TDAR.autocomplete.delegateKeyword("#uncontrolledCultureKeywordsRepeatable", "culture", "CultureKeyword");
        TDAR.autocomplete.delegateKeyword("#temporalKeywordsRepeatable", "temporal", "TemporalKeyword");
        TDAR.autocomplete.delegateKeyword("#otherKeywordsRepeatable", "other", "OtherKeyword");
        TDAR.autocomplete.delegateKeyword("#geographicKeywordsRepeatable", "geographic", "GeographicKeyword");
        TDAR.autocomplete.applyInstitutionAutocomplete($('#txtResourceProviderInstitution'), true);
        TDAR.autocomplete.applyInstitutionAutocomplete($('#publisher'), true);
        $('#resourceCollectionTable').on("focus", ".collectionAutoComplete", function () {
                    TDAR.autocomplete.applyCollectionAutocomplete($(this), {showCreate: true, showCreatePhrase: "Create a new collection"}, {permission: "ADMINISTER_GROUP"});
                });

        // prevent "enter" from submitting
        $('input,select').keypress(function (event) {
            return event.keyCode != 13;
        });

        //init sortables
        //FIXME: sortables currently broken 
        $(".alphasort").click(_sortFilesAlphabetically);

        //ahad: toggle license
        $(".licenseRadio", $("#license_section")).change(_toggleLicense);

        //monitor document height and fire event when it changes
        $.documentHeightEvents();

        //Refresh any scrollspies whenever document height changes.
        $('[data-spy="scroll"]').each(function () {
            var $scrollspy = $(this);

            $(document).bind("repeatrowadded repeatrowdeleted heightchange", function () {
                console.log("resizing scrollspy");
                $scrollspy.scrollspy("refresh");
            });
        });

        TDAR.contexthelp.initializeTooltipContent(form);
        _applyWatermarks(form);

        //FIXME: other init stuff that is separate function for some reason 
        _setupEditForm(form);

        _applyTreeviews();

        //show project preview button when appropriate
        $('#projectId').change(function () {
            var $select = $(this);
            var $row = $select.closest('.controls-row');
            $('.view-project', $row).remove();
            if ($select.val().length > 0 && $select.val() !== "-1") {
                var href = getURI('project/' + $select.val());
                var $button = '<a class="view-project btn btn-small" target="_project" href="' + href + '">View project in new window</a>';
                $row.append($button);
            }
        }).change();

        //display generic wait message with ajax events
        _registerAjaxEvents();

        // I must be "last"
        $(form).not('.disableFormNavigate').FormNavigate({
            message: "Leaving the page will cause any unsaved data to be lost!",
            customEvents: "repeatrowdeleted fileuploadstarted",
            cleanOnSubmit: false
        });

    };

    /**
     * Perform initialization tasks for a typical tdar "view" page. Elements and functionality that are unique to a
     * specific page are not addressed by this funtion.
     */
    var _initializeView = function () {
        console.debug('initialize view');
        var mapdiv, $divSearchContext = $("#divSearchContext");

        if ($('#large-google-map').length) {
            mapdiv = $('#large-google-map')[0];
        }
        ;
        if ($(".google-map").length) {
            mapdiv = $('.google-map')[0];
        }
        if (mapdiv != undefined) {
            var inputContainer = $("#divCoordContainer")[0];
            TDAR.maps.initMapApi();
            TDAR.maps.setupMap(mapdiv, inputContainer);
        }

        if ($divSearchContext.length === 1) {
            $(".searchbox").focus(function () {
                $divSearchContext.addClass("active");
            }).blur(function () {
                //$divSearchContext.removeClass("active");
            });
        }

    };

    /**
     * Register event listener that displays generic wait message for ajax requests. If the ajaxOptions property
     * of the event contain a "waitmessage" property, display that messages, otherwise the function displays "Loading"
     * while the request is in flight, and "Done" after the request is complete.
     */
    var _registerAjaxEvents = function () {
        $('body').bind('ajaxSend', function (e, jqXHR, ajaxOptions) {
            if (typeof ajaxOptions.waitMessage === "undefined") {
                ajaxOptions.waitMessage = "Loading";
            }
            $('#ajaxIndicator').html("<strong>Waiting</strong>: " + ajaxOptions.waitMessage + "...").fadeIn('fast');
            //TODO: include a timeout to dismiss loading or display warning mesage
        });
        $('body').bind('ajaxComplete', function (e, jqXHR, ajaxOptions) {
            $('#ajaxIndicator').html("<strong>Complete</strong>: " + ajaxOptions.waitMessage + "...").fadeOut(1000);
        });

    };

    /**
     * Return html-encoded copy of provided string
     * @param value string to encode
     * @returns {*} html-encoded copy of the provided string (e.g. htmleEncode('you & me') == "&amp;")
     */
    var _htmlEncode = function (value) {
        if (value == undefined || value == '') {
            return "";
        }
        return $('<div/>').text(value).html();
        // older vesrion
        //      if (typeof value === "undefined" || str === '') return "";
        //            return $('<div></div>').text(str).html();
    }

    /**
     * Return string that has been html-encoded twice
     * @param value string to encode
     * @returns {*} double-encoded copy of string (e.g. htmlDoubleEncode('&') == "you &amp;amp; me")
     * @private
     */
    var _htmlDoubleEncode = function (value) {
        return _htmlEncode(_htmlEncode(value));
    }

    /**
     * Based on specified window size, return a string label a responsive "profile" title.
     * @param width size(px) of a window
     * @returns {string} best-fit profile title for specified width
     * @private
     */
    var _determineResponsiveClass = function (width) {
        return width > 1200 ? 'responsive-large-desktop' : width > 979 ? 'responsive-desktop' : width > 767 ? 'responsive-tablet' : width > 500 ? 'responsive-phone' : width > 1 ? 'responsive-phone-portrait' : '';
    }

    function _elipsify(text, n, useWordBoundary) {
        /* from: http://stackoverflow.com/questions/1199352/smart-way-to-shorten-long-strings-with-javascript */
        var toLong = text.length > n, s_ = toLong ? text.substr(0, n - 1) : text;
        s_ = useWordBoundary && toLong ? s_.substr(0, s_.lastIndexOf(' ')) : s_;
        return  toLong ? s_ + '...' : s_;
    }

    /**
     * Click event handler used when user clicks on the "bookmark" icon beside a resource. If the resource is
     * "bookmarked" it is  tagged as a potential integration source on the "integrate" page.  This function shows the
     * correct state (clicking the button togges the state on/off)  and sends an ajax request to update
     * the bookmark status on the server-side
     * @returns {boolean}
     * @private
     */
    function _applyBookmarks() {
        var $this = $(this);
        var resourceId = $this.attr("resource-id");
        var state = $this.attr("bookmark-state");
        var $waitingElem = $("<img src='" + getURI('images/ui-anim_basic_16x16.gif') + "' class='waiting' />");
        $this.prepend($waitingElem);
        var $icon = $(".bookmark-icon", $this);
        $icon.hide();
        console.log(resourceId + ": " + state);
        var oldclass = "tdar-icon-" + state;
        var newtext = "un-bookmark";
        var newstate = "bookmarked";
        var action = "bookmarkAjax";
        var newUrl = "/resource/removeBookmark?resourceId=" + resourceId;

        if (state == 'bookmarked') {
            newtext = "bookmark";
            newstate = "bookmark";
            action = "removeBookmarkAjax";
            newUrl = "/resource/bookmark?resourceId=" + resourceId;
        }
        var newclass = "tdar-icon-" + newstate;

        $.getJSON(getBaseURI() + "resource/" + action + "?resourceId=" + resourceId, function (data) {
                    if (data.success) {
                        $(".bookmark-label", $this).text(newtext);
                        $icon.removeClass(oldclass).addClass(newclass).show();
                        $this.attr("bookmark-state", newstate);
                        $this.attr("href", newUrl);
                        $(".waiting", $this).remove();
                    }
                });

        return false;
    }

    /**
     *  apply watermark input tags in context with watermark attribute.   'context' can be any valid
     *  argument to jQuery(selector[, context])
     * @param context
     * @private
     */
    var _applyWatermarks = function (context) {
        if (!Modernizr.input.placeholder) {
            $("input[placeholder]", context).each(function () {
                //todo: see if its any faster to do direct call to attr, e.g. this.attributes["watermark"].value
                $(this).watermark($(this).attr("placeholder"));
            });
        }
    }

    /**
     * Show the access rights reminder if any files are marked as confidential or if
     * the resource is embargoed
     * @private
     */
    var _showAccessRightsLinkIfNeeded = function () {
        if ($(".fileProxyConfidential").filter(function (index) {
            return $(this).val() != "PUBLIC";
        }).length > 0) {
            $('#divConfidentialAccessReminder').removeClass("hidden");
        } else {
            $('#divConfidentialAccessReminder').addClass("hidden");
        }
    }

    /**
     * return a decoded string of  the specified html-encoded text
     * @param value html-encoded string
     * @returns {string} decoded version of argument
     */
    var _htmlDecode = function (value) {
        if (value == undefined || value == '') {
            return "";
        }
        return $('<div/>').html(value).text();
    }

    // http://stackoverflow.com/questions/1038746/equivalent-of-string-format-in-jquery
    /**
     * Simple string format function.
     * @param {string} format string. e.g.
     * @param {...string} replacements.
     * @returns {*} string containing replacements (if provided).  for example,
     *              sprintf("{0} {0} {0} your {1}, gently down the stream", "row", "boat");
     */
    var _sprintf = function () {
        var s = arguments[0];
        for (var i = 0; i < arguments.length - 1; i++) {
            var reg = new RegExp("\\{" + i + "\\}", "gm");
            s = s.replace(reg, arguments[i + 1]);
        }
        return s;
    }

    /**
     * After certain amount of time,  display a dialog indicating that the users session has expired, then direct
     * the window to the login page.
     */
    var _sessionTimeoutWarning = function () {
        // I RUN ONCE A MINUTE
        // sessionTimeout in seconds
        if (parseInt(TDAR.common.currentTime)) {
            TDAR.common.currentTime += 60;
            var remainingTime = TDAR.common.sessionTimeout - TDAR.common.currentTime;
            if (remainingTime % 300 == 0) {
                console.log("remaining time in session:" + remainingTime);
            }
            if (remainingTime == 300) {
                var dialog = $('<div id=timeoutDialog></div>').html("<B>Warning!</B><br/>Your session will timeout in 5 minutes, please save the document you're currently working on").dialog({
                            modal: true,
                            title: "Session Timeout Warning",
                            closeText: "Ok",
                            buttons: {
                                "Ok": function () {
                                    $(this).dialog("close");
                                }
                            }
                        });
            }
            if ($("#timeoutDialog").length != 0 && remainingTime <= 0) {
                $("#timeoutDialog").html("<B>WARNING!</B><BR>Your Session has timed out, any pending changes will not be saved");
            } else {
                setTimeout(TDAR.common.sessionTimeoutWarning, 60000);
            }
        }
    }

    /**
     * specific initialization for the edit page for "document" resources
     */
    var _setupDocumentEditForm = function () {
        $(".doctype input[type=radio]").click(function () {
            _switchDocType(this);
        });
        _switchDocType($(".doctype input[type=radio]:checked"));
    }

    /**
     * For use with document edit page.  display relevant fieldsets corresponding to the current "document type"
     * @param radio
     * @param container
     */
    var _switchType = function (radio, container) {
        var type = $(radio).val().toLowerCase();

        console.debug('switchType:start:' + type);
        var $container = $(container);
        $(".typeToggle", $container).hide();
        $($("." + type), $container).show();

    }

    //FIXME: can switchType and switchDocType be refactored? at very least they need better names
    /**
     * Similar to switchType, but this (i think)) swaps out labels and descriptions for inputs that are re-used by
     * multiple document types.
     *
     * @param el doctype select element
     * @private
     */
    var _switchDocType = function (el) {
        var doctype = $(el).val().toLowerCase();

        console.debug('switchType:start:' + doctype);
        var $citeInfo = $("#citationInformation");
        $(".doctypeToggle", $citeInfo).hide();
        $($("." + doctype), $citeInfo).show();

        _switchLabel($("#publisher-hints"), doctype);
        _switchLabel($("#publisherLocation-hints"), doctype);

    }

    /**
     * specific setup for initializing "supporting resoure" edit forms.
     * @param totalNumberOfFiles total number of flies that can be associated with the resource
     * @param rtype resource type name
     * @private
     */
    var _setupSupportingResourceForm = function (totalNumberOfFiles, rtype) {
        // the ontology textarea or file upload field is required whenever it is
        // visible AND
        // no ontology rules are already present from a previous upload

        $('#fileInputTextArea').rules("add", {
                    required: {
                        depends: isFieldRequired
                    },
                    messages: {
                        required: "No " + rtype + " data entered. Please enter " + rtype + " manually or upload a file."
                    }
                });

        $('#fileUploadField').rules("add", {
                    required: {
                        depends: isFieldRequired
                    },
                    messages: {
                        required: "No " + rtype + " file selected. Please select a file or enter " + rtype + " data manually."
                    }
                });

        function isFieldRequired(elem) {
            var noRulesExist = !((totalNumberOfFiles > 0) || ($("#fileInputTextArea").val().length > 0) || ($("#fileUploadField").val().length > 0));
            return noRulesExist && $(elem).is(":visible");
        }

        _refreshInputDisplay();
    }

    /**
     * Wrapper for triggering custom Google Analytics events.
     *
     * @returns {boolean}
     * @param {...string} values to include in event. "_trackEvent" is implied -- do not include it in arguments.
     * @private
     */
    var _gaevent = function () {
        if (!_gaq || arguments.length < 1) {
            return true;
        }
        var args = Array.prototype.slice.call(arguments, 0);
        args.unshift('_trackEvent');
        var errcount = _gaq.push(args);
        if (errcount) {
            console.warn("_trackEvent failed:%s", args[1]);
        }
        return true;
    }

    /**
     * emit "file downloaded" google analytics event
     * @param url
     * @param tdarId
     * @private
     */
    var _registerDownload = function (url, tdarId) {
        if (tdarId) {
            _gaevent("Download", url, tdarId);
        } else {
            _gaevent("Download", url);
        }
    }

    /**
     * emit "outbound link clicked" event.
     * @param elem
     * @private
     */
    var _outboundLink = function (elem) {
        _gaevent("outboundLink", elem.href, window.location);
    }

    /**
     * for use in edit-column-metadata:  event handler for subcategoroy change event.
     * @param categoryIdSelect
     * @param subCategoryIdSelect
     * @private
     */
    var _changeSubcategory = function (categoryIdSelect, subCategoryIdSelect) {
        var $categoryIdSelect = $(categoryIdSelect);
        var $subCategoryIdSelect = $(subCategoryIdSelect);
        $categoryIdSelect.siblings(".waitingSpinner").show();
        $.get(getBaseURI() + "resource/ajax/column-metadata-subcategories", {
            "categoryVariableId": $categoryIdSelect.val()
        }, function (data_, textStatus) {
            var data = jQuery.parseJSON(data_);

            var result = "";
            for (var i = 0; i < data.length; i++) {
                result += "<option value=\"" + data[i]['value'] + "\">" + data[i]['label'] + "</option>\n";
            }

            $categoryIdSelect.siblings(".waitingSpinner").hide();
            $subCategoryIdSelect.html(result);
        });
    }

    /**
     * Used by ontology and coding-sheet edit pages; show relevant fields based on users choice of "manual text entry"
     * or "file upload"
     */
    var _refreshInputDisplay = function () {
        var selectedInputMethod = $('#inputMethodId').val();
        var showUploadDiv = (selectedInputMethod == 'file');
        $('#uploadFileDiv').toggle(showUploadDiv);
        $('#textInputDiv').toggle(!showUploadDiv);
    }

    /**
     * document edit page: expand those nodes where children are selected
     * @param field
     * @param type
     * @private
     */
    function _switchLabel(field, type) {
        var label = "#" + $(field).attr('id') + '-label';
        if ($(field).attr(type) != undefined && $(label) != undefined) {
            $(label).text($(field).attr(type));
        }
    }

    //FIXME: this doesn't need to be it's own function.
    /**
     * event handler that toggles manual coordinate entry
     * @param elem
     * @private
     */
    function _coordinatesCheckboxClicked(elem) {
        $('#explicitCoordinatesDiv').toggle(elem.checked);
    }

    /**
     * Render list of user's collections as a treeview.
     * @private
     */
    var _collectionTreeview = function () {
        $(".collection-treeview").find(".hidden").removeClass("hidden").end().treeview();
    }

    /**
     * return string that describes size of specified bytes in easier syntax
     * @param bytes  size in bytes
     * @param si true if description should be in SI units (e.g. kilobyte, megabyte) vs. IEC (e.g. kibibyte, mebibyte)
     * @returns {string} size as human readable equivalent of specified bytecount
     */
    function humanFileSize(bytes, si) {
        var thresh = si ? 1000 : 1024;
        if (bytes < thresh) {
            return bytes + ' B';
        }
        var units = si ? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'] : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
        var u = -1;
        do {
            bytes /= thresh;
            ++u;
        } while (bytes >= thresh);
        return bytes.toFixed(1) + ' ' + units[u];
    };

    $.extend(self, {
        "initEditPage": _initEditPage,
        "initFormValidation": _setupFormValidate,
        "applyTreeviews": _applyTreeviews,
        "initializeView": _initializeView,
        "initRegformValidation": _initRegformValidation,
        "determineResponsiveClass": _determineResponsiveClass,
        "elipsify": _elipsify,
        "populateTarget": _populateTarget,
        "prepareDateFields": _prepareDateFields,
        "setAdhocTarget": _setAdhocTarget,
        "changeSubcategory": _changeSubcategory,
        "registerDownload": _registerDownload,
        "gaevent": _gaevent,
        "outboundLink": _outboundLink,
        "setupSupportingResourceForm": _setupSupportingResourceForm,
        "switchType": _switchType,
        "setupDocumentEditForm": _setupDocumentEditForm,
        "sessionTimeoutWarning": _sessionTimeoutWarning,
        "applyBookmarks": _applyBookmarks,
        "sprintf": _sprintf,
        "htmlDecode": _htmlDecode,
        "htmlEncode": _htmlEncode,
        "htmlDoubleEncode": _htmlDoubleEncode,
        "applyWatermarks": _applyWatermarks,
        "coordinatesCheckboxClicked": _coordinatesCheckboxClicked,
        "refreshInputDisplay": _refreshInputDisplay,
        "maxJavascriptValidationMessages": 25,

        //I don't like how  Javascript Templates from "(tmpl.min.js)" puts "tmpl" in global scope, so I'm aliasing it here.
        "tmpl": tmpl,

        "collectionTreeview": _collectionTreeview,
        "humanFileSize": humanFileSize
    });

    return self;
}();

function checkWindowSize() {
    var width = $(window).width()
    var new_class = TDAR.common.determineResponsiveClass(width);
    $(document.body).removeClass('responsive-large-desktop responsive-desktop responsive-tablet responsive-phone responsive-phone-portrait').addClass(new_class);
}

/*
 * assigns a class to the body tag based on the current width.  These sizes match the bootstrap responsive grid sizes
 */
$(document).ready(function () {
    checkWindowSize();
    $(window).resize(checkWindowSize);
    if ($.cookie("hide_jira_button")) {
        setTimeout(function () {
            $('#atlwdg-trigger').hide()
        }, 700);
    }

    TDAR.common.sessionTimeoutWarning();

    $(document).delegate(".bookmark-link", "click", TDAR.common.applyBookmarks);

});
