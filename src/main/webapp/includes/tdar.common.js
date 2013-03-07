/*
 * $Id$
 * 
 * Common JS functions used in tDAR (with dependency on JQuery).  
 * Mostly have to do with adding new rows for multi-valued fields, etc.
 */

//Define a dummy console for browsers that don't support logging
if (!window.console) {
    console = {};
}
console.log = console.log || function() {
};
console.warn = console.warn || function() {
};
console.debug = console.debug || function() {
};
console.error = console.error || function() {
};
console.info = console.info || function() {
};
console.trace = function() {
};
// To quickly disable all console messages, uncomment the following line
// console.log = console.debug = console.warn = console.error = console.info =
// function() {};
// or simply turn off the mundane console messages
// console.debug = function(){};
//
if (!window.JSON)
    JSON = {};
JSON.stringify = JSON.stringify || function() {
};

TDAR.ellipsify = function(str, maxlength) {
    if (!str)
        return;
    var newString = str;
    if (str.length > maxlength - 3) {
        newString = str.substring(0, maxlength - 3) + "...";
    }
    return newString;
};

function getQSParameterByName(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.href);
    if (results == null)
        return "";
    else
        return decodeURIComponent(results[1].replace(/\+/g, " "));
}

// Compare two arrays. return true if A and B contain same elements (
// http://stackoverflow.com/questions/1773069/using-jquery-to-compare-two-arrays
jQuery.extend({
    compareArray : function(arrayA, arrayB, ignoreOrder) {
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
        for ( var i = 0, l = a.length; i < l; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
});

function getFunctionBody(func) {
    var m = func.toString().match(/\{([\s\S]*)\}/m)[1];
    return m;
}

// replace last occurance of str in attribute with rep
function replaceAttribute(elem, attrName, str, rep) {
    if (!$(elem).attr(attrName))
        return;
    var oldval = $(elem).attr(attrName);
    if (typeof oldval == "function") {
        oldval = getFunctionBody(oldval);
        // console.debug("converting function to string:" + oldval );

    }
    if (oldval.indexOf(str) != -1) {
        var beginPart = oldval.substring(0, oldval.lastIndexOf(str));
        var endPart = oldval.substring(oldval.lastIndexOf(str) + str.length,
                oldval.length);
        var newval = beginPart + rep + endPart;
        $(elem).attr(attrName, newval);
        // console.debug('attr:' + attrName + ' oldval:' + oldval + ' newval:' +
        // newval);
    }
}

function refreshInputDisplay() {
    var selectedInputMethod = $('#inputMethodId').val();
    var showUploadDiv = (selectedInputMethod == 'file');
    $('#uploadFileDiv').toggle(showUploadDiv);
    $('#textInputDiv').toggle(!showUploadDiv);
}

function personAdded(id) {
//    console.log("person added " + id);
    $(".creatorInstitution", "#" + id).hide();
    $(".creatorPerson", "#" + id).show();
}

function institutionAdded(id) {
//    console.log("institution added " + id);
    // hide the person record
    $(".creatorPerson", "#" + id).hide();
    $(".creatorInstitution", "#" + id).show();
}

// expand those nodes where children are selected
function switchLabel(field, type) {
    var label = "#" + $(field).attr('id') + '-label';
    if ($(field).attr(type) != undefined && $(label) != undefined) {
        $(label).text($(field).attr(type));
    }
}





// called whenever date type changes
//FIXME: I think we can improve lessThanEqual and greaterThenEqual so that they do not require parameters, and hence can be 
//       used via $.validator.addClassRules.  The benefit would be that we don't need to register these registration rules each time a date
//       gets added to the dom.
function prepareDateFields(selectElem) {
    var startElem = $(selectElem).siblings('.coverageStartYear');
    var endElem = $(selectElem).siblings('.coverageEndYear');
    $(startElem).rules("remove");
    $(endElem).rules("remove");
    switch ($(selectElem).val()) {
    case "CALENDAR_DATE":
        $(startElem).rules("add", {
            range : [ -99900, 2100 ],
            lessThanEqual : [endElem,"Calender Start", "Calendar End"],
            required : function() {
                return $(endElem).val() != "";
            }
        });
        $(endElem).rules("add", {
            range : [ -99900, 2100 ],
            required : function() {
                return $(startElem).val() != "";
            }
        });
        break;
    case "RADIOCARBON_DATE":
        $(startElem).rules("add", {
            range : [ 0, 100000 ],
            greaterThanEqual : [endElem, "Radiocarbon Start", "Radiocarbon End"],
            required : function() {
                return $(endElem).val() != "";
            }
        });
        $(endElem).rules("add", {
            range : [ 0, 100000 ],
            required : function() {
                return $(startElem).val() != "";
            }
        });
        break;
    case "NONE":
        $(startElem).rules("add", {
        	blankCoverageDate: {"start":startElem, "end":endElem}
        });
        break;
    }
}

/*
 * AJAX BOOKMARKING FUNCTIONS
 */

$(document).ready(function() {
	$(document).delegate(".bookmark-link","click",applyBookmarks);
});

function applyBookmarks() {
	var $this = $(this);
	var resourceId = $this.attr("resource-id");
	var state = $this.attr("bookmark-state");
	$waitingElem = $("<img src='" + getURI('images/ui-anim_basic_16x16.gif') + "' class='waiting' />");
	$this.prepend($waitingElem);
	var $icon = $(".bookmark-icon",$this);
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
	
    $.getJSON(getBaseURI() + "resource/"+action+"?resourceId=" + resourceId,
            function(data) {
                if (data.success) {
                	$(".bookmark-label",$this).text(newtext);
                	$icon.removeClass(oldclass).addClass(newclass).show();
                	$this.attr("bookmark-state",newstate);
                	$this.attr("href",newUrl);
                	$(".waiting",$this).remove();
                }
            });
	
	return false;
}

//apply watermark input tags in context with watermark attribute.  'context' can be any valid argument to jQuery(selector[, context])
function applyWatermarks(context) {
    if(!Modernizr.input.placeholder){
        $("input[placeholder]", context).each(function() {
            //todo: see if its any faster to do direct call to attr, e.g. this.attributes["watermark"].value
            $(this).watermark($(this).attr("placeholder"));
        });
    }
}


// show the access rights reminder if any files are marked as confidential or if
// the resource is embargoed
function showAccessRightsLinkIfNeeded() {
    if ($(".fileProxyConfidential").filter(function(index) {return $(this).val() != "PUBLIC"; }).length > 0) {
        $('#divConfidentialAccessReminder').removeClass("hidden");
    } else {
        $('#divConfidentialAccessReminder').addClass("hidden");
    }
}



/**
 * Sensory Data Support
 */
// TODO: handle edit scenario
// TODO: handle new row scenario
// display the proper fields that correspond to the current value of the
// supplend scanner technology dropdown element.
function showScannerTechFields(elemScannerTech) {
    // get the parent element of the scanner tech field
    console.debug("showing scanner tech fields for:");
    console.debug(elemScannerTech);
    // determine which div to show based on the value of the scanner tech
    var divmap = {
        'TIME_OF_FLIGHT' : '.scantech-fields-tof',
        'PHASE_BASED' : '.scantech-fields-phase',
        'TRIANGULATION' : '.scantech-fields-tri'
    };
    var parent = $(elemScannerTech).parents('.scantech-fields');
    parent.find('.scantech-field').addClass('hide');
    var scannerTechnologyValue = $(elemScannerTech).val();
    if (scannerTechnologyValue) {
        var targetClass = divmap[scannerTechnologyValue];
        console.log("showing all elements of class: " + targetClass);
        parent.find(targetClass).removeClass('hide');
//        $(elemScannerTech).siblings(targetClass).removeClass('hide');
        // $(elemScannerTech).parent().find('.scantech-fields-tof');
    }

}

function scanAdded(rowElem) {
    // get the select element
    var scannerTechElem = $('.scannerTechnology', rowElem);
    // the scanner type changed to blank, so we hide the scanner-tech-specific
    // fields, and bind to the select change
    showScannerTechFields(scannerTechElem);
    $(scannerTechElem).change(function() {
        var elem = this;
        showScannerTechFields(elem);
    });
}


function cancelSearchRequest($elem) {

}

function htmlEncode(value) {
    if (value == undefined || value == '')
        return "";
    return $('<div/>').text(value).html();
}

function htmlDecode(value) {
    if (value == undefined || value == '')
        return "";
    return $('<div/>').html(value).text();
}

// return skeleton project
function getBlankProject() {
    var skeleton = {
        "approvedCultureKeywords" : [],
        "approvedSiteTypeKeywords" : [],
        "cultureKeywords" : [],
        "dateCreated" : {},
        "description" : null,
        "firstLatitudeLongitudeBox" : null,
        "geographicKeywords" : [],
        "id" : null,
        "investigationTypes" : [],
        "materialKeywords" : [],
        "otherKeywords" : [],
        "resourceType" : null,
        "siteNameKeywords" : [],
        "siteTypeKeywords" : [],
        "submitter" : null,
        "temporalKeywords" : [],
        "coverageDates" : [],
        "title" : null,
        "resourceNotes" : [],
        "relatedComparativeCollections" : [],
        "resourceAnnotations" : [],
        "uncontrolledCultureKeywords" : [],
        "uncontrolledSiteTypeKeywords" : []
    };
    return skeleton;
}


// http://stackoverflow.com/questions/1038746/equivalent-of-string-format-in-jquery
function sprintf() {
    var s = arguments[0];
    for ( var i = 0; i < arguments.length - 1; i++) {
        var reg = new RegExp("\\{" + i + "\\}", "gm");
        s = s.replace(reg, arguments[i + 1]);
    }
    return s;
}

/**
 * Testing Support
 */

function initializeView() {
    console.debug('initialize view');
    var maps = $(".google-map, #large-google-map");
    if(maps.length) {
        TDAR.maps.initMapApi();
        maps.each(function() {
            TDAR.maps.setupMap(this, this);
        });
    }
}

function delegateCreator(id, user, showCreate) {
    if (user == undefined || user == false) {
        $(id).delegate(
                ".nameAutoComplete",
                "focusin",
                function() {
                    // TODO: these calls re-regester every row after a row is
                    // created,
                    // change so that only the new row is registered.
                    applyPersonAutoComplete($(".nameAutoComplete", id), false,
                            showCreate);
                });
        $(id).delegate(".institutionAutoComplete", "focusin", function() {
            applyInstitutionAutocomplete($(".institution", id), true);
        });
    } else {
        $(id).delegate(".userAutoComplete", "focusin", function() {
            applyPersonAutoComplete($(".userAutoComplete", id), true, false);
        });
    }
}

// fixme: instead of focusin, look into using a customEvent (e.g. 'rowCreated')
function delegateAnnotationKey(id, prefix, delim) {
    $(id).delegate("." + prefix + "AutoComplete", "focusin", function() {
        applyKeywordAutocomplete("." + prefix + "AutoComplete", delim, {}, false);
    });
}

function delegateKeyword(id, prefix, type) {
    $(id).delegate(".keywordAutocomplete", "focusin", function() {
        // TODO: these calls re-regester every row after a row is created,
        // change so that only the new row is registered.
        console.log('focusin:' + this.id);
        applyKeywordAutocomplete(id + " .keywordAutocomplete", "keyword", {
            keywordType : type
        }, true);
    });

}

function sessionTimeoutWarning() {
    // I RUN ONCE A MINUTE
    // sessionTimeout in seconds
    currentTime += 60;
    var remainingTime = sessionTimeout - currentTime;
    if (remainingTime == 300) {
        var dialog = $('<div id=timeoutDialog></div>')
                .html(
                        "<B>Warning!</B><br/>Your session will timeout in 5 minutes, please save the document you're currently working on")
                .dialog({
                    modal : true,
                    title : "Session Timeout Warning",
                    closeText : "Ok",
                    buttons : {
                        "Ok" : function() {
                            $(this).dialog("close");
                        }
                    }
                });
    }
    if ($("#timeoutDialog").length != 0 && remainingTime <= 0) {
        $("#timeoutDialog")
                .html(
                        "<B>WARNING!</B><BR>Your Session has timed out, any pending changes will not be saved");
    } else {
        setTimeout(sessionTimeoutWarning, 60000);
    }
}
/*
function getBrowserMajorVersion() {
    var browserMajorVersion = 1;
    try {
        browserMajorVersion = parseInt($.browser.version);
    } catch (e) {
    }
    return browserMajorVersion;
}
*/
function setupDocumentEditForm() {
    $(".doctype input[type=radio]").click(function() {switchDocType(this);});
    switchDocType($(".doctype input[type=radio]:checked"));
}


function switchType(radio, container) {
    var type = $(radio).val().toLowerCase();

    console.debug('switchType:start:' + type);
    var $container = $(container);
    $(".typeToggle",$container).hide();
    $($("." + type) ,$container).show();

}




function switchDocType(el) {
    var doctype = $(el).val().toLowerCase();

    console.debug('switchType:start:' + doctype);
    var $citeInfo = $("#citationInformation");
    $(".doctypeToggle",$citeInfo).hide();
    $($("." + doctype) ,$citeInfo).show();

    switchLabel($("#publisher-hints"), doctype);
    switchLabel($("#publisherLocation-hints"), doctype);

}

function switchLabel(field, type) {
    // console.debug('switchLabel('+field+','+type+')');
    $("label",field).text(field.attr(type));
}

function toggleDiv() {
    $(this).next().slideToggle('slow');
    $(this).find("span.ui-icon-triangle-1-e").switchClass(
            "ui-icon-triangle-1-e", "ui-icon-triangle-1-s", 700);
    $(this).find("span.ui-icon-triangle-1-s").switchClass(
            "ui-icon-triangle-1-s", "ui-icon-triangle-1-e", 700);
}

function setupSupportingResourceForm(totalNumberOfFiles, rtype) {
    // the ontology textarea or file upload field is required whenever it is
    // visible AND
    // no ontology rules are already present from a previous upload

    $('#fileInputTextArea').rules(
            "add",
            {
                required : {
                    depends : isFieldRequired
                },
                messages : {
                    required : "No " + rtype + " data entered. Please enter "
                            + rtype + " manually or upload a file."
                }
            });

    $('#fileUploadField').rules(
            "add",
            {
                required : {
                    depends : isFieldRequired
                },
                messages : {
                    required : "No " + rtype
                            + " file selected. Please select a file or enter "
                            + rtype + " data manually."
                }
            });

    function isFieldRequired(elem) {
        var noRulesExist = !((totalNumberOfFiles > 0)
                || ($("#fileInputTextArea").val().length > 0) || ($(
                "#fileUploadField").val().length > 0));
        return noRulesExist && $(elem).is(":visible");
    }

    refreshInputDisplay();
}

function makeMap(json, mapId, type, value_) {
    var mapString = "";

    if (!json.chartshape) {
        alert("No map elements");
        return;
    }
    mapString = "<map name='" + mapId + "'>";
    var area = false;
    var chart = json.chartshape;
    var values = value_.split("|");
    for ( var i = 0; i < chart.length; i++) {
        area = chart[i];
        mapString += "\n  <area name='" + area.name + "' shape='" + area.type
                + "' coords='" + area.coords.join(",");
        var val = values[i];

        // FIXME: I don't always consistently work
        // var offset = values.length - 1;
        // if (val == undefined && i >= offset && values[i-offset] != undefined)
        // {
        // val = values[(i-offset)];
        // }
        // console.log(values.length + ' ' + i + "{"+ (i -offset)+ "}" + ' ' +
        // values[(i-offset)]);
        if (val != undefined) {
            mapString += "' href='" + getURI("search/results") + "?" + type
                    + "=" + val + "&useSubmitterContext=true'";
        }
        mapString += " title='" + val + "'>";
        ;
    }
    mapString += "\n</map>";
    $("#" + mapId + "-img").after(mapString);
}

function registerDownload(url, tdarId) {
    if (typeof _gaq == 'undefined')
        return;
    var command = [ '_trackEvent', 'Download', url ];
    if (tdarId)
        command.push(tdarId);
    var errcount = _gaq.push(command);
    if (errcount) {
        console.warn("_trackEvent command failed for" + url);
    }
}

function changeSubcategory(categoryIdSelect, subCategoryIdSelect) {
    var $categoryIdSelect = $(categoryIdSelect);
    var $subCategoryIdSelect = $(subCategoryIdSelect);
    $categoryIdSelect.siblings(".waitingSpinner").show();
    $.get(getBaseURI() + "resource/ajax/column-metadata-subcategories", {
        "categoryVariableId" : $categoryIdSelect.val()
    }, function(data_, textStatus) {
        var data = jQuery.parseJSON(data_);

        var result = "";
        for ( var i = 0; i < data.length; i++) {
            result += "<option value=\"" + data[i]['value'] + "\">"
                    + data[i]['label'] + "</option>\n";
        }

        $categoryIdSelect.siblings(".waitingSpinner").hide();
        $subCategoryIdSelect.html(result);
    });
}

//indicate the root context  to use when populateTarget is called. 
function setAdhocTarget(elem, selector) {
    var _selector = selector;
    if (!_selector) selector = "div";
    var adhocTarget = $(elem).closest(_selector);
    $('body').data("adhocTarget", adhocTarget);
    //expose target for use by child window
    TDAR.common.adhocTarget = adhocTarget;
    //return false; 
}


function showTooltip(x, y, contents) {
    $('<div id="flottooltip">' + contents + '</div>').css({
        position : 'absolute',
        display : 'none',
        top : y + 30,
        left : x + 5
    }).appendTo("body").fadeIn(200);
}

var previousPoint = null;

function dynamicSort(property, caseSensitive) {
    return function(a, b) {
        if (caseSensitive == undefined || caseSensitive == false) {
            return (a[property].toLowerCase() < b[property].toLowerCase()) ? -1
                    : (a[property].toLowerCase() > b[property].toLowerCase()) ? 1
                            : 0;
        } else {
            return (a[property] < b[property]) ? -1
                    : (a[property] > b[property]) ? 1 : 0;
        }
    };
}

function sortFilesAlphabetically() {
    var rowList = new Array();
    var $table = $("#files tbody");
    $("tr", $table).each(function() {
        var row = {};
        row["id"] = $(this).attr("id");
        row["filename"] = $(".filename", $(this)).text();
        rowList[rowList.length] = row;
    });

    rowList.sort(dynamicSort("filename"));

    for (var i = 0; i < rowList.length; i++) {
        $("#" + rowList[i]["id"]).appendTo("#files");
    }
}

//populate a coding sheet / ontology field from an adhoc add-resource child page. 
//for now, let's assume there's never more than one adhoc child in play...
function populateTarget(obj) {
    var $body = $("body");
    var adhocTarget = $body.data("adhocTarget");
    if(typeof(adhocTarget) == 'undefined') return;

    console.log("populateTarget called.   adHocTarget:%s", adhocTarget);
    $('input[type=hidden]', adhocTarget).val(obj.id);
    $('input[type=text]', adhocTarget).val(obj.title);
    $body.removeData("adhocTarget");
    TDAR.common.adhocTarget = null;

}


//function toggleCopyrightHolder() {
//
//    $("#copyrightHolderTable input[type!='radio']").val("");
//
//    $("#copyrightPerson").toggle();
//    $("#copyrightInstitution").toggle();
//    $("#copyright_holder_institution_name").toggleClass("required");
//    $("#copyright_holder_person_first_name").toggleClass("required");
//    $("#copyright_holder_person_last_name").toggleClass("required");
//}

function toggleLicense() {

    // update display of licenses when the radio button selection changes
    $("#license_section input[type='radio']").each(
        function(index) {
            // show or hide the row depending on whether the corresponding radio button is checked
            var $this = $(this);
            var license_type_name = $this.val();
            var license_details_reference = "#license_details_" + license_type_name;
            var license_details = $(license_details_reference);
            var $licenseText = $('#licenseText');
            if ($this.is(":checked")) {
                license_details.show();
            } else {
                license_details.hide();
            }    
            if (!$licenseText.is(':hidden')) {
                $licenseText.addClass("required");
            } else {
                $licenseText.removeClass("required");
            }
        }
    );
}


/**
 * trying to move these functions out of global scope and apply strict parsing.
 */


TDAR.namespace("common");
TDAR.common = function() {
    "use strict";
    
    var self = {};
    
    var _defaultValidateOptions = {
        errorLabelContainer : $("#error ul"),
        wrapper: "li",
        highlight: function(element, errorClass, validClass) {
            $(element).addClass("error");
             $(element).closest("div.control-group").addClass("error");
         },
         unhighlight:function(element, errorClass, validClass) {
             $(element).removeClass("error");
             //highlight this div until all visible controls in group are valid
             var $controlGroup = $(element).closest("div.control-group");
             if($controlGroup.find('.error:visible').length === 0) {
                 $controlGroup.removeClass("error");
             }
         },
        showErrors: function(errorMap, errorList) {
            this.defaultShowErrors();
        }
                 
    };
    
    //TODO: remove redundant code -- this is very similar to repeatrow._clearInputs.
    var _clearInputs = function($parents) {
    	
    	//FIXME: can we just set all of these to disabled instead?
    	
        //clear any non-showing creator proxy fields so server knows the actualCreatorType for each
        console.log("clearing unused proxy fields");
        // most input elements should have value attribute cleared (but not radiobuttons, checkboxes, or buttons)
        $parents.find("input[type!=button],textarea").not('input[type=checkbox],input[type=radio]').val("");
        // uncheck any checkboxes/radios
        $parents.find("input[type=checkbox],input[type=radio]").prop("checked", false);
        // remove "selected" from options that were already selected
        $parents.find("option[selected=selected]").removeAttr("selected");
        // revert all select inputs to first option. 
        $parents.find("select").attr("disabled", "disabled");
    }

    
    
     // FIXME: the jquery validate documentation for onfocusout/onkeyup/onclick
     // doesn't jibe w/ what we see in practice. supposedly these take a boolean
     // argument specifying 'true' causes an error. since true is the default for
     // these three options I'm simply removing those lines from the validate
     // call
     // below.
     // see http://docs.jquery.com/Plugins/Validation/validate#options for
     // options and defaults
     // see http://whilefalse.net/2011/01/17/jquery-validation-onkeyup/ for
     // undocumented feature that lets you specify a function instead of a
     // boolean.
    var _setupFormValidate = function(form) {
        var options = {
            onkeyup : function() {
                return;
            },
            onclick : function() {
                return;
            },
            onfocusout : function(element) {
                return;
                // I WORK IN CHROME but FAIL in IE & FF
                // if (!dialogOpen) return;
                // if ( !this.checkable(element) && (element.name in
                // this.submitted ||
                // !this.optional(element)) ) {
                // this.element(element);
                // }
            },
            showErrors: function(errorMap, errorList) {
                this.defaultShowErrors();
                //spawn a modal widget and copy the errorLabelContainer contents (a separate div) into the widget's body section
                //TODO: docs say this is only called when errorList is not empty - can we remove this check?
                if (typeof errorList !== "undefined" && errorList.length > 0) {
                    $('#validationErrorModal .modal-body p').empty().append($("<ul></ul>").append($('#error ul').html()));
                    $('#validationErrorModal').modal();

                }
                $('#error').show();
            },
            submitHandler : function(f) {
                //prevent double submit and dazzle user with animated gif
                _submitButtonStartWait();
                _clearInputs($(f).find(".creatorPerson.hidden, .creatorInstitution.hidden")); 
                $('#error').hide();
                
                $(f).FormNavigate("clean");
                f.submit();
                
            }
        };
        
         var allValidateOptions = $.extend({}, _defaultValidateOptions, options);
         $(form).validate(allValidateOptions);

     };
     
    var _initRegformValidation = function(form) {
        var $form = $(form);
        var options = {
            errorLabelContainer:
                    $("#error"),
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
            }
        };
        $form.validate($.extend({},  _defaultValidateOptions, options));

    };
             
    //setup other form edit controls
    //FIXME: wny is this broken out from  initEditPage?   If anything, break it out even further w/ smaller private functions
    var _setupEditForm = function (form) {
        var $form = $(form);
        //fun fact: because we have a form field named "ID",  form.id actually refers to this DOM element,  not the ID attribute of the form.
        var formid = $form.attr("id");
        
        // prevent "enter" from submitting
        $form.delegate('input,select',"keypress", function(event) {
            return event.keyCode != 13;
        });

        //initialize form validation
        _setupFormValidate(form);
        
        //prepwork prior to form submit (trimming fields)
        $form.submit(function(f) {
            try {
                $.each($('.reasonableDate, .coverageStartYear, .coverageEndYear, .date, .number'), function(idx, elem) {
                    if ($(elem).val() !== undefined)  {
                        $(elem).val($.trim($(elem).val()));
                    }
                });
            } catch (err) {
                console.error("unable to trim:" + err);
            }

            var $button = $('input[type=submit]', f);
            $button.siblings(".waitingSpinner").show();

            //warn user about leaving before saving
            //FIXME: FormNavigate.js has bugs and is not being maintained. need to find/write replacement.
            $("#jserror").val("");
            return true;
        });


        $('.coverageTypeSelect', "#coverageDateRepeatable").each(function(i, elem) {
            prepareDateFields(elem);
        });

        var $uploaded = $(formid + '_uploadedFiles');
        if ($uploaded.length > 0) {
            var validateUploadedFiles = function() {
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

        $("#coverageDateRepeatable").delegate(".coverageTypeSelect", "change", function() {
            prepareDateFields(this);
        });
        showAccessRightsLinkIfNeeded();
        $('.fileProxyConfidential').change(showAccessRightsLinkIfNeeded);
        
        //FIXME: idea is nice, but default options produce more annoying UI than original browser treatment of 'title' attribute. also, bootstrap docs
        //       tell you how to delegate to selectors but I couldn't figure it out.
        //$(form).find('label[title]').tooltip();
        
        
        
        if ($('#explicitCoordinatesDiv').length > 0) {
            $('#explicitCoordinatesDiv').toggle($('#viewCoordinatesCheckbox')[0].checked);
        
        }
        $(".latLong").each(function(index, value){
            $(this).hide();
            //copy value of hidden original to the visible text input
            var id = $(this).attr('id'); 
            $('#d_' + id).val($('#' + id).val());
        });
        
        $("#jserror").val("SAVE");
        
        // delete/clear .repeat-row element and fire event
        $('#copyrightHolderTable').on("click", ".row-clear", function(e){
            var rowElem = $(this).parents(".repeat-row")[0];
            TDAR.repeatrow.deleteRow(rowElem);
        });
        
        
    };
    
    
    var _applyTreeviews = function() {
        //console.debug("applying tdar-treeviews v3");
        var $treeviews = $(".tdar-treeview");
        $treeviews.treeview({
                collapsed : true
        });
        // expand ancestors if any children are selected
        $treeviews.find("input:checked").parents(".hitarea").trigger("click");
    };
    
   var _submitButtonStartWait = function(){
       var $submitDivs = $('#editFormActions, #fakeSubmitDiv');
       var $buttons = $submitDivs.find(".submitButton");
       $buttons.prop("disabled", true);
       
       //fade in the wait icon
       $submitDivs.find(".waitingSpinner").show();
   };
   
   var _submitButtonStopWait = function() {
       var $submitDivs = $('#editFormActions, #fakeSubmitDiv');
       var $buttons = $submitDivs.find(".submitButton");
       $buttons.prop("disabled", false);
       
       //fade in the wait icon
       $("#possibleJsError").val("");
       $submitDivs.find(".waitingSpinner").hide();
   } 
    
    
    //public: initialize the edit page form
    var _initEditPage = function(form) {
        $("#possibleJsError").val("INIT");

       //Multi-submit prevention disables submit button, so it will be disabled if we get here via back button. So we explicitly enable it. 
        _submitButtonStopWait();
        
        $("#fakeSubmitButton").click(function() {$("#submitButton").click();});

        //init repeatrows
        TDAR.repeatrow.registerRepeatable(".repeatLastRow");
        
        //init person/institution buttons
        $(".creatorProxyTable").on("click", '.creator-toggle-button', function(event){
            var $this = $(this);
            var $top = $this.closest(".repeat-row");
            if ($top == undefined) {
            	$top = $this.closest(".control-group");
            }
            if ($(event.target).hasClass("personButton")) {
                $(".creatorPerson", $top).removeClass("hidden");
                $(".creatorInstitution",$top).removeClass("hidden").addClass("hidden");
            } else {
                $(".creatorPerson", $top).removeClass("hidden").addClass("hidden");
                $(".creatorInstitution",$top).removeClass("hidden");
            }
        });    
        

        //wire up autocompletes
        delegateCreator("#authorshipTable", false, true);
        delegateCreator("#creditTable", false, true);
        delegateCreator("#divAccessRights", true, false);
        delegateCreator("#copyrightHolderTable",false,true);
        delegateAnnotationKey("#resourceAnnotationsTable", "annotation", "annotationkey");
        delegateKeyword("#siteNameKeywordsRepeatable", "sitename", "SiteNameKeyword");
        delegateKeyword("#uncontrolledSiteTypeKeywordsRepeatable", "siteType", "SiteTypeKeyword");
        delegateKeyword("#uncontrolledCultureKeywordsRepeatable", "culture", "CultureKeyword");
        delegateKeyword("#temporalKeywordsRepeatable", "temporal", "TemporalKeyword");
        delegateKeyword("#otherKeywordsRepeatable", "other", "OtherKeyword");
        delegateKeyword("#geographicKeywordsRepeatable", "geographic", "GeographicKeyword");
        applyInstitutionAutocomplete($('#txtResourceProviderInstitution'), true);
        applyInstitutionAutocomplete($('#publisher'), true);
        $('#resourceCollectionTable').on(
                "focus",
                ".collectionAutoComplete",
                function() {
                    applyCollectionAutocomplete($(this), {showCreate:true});
                });

        // prevent "enter" from submitting
        $('input,select').keypress(function(event) {
            return event.keyCode != 13;
        });

        //init sortables
        //FIXME: sortables currently broken 
        $(".alphasort").click(sortFilesAlphabetically);
        
        //ahad: toggle license
        $(".licenseRadio",$("#license_section")).change(toggleLicense);
        
//        //ahad: toggle person/institution for copyright holder
//        $("#copyright_holder_type_person").change(toggleCopyrightHolder);
//        $("#copyright_holder_type_institution").change(toggleCopyrightHolder);
    
        //if page has a navbar,  wire it up and refresh it whenever something changes page size (e.g. repeatrow additions)
        
        //fixme: ths scrollspy is being registered twice (remove data-attributes from scrollspy div?)
        $('#subnavbar').each(function() {
            var $scrollspy = $(this).scrollspy();
            
            //monitor document height and fire event when it changes
            $.documentHeightEvents();
            
            $(document).bind("repeatrowadded repeatrowdeleted heightchange", function() {
                //console.log("resizing scrollspy");
                $scrollspy.scrollspy("refresh");
            });
            
        });
        
        
        TDAR.contexthelp.initializeTooltipContent(form);
        applyWatermarks(form);
        
        //FIXME: other init stuff that is separate function for some reason 
        _setupEditForm(form);

        _applyTreeviews();
        
        //show project preview button when appropriate
        $('#projectId').change(function() {
            var $select = $(this);
            var $row = $select.closest('.controls-row');
            $('.view-project', $row).remove();
            if($select.val().length > 0 && $select.val() !=="-1") {
                var href = getURI('project/' + $select.val());
                var $button = '<a class="view-project btn btn-small" target="_project" href="' + href + '">View project in new window</a>';
                $row.append($button);
            }
        }).change();
        
        
        //display generic wait message with ajax events
        _registerAjaxEvents();

        // I must be "last"
        $("#possibleJsError").val("SAVE");
        $(form).not('.disableFormNavigate').FormNavigate({
            message:"Leaving the page will cause any unsaved data to be lost!",
            customEvents: "repeatrowdeleted fileuploadstarted fileuploadfinished",
            cleanOnSubmit: false
        });

    };
    
    var _initializeView = function() {
        console.debug('initialize view');
        if($('#large-google-map').length) {
            var mapdiv = $('#large-google-map')[0];
            var inputContainer = $("#divCoordContainer")[0];
            TDAR.maps.initMapApi();
            TDAR.maps.setupMap(mapdiv, inputContainer);
        }
    };
    
    //display generic wait message for ajax requests
    var _registerAjaxEvents = function() {
        $('body').bind('ajaxSend', function(e, jqXHR, ajaxOptions){
            if(typeof ajaxOptions.waitMessage === "undefined") {
                ajaxOptions.waitMessage = "Loading";
            }
            $('#ajaxIndicator').html("<strong>Waiting</strong>: " + ajaxOptions.waitMessage + "...").fadeIn('fast');
            //TODO: include a timeout to dismiss loading or display warning mesage
        });
        $('body').bind('ajaxComplete', function(e, jqXHR, ajaxOptions) {
            $('#ajaxIndicator').html("<strong>Complete</strong>: " + ajaxOptions.waitMessage + "...").fadeOut(1000);
        });
        
    };
    
    var _index = function(obj, key){
        if(typeof obj === "undefined") return undefined;
        return obj[key];
    };
    
    //public: for a given object, return the value of the field specified using 'dot notation'
    // e.g.:  getObjValue(obj, "foo.bar.baz") will return obj[foo][bar][baz]
    
    var _getObjValue = function(obj, strFieldName) {
        //FIXME: add fallback impl. when  Array.prototype.reduce() not supported (IE8)
        //https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Array/Reduce#Compatibility
        return strFieldName.split(".").reduce(_index, obj);
    }
    
    //return html-encoded copy of provided string
    var _htmlEncode = function(str) {
            if (typeof value === "undefined" || str === '') return "";
            return $('<div></div>').text(str).html();
    }
    
    var _determineResponsiveClass = function(width) {
        return width > 1200 ? 'responsive-large-desktop' :
            width > 979 ? 'responsive-desktop' :
            width > 767 ? 'responsive-tablet' :
        	width > 500 ? 'responsive-phone' :
            width > 1 ? 'responsive-phone-portrait' : '';
    }
    
    //initialize image gallery using w/ width/height appropriate for responsive format
    var _initImageGallery = function($container, numImagesToDisplay, authenticatedUser) {
        var options = {
            fit_to_parent:      false,
            auto:         false,
            interval:       3000,
            continuous:       false,
            loading:        true,
            tooltip_width:      200,
            tooltip_icon_width:   32,
            tooltip_icon_height:  32,
            tooltip_offsetx:    18,
            tooltip_offsety:    0,
            buttons:        true,
            btn_numbers:      true,
            keybord_keys:     true,
            mousetrace:       false, /* Trace x and y coordinates for the mouse */
            pauseonover:      true,
            stoponclick:      true,
            transition:       'hslide', /* hslide/vslide/fade */
            transition_delay:   300,
            transition_speed:   500,
            show_caption:     'onhover', /* onload/onhover/show */
            thumbnails:       true,
            thumbnails_position:  'outside-last', /* outside-last/outside-first/inside-last/inside-first */
            thumbnails_direction: 'horizontal', /* vertical/horizontal */
            thumbnails_slidex:    1, /* 0 = auto / 1 = slide one thumbnail / 2 = slide two thumbnails / etc. */
            dynamic_height:     false, /* For dynamic height to work in webkit you need to set the width and height of images in the source. Usually works to only set the dimension of the first slide in the showcase. */
            speed_change:     false, /* Set to true to prevent users from swithing more then one slide at once. */
            viewline:       false  /* If set to true content_width, thumbnails, transition and dynamic_height will be disabled. As for dynamic height you need to set the width and height of images in the source. */
        };
        
        /*
         * Heights and widths of gallery should be roughly square as our derivative sizes are at maximum sqaures
         *     public static final int LARGE = 600;
    		   public static final int MEDIUM = 300;
    		   public static final int SMALL = 96;
         */
        var responsiveOptions = {
                'responsive-large-desktop': {
                    content_height: 600,
                    content_width: 600
                },
                'responsive-desktop':{
                    content_height: 600,
                    content_width: 600
                },
                'responsive-tablet':{
                    content_height: 500,
                    content_width: 500
                },
                'responsive-phone':{
                    content_height: 300,
                    content_width: 300
                },
                'responsive-phone-portrait':{
                    content_height: 300,
                    content_width: 300
                }

        };
        
        $.extend(options, responsiveOptions[_determineResponsiveClass($(window).width())]);
        
        if(!authenticatedUser) {
            options.content_height = 0;
            options.arrows = false;
        }
        
        //fixme: make content_width smaller if not enough thumbnails (need thumbnail width to determind... which option is that?)
        $container.awShowcase(options);
    }
    
    //hide the jira button for a week
    function _delayJiraButton() {
        //fixme: add function to hide jira button and store choice in cookie
        //id="" class="atlwdg-trigger atlwdg-TOP"
        $.cookie("hide_jira_button", true, { expires: 7});
        console.log("see you next week");
    
    }
    
    $.extend(self, {
        "initEditPage": _initEditPage,
        "initFormValidation": _setupFormValidate,
        "applyTreeviews": _applyTreeviews,
        "initializeView": _initializeView,
        "getObjValue": _getObjValue,
        "htmlEncode": _htmlEncode,
        "initRegformValidation": _initRegformValidation,
        "determineResponsiveClass": _determineResponsiveClass,
        "initImageGallery": _initImageGallery, 
        "delayJiraButton": _delayJiraButton
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
$(document).ready(function() {
    checkWindowSize();
    $(window).resize(checkWindowSize);
    if($.cookie("hide_jira_button")) {
        setTimeout(function(){$('#atlwdg-trigger').hide()}, 700);
    }
});


function elipsify(text, n, useWordBoundary){
    /* from: http://stackoverflow.com/questions/1199352/smart-way-to-shorten-long-strings-with-javascript */
    var toLong = text.length>n,
        s_ = toLong ? text.substr(0,n-1) : text;
    s_ = useWordBoundary && toLong ? s_.substr(0,s_.lastIndexOf(' ')) : s_;
    return  toLong ? s_ + '...' : s_;
}
