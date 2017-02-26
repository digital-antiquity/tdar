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
 * @param useWordBoundary should we ellipsify in the middle of a word?
 * @returns {*} copy of string no longer than maxlength.
 */
TDAR.ellipsify = function _ellipsify(text, n, useWordBoundary) {
        /* from: http://stackoverflow.com/questions/1199352/smart-way-to-shorten-long-strings-with-javascript */
        var toLong = text.length > n, s_ = toLong ? text.substr(0, n - 1) : text;
        s_ = useWordBoundary && toLong ? s_.substr(0, s_.lastIndexOf(' ')) : s_;
        return  toLong ? s_ + '...' : s_;
    }


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
        var a = arrayA.concat(), b = arrayB.concat();
        if (ignoreOrder) {
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

TDAR.common = function (TDAR, fileupload) {
    "use strict";

    var self = {};


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
     * Suppress the browser's default behavior of submitting the current form when user presses RETURN while a text-input has focus.  User can still submit
     * via keypress when focussed on a submit button.
     *
     * @param $form jQuery selection containing the form that will suppress keypress submissions.
     */
    var _suppressKeypressFormSubmissions = function($form) {
        /* phantomjs does not suppport KeyEvents */
        /* istanbul ignore next */
        $form.find('input,select').keypress(function (event) {
            if(event.keyCode === $.ui.keyCode.ENTER) {
                event.preventDefault();
                return false;
            }
        });
    };

    /**
     * Perform initialization and setup for a typical elements and functionality of a tdar "edit page".  This does not
     * include initialization tasks for specific edit pages with unique functionality.
     *
     * @param form the form to initialize
     * @private
     */
    var _initEditPage = function (form, props) {

        if (props == undefined) {
            props = {};
        }
        //FIXME: other init stuff that is separate function for some reason 
        var $form = $(form);
        //fun fact: because we have a form field named "ID",  form.id actually refers to this DOM element,  not the ID attribute of the form.
        var formid = $form.attr("id");
        

        //information needed re: existing file uploads - needed by TDAR.upload library

        if (props.multipleUpload) {
            //init fileupload
            var id = $('input[name=id]').val();
            if (props.ableToUpload && props.multipleUpload) {
                TDAR.fileupload.registerUpload({
                    informationResourceId: id,
                    acceptFileTypes: props.acceptFileTypes,
                    formSelector: props.formSelector,
                    inputSelector: '#fileAsyncUpload',
                    fileuploadSelector: '#divFileUpload'
                });

            }
        }


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
        TDAR.autocomplete.delegateKeyword("#uncontrolledMaterialKeywordsRepeatable", "material", "MaterialKeyword");
        TDAR.autocomplete.delegateKeyword("#temporalKeywordsRepeatable", "temporal", "TemporalKeyword");
        TDAR.autocomplete.delegateKeyword("#otherKeywordsRepeatable", "other", "OtherKeyword");
        TDAR.autocomplete.delegateKeyword("#geographicKeywordsRepeatable", "geographic", "GeographicKeyword");
        TDAR.autocomplete.applyInstitutionAutocomplete($('#txtResourceProviderInstitution'), true);
        TDAR.autocomplete.applyInstitutionAutocomplete($('#publisher'), true);
        $('#resourceCollectionTable').on("focus", ".collectionAutoComplete", function () {
                    TDAR.autocomplete.applyCollectionAutocomplete($(this), {showCreate: true, showCreatePhrase: "Create a new collection"}, {permission: "ADMINISTER_GROUP"});
                });

        // prevent "enter" from submitting
        _suppressKeypressFormSubmissions($form);

        //init sortables
        //FIXME: sortables currently broken 
        $(".alphasort").click(_sortFilesAlphabetically);

        //ahad: toggle license
        $(".licenseRadio", $("#license_section")).change(_toggleLicense);

        //Refresh any scrollspies whenever document height changes.
        $('[data-spy="scroll"]').each(function () {
            var $scrollspy = $(this);

            $(document).bind("repeatrowadded repeatrowdeleted heightchange", function () {
                //console.debug("resizing scrollspy");
                $scrollspy.scrollspy("refresh");
            });
        });

        TDAR.contexthelp.initializeTooltipContent(form);
        _applyWatermarks(form);

        // prevent "enter" from submitting
        $form.on('keypress', 'input,select', function (event) {
            var elem = this;
            if(event.keyCode != 13) {return true;}

            //don't ignore ENTER key in a leaflet geocoder control
            return $(elem).closest('.leaflet-control-geocoder').length > 0;
        });

        //prepwork prior to form submit (trimming fields)
        $form.submit(function (f) {
            try {
                $.each($('.date, .number, .trim, .keywordAutocomplete'), function (idx, elem) {
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



        Modernizr.addTest('cssresize', Modernizr.testAllProps('resize'));

        //http://caniuse.com/#feat=css-resize
        if (!Modernizr.cssresize) {
            $('textarea.resizable:not(.processed)').TextAreaResizer();
        }

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

        _applyTreeviews();

        //show project preview button when appropriate
        $('#projectId').change(function () {
            var $select = $(this);
            var $row = $select.closest('.controls-row');
            $('.view-project', $row).remove();
            if ($select.val().length > 0 && $select.val() !== "-1") {
                var href = TDAR.uri('project/' + $select.val());
                var $button = '<a class="view-project btn btn-small" target="_project" href="' + href + '">View project in new window</a>';
                $row.append($button);
            }
        }).change();

        //Display status messages during ajax requests.
        _registerAjaxStatusContainer();


        if (props.includeInheritance) {
            TDAR.inheritance.applyInheritance(props.formSelector);
        }


        $("#fileUploadField").each(function(){
            var $fileUploadField = $(this);
            var _updateReminderVisibility = function() {
                if ($fileUploadField.val().length) {
                    $("#reminder").hide();
                }
            };
            $fileUploadField.change(_updateReminderVisibility);
            _updateReminderVisibility();
        });

        TDAR.inheritance.registerClearSectionButtons(form);

        // I should be "last", to avoid accidentally marking the form dirty before the user has done anything.
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
        //console.debug('initialize view');
        var $divSearchContext = $("#divSearchContext");

        if ($divSearchContext.length === 1) {
            $(".searchbox").focus(function () {
                $divSearchContext.addClass("active");
            }).blur(function () {
                //$divSearchContext.removeClass("active");
            });
        }
    };

    /**
     * Custom  ajax filter (enable by calling $.ajaxPrefilter(_customAjaxPrefilter). JQuery executes this prefilter
     * prior to any ajax call.
     *
     * @param options  options for the current request (including jquery defaults)
     * @param originalOptions options passed to $.ajax()  by the caller, without defaults.
     * @param $xhr  jquery xmlHttpRequest object
     * @private
     */
    var _statusContainerAjaxPrefilter = function(options, originalOptions, $xhr) {
        var hdlTimeout = 0;
        var $container, $message, $label;
        var defaults = {
            enabled: true,                      // Show status messages for this request
            selector:       '#ajaxIndicator',
            fadeInDelay:    'fast',
            fadeOutDelay:   1 * 1000,
            timeout:        20 * 1000,          // Hide message after specified timeout -  does not cancel the ajax request (0 for no timeout)
            label:          "Loading",
            waitMessage:    "...",
            doneMessage:    "...complete",
            failMessage:    "...failed",
            timeoutMessage: "request timed out"
        };

        var settings = $.extend({}, defaults, options);
        if(settings.enabled) {
            $container = $(settings.selector);
            $label = $container.find("strong");
            $message = $container.find("span");
            $label.text(settings.label);

            //Initial message
            $message.text(settings.waitMessage);
            $container.fadeIn(settings.fadeInDelay);

            //success message
            $xhr.done(function(){
                $message.text(settings.doneMessage);
            });

            //error message
            $xhr.fail(function() {
                $message.text(settings.failMessage);
            });

            //Fade out after success/failure
            $xhr.always(function(){
                $container.fadeOut(settings.fadeOutDelay);
                clearTimeout(hdlTimeout);
            });

            //Fade out after timeout, if specified.
            if(settings.timeout) {
                hdlTimeout = setTimeout(function () {
                    $container.fadeOut(settings.fadeOutDelay);
                }, settings.timeout);
            }
        }
    };

    /**
     * Register event listener that displays generic wait message for ajax requests. If the ajaxOptions property
     * of the event contain a "waitmessage" property, display that messages, otherwise the function displays "Loading"
     * while the request is in flight, and "Done" after the request is complete.
     */
    var _registerAjaxStatusContainer = function () {
        $.ajaxPrefilter(_statusContainerAjaxPrefilter);
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
     * @returns {*} double-encoded copy of string (e.g. htmlDoubleEncode('you & me') == "you &amp;amp; me")
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
        var $doc =  $(document);
        var sessionTimeout = $doc.data("sessionTimeout");
        var currentTime = $doc.data("currentTime");
        if (parseInt(currentTime)) {
            currentTime += 60;
            $doc.data("currentTime",currentTime);
            var remainingTime = sessionTimeout - currentTime;
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

    //FIXME: refactor/dedupe switchType (TDAR-3989)
    /**
     * Toggle the display of certain elements based on the value of a specified radio button.
     *
     * This function shows .typeToggle elements that also have css class which matches the radio's value, and hides all other .typeToggle elements.
     *
     * @param radio element/selector
     * @param container element/selector. Context for .typeToggle search.
     */
    var _switchType = function (radio, container) {
        var val = $(radio).val();
        var type = (typeof val !== 'undefined') ? val.toLowerCase() : "SWITCHTYPEDEFAULT";
        type = "." + type;

        //console.debug('switchType:start:' + type);
        var $container = $(container);
        $container.find(".typeToggle").hide();
        $container.find(type).show();
    }

    //FIXME: can switchType and switchDocType be refactored? at very least they need better names (TDAR-3989)
    /**
     * Similar to switchType, but this (i think)) swaps out labels and descriptions for inputs that are re-used by
     * multiple document types.
     *
     * @param el doctype select element
     * @private
     */
    var _switchDocType = function (el) {
        var doctype = $(el).val().toLowerCase();

        //console.debug('switchType:start:' + doctype);
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

        _refreshInputDisplay();
    }

    /**
     * emit "file downloaded" google analytics event
     * @param url
     * @param tdarId
     * @private
     */
    var _registerDownload = function (url, tdarId) {
        if (tdarId) {
            ga("send" , "event", "download", "download",  url, tdarId);
        } else {
            ga("send", "event", "download", "download", url);
        }
    };

    /**
     * emit "file downloaded" google analytics event
     * @param url
     * @param tdarId
     * @private
     */
    var _registerShare = function (service, url, tdarId) {
        if (tdarId) {
            ga("send", "event", service, "shared", url, tdarId);
        } else {
            ga("send", "event", service, "shared", url);
        }
    };

    /**
     * emit "outbound link clicked" event.
     * @param elem
     * @private
     */
    var _outboundLink = function (elem) {
        ga("send", "event", "outboundLink", "clicked", elem.href);
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
        $subCategoryIdSelect.empty();
        $categoryIdSelect.siblings(".waitingSpinner").show();
        $.get(TDAR.uri() + "api/resource/column-metadata-subcategories", {
            "categoryVariableId": $categoryIdSelect.val()
        }, function (data, textStatus) {
            var result = "";
            for (var i = 0; i < data.length; i++) {
                if (parseInt(data[i]['id']) > -1) {
                    result += "<option value=\"" + data[i]['id'] + "\">" + data[i]['label'] + "</option>\n";
                }
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
        var $field =  $(field);
        var $fieldId = "#" + $(field).attr("id");
        var label =  $fieldId + '-label';
        var $label = $(label);
        var $labelByName = $("label",$fieldId);
        if (($label == undefined || $label.length == 0) && ($labelByName != undefined && $labelByName.length != 0)) {
            $label = $labelByName;
        }
        
        if ($field.attr(type) != undefined && $label != undefined) {
            $label.text($field.attr(type));
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
        $(".collection-treeview").find(".hidden").removeClass("hidden").end().treeview({collapsed:true});
    }

    /**
     * Format number w/ comma grouping. If num is fractional, display fractional to two places.
     * @param num
     * @returns {string}
     */
    function _formatNumber(num) {
        var numparts = Math.floor(num).toString().split('.');
        var r = num % 1;
        var str = numparts[0].split('').reverse().join('').replace(/(\d{3})\B/g, '$1,').split('').reverse().join('');
        str += numparts[1] ? '.'  + numparts[1] : '';

        if(r > 0) {
            str += '.' + r.toFixed(2).replace('0.', '');
        }


        return str;
    }


    /**
     * return string that describes size of specified bytes in easier syntax
     * @param bytes  size in bytes
     * @param si true if description should be in SI units (e.g. kilobyte, megabyte) vs. IEC (e.g. kibibyte, mebibyte)
     * @returns {string} size as human readable equivalent of specified bytecount
     */
    function _humanFileSize(bytes) {
        var thresh = 1000;
        if (bytes < thresh) {
            return bytes + ' B';
        }
        //jtd: IEC names would be less ambiguous, but JEDEC names are more consistent with what we show elsewhere on the site
        //var units = si ? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'] : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
        var units = ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
        var u = -1;
        do {
            bytes /= thresh;
            ++u;
        } while (bytes >= thresh);
        return bytes.toFixed(1) + ' ' + units[u];
    };


    /**
     * Initialize bootstrap galleries that have .image-carousel class.  Bootstrap Gallery typically 
     * doesn't require initialization, but we do extra stuff such as lazy-loading of thumbnails for 
     * big lists, and binding of analytics events.
     * @private
     */ 
    function _initImageGallery() {
        //init bootstrap image gallery (if found)
        $(".image-carousel").each(function(idx, divGallery) {
            //for big galleries, defer the loading of thumbnails that can't be seen yet
            $(divGallery).find(".thumbnailLink[data-src]").each(function(idx, elem){
                elem.src = $(elem).data("src");
            });
        })

        $(".thumbnailLink").click(function () {
            var $this = $(this);
            $("#bigImage").attr('src', $this.data('url'));
            var rights = "";
            if ($this.data("access-rights")) {
                rights = "This file is <em>" + $this.data("access-rights") + "</em> but you have rights to it";
            }
            $("#confidentialLabel").html(rights);
            $("#downloadText").html($this.attr('alt'));
            $(".thumbnail-border-selected").removeClass("thumbnail-border-selected");
            $this.parent().addClass("thumbnail-border-selected");
        });
    }



    $.extend(self, {
        "initEditPage": _initEditPage,
        "applyTreeviews": _applyTreeviews,
        "initializeView": _initializeView,
        "determineResponsiveClass": _determineResponsiveClass,
        "populateTarget": _populateTarget,
        "setAdhocTarget": _setAdhocTarget,
        "changeSubcategory": _changeSubcategory,
        "registerDownload": _registerDownload,
        "registerShare": _registerShare,
        "outboundLink": _outboundLink,
        "setupSupportingResourceForm": _setupSupportingResourceForm,
        "switchType": _switchType,
        "setupDocumentEditForm": _setupDocumentEditForm,
        "sessionTimeoutWarning": _sessionTimeoutWarning,
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
        "humanFileSize": _humanFileSize,
        "initImageGallery": _initImageGallery,
        "formatNumber": _formatNumber,
        "registerAjaxStatusContainer": _registerAjaxStatusContainer,
        "suppressKeypressFormSubmissions": _suppressKeypressFormSubmissions
    });

    return self;
}(TDAR, TDAR.fileupload);

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
    TDAR.common.sessionTimeoutWarning();
});
