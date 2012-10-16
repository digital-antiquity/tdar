/*
 * $Id$
 * 
 * Common JS functions used in tDAR (with dependency on JQuery).  
 * Mostly have to do with adding new rows for multi-valued fields, etc.
 */

//Define a dummy console for browsers that don't support logging
if (!window.console)
    console = {};
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
//        console.log("comparArray: " + arrayA + " vs. " + arrayB);
        if (arrayA.length != arrayB.length) {
            return false;
        }
        // ignore order by default
        if (typeof (ignoreOrder) == 'undefined')
            ignoreOrder = true;
        var a = arrayA, b = arrayB;
        if (ignoreOrder) {
//            console.log("comparArray: ignoring order");
            a = jQuery.extend(true, [], arrayA);
            b = jQuery.extend(true, [], arrayB);
            a.sort();
            b.sort();
        }
        for ( var i = 0, l = a.length; i < l; i++) {
//            console.log("comparArray: comparing " + a[i] + " and " + b[i]);
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

function repeatRow(tableId, rowAddedCallback, resetRights) {
    // FIXME: this business of optionally enabling the cloned row is screwing up
    // existing stuff. remove this option and break it out into a separate
    // function.
    var _resetRights = true;
    if (typeof resetRights != 'undefined') {
        _resetRights = resetRights;
    }

    var rowReference = $('#' + tableId + " > tbody > tr:last");
    var clonedRow = rowReference.clone();

    // we assume that the table row will have an ID that follows the convention
    // _num_, and we
    // will use this same convention for choosing the next ID addribute for the
    // row as well as
    // any element inside the row that uses the same convention for the NAME and
    // ID attributes.
    var rex = /_(\d+)_/i;
    var match = rex.exec(rowReference.attr("id"));
    var currentId = parseInt(match[1]); // the last occurance _num_ is our
    // current id

    var nextId = currentId + 1;
    var newRowId = nextId;
    if (rowReference.attr("id") != undefined
            && rowReference.attr("id").indexOf("_") != -1) {
        while ("a" != "b") {
            newRowId = rowReference.attr("id").substring(0,
                    rowReference.attr("id").lastIndexOf('_' + currentId + '_'))
                    + "_" + nextId + '_';
            if ($(newRowId).length == 0)
                break;
        }
    }
    // remove an error container if it exists (added by multi-column validation
    if (clonedRow.children("td:last").html().indexOf("errorContainer") != -1) {
        clonedRow.children("td:last").remove();
    }

    // update the id for our new row
    clonedRow.attr('id', newRowId);

    /*
     * Now that we've cloned the row, certain element attributes may need to be
     * renamed (for example, input tags with name attributes of the form
     * "fieldval[0]" should be renamed "fieldval[1]". Our assumption is that
     * every ID or NAME attribute that contains either "_num_" or "[num]" will
     * renamed.
     * 
     * However, we do not modify any tags that that have the css
     * class"repeatRowSkip".
     */
    // console.debug("about to find each elment in clonedrow:" + currentId);
    clonedRow.find('*').each(
            function() {
                var elem = this;
                // skip any tags that with the repeatRowSkip attribute
                if (!$(elem).hasClass('repeatRowSkip')) {
                    $(
                            [ "id", "autoVal", "name", "autocompleteIdElement",
                                    "autocompleteParentElement" ]).each(
                            function(i, attrName) {
                                // replace occurances of [num]
                                replaceAttribute(elem, attrName, '['
                                        + currentId + ']', '[' + nextId + ']');

                                // replace occurances of _num_
                                replaceAttribute(elem, attrName, '_'
                                        + currentId + '_', '_' + nextId + '_');
                            });
                }
            });

    rowReference.after(clonedRow);
    // FIXME: uniformly name forms tdarForm to add tdarForm context?
    // clear all input fields from the cloned row (except buttons)
    clearRow('#' + newRowId, _resetRights);
    // set focus on the first input field.

    repeatRowPostCloneActions(clonedRow);

    if (rowAddedCallback) {
        var clonedRowId = clonedRow.attr("id");
//        console.log("row added, calling callback with:" + clonedRowId);
        rowAddedCallback(clonedRow.attr("id"));
    }

    if ($('#' + tableId).attr('callback') != undefined) {
        console.log("about to execute callback");
        try {
            eval($('#' + tableId).attr('callback') + "(" + clonedRow.attr("id")
                    + ")");
        } catch (e) {
            console.log(e);
        }
    }

    $("input[type=text], textarea",clonedRow).filter(":visible:first").focus();
    return false;
}

function repeatRowPostCloneActions(clonedRow) {

	if(!Modernizr.input.placeholder){
	    $.each(clonedRow.find("[placeholder]"), function(k, v) {
    		$(this).watermark($(this).attr("placeholder"));
	    });
	}

}

function initializeRepeatRow() {
    $("table.repeatLastRow").each(
            function(index) {
                var msg = "add another";
                if ($(this).attr('addAnother') != undefined)
                    msg = $(this).attr('addAnother');
                var extraClass = "";
                if ($(this).hasClass("tableFormat"))
                    extraClass = "normalTop";
                $(this).after(
                        "<button type=button  class='addAnother " + extraClass
                                + "' onClick=\"repeatRow(\'" + this.id
                                + "\')\"><img src='/images/add.gif'>" + msg
                                + "</button>");
            });
}

// FIXME: this business of optionally enabling the cloned row is screwing up
// existing stuff. remove this option and break it out into a separate function.
function clearRow(rowId, resetRights) {
    if (typeof resetRights == 'undefined') {
        resetRights = true;
    }
    try {
        if (typeof global_formNavigate != 'undefined') {
            global_formNavigate = false;
        }
    } catch (e) {
    }
    // NOTE: we do not renumber IDs afterwards if they delete from the middle.
    // Not bad, but should be accounted for in controller
    $("input[type!=button],textarea", rowId).not(
            'input[type=checkbox],input[type=radio]').each(function() {
        $(this).val("");
        if (resetRights) {
            $(this).attr("readonly", false);
            $(this).attr("disabled", false);
        }
    });

    $("button,input[type=button],select").each(function() {
        if (resetRights) {
            $(this).attr("readonly", false);
            $(this).attr("disabled", false);
        }
    });
    // uncheck any checkboxes/radios
    $("input[type=checkbox],input[type=radio]", rowId).prop("checked", false);

    $("select", rowId).val($("select option:first", rowId).val());
    var parent = $(rowId).parents("table")[0];
    if ($(parent).attr('callback') != undefined) {
        try {
            // FIXME: push this callback call up to deleteRow. Otherwise
            // callbacks get called twice each time user adds a row.
            eval($(parent).attr('callback') + "('" + rowId + "')");
        } catch (e) {
            console.log(e);
        }
    }

}

function navigateTempIgnore() {
    global_formNavigate = true;
    setTimeout(function() {
        global_formNavigate = false;
    }, 2000);
}

function deleteRow(rowId) {
    try {
        if (typeof global_formNavigate != 'undefined') {
            global_formNavigate = false;
        }
    } catch (e) {
    }
    if ($(rowId).parent().children().size() > 1) {
        $(rowId).remove();
    } else {
        clearRow(rowId);
    }
    return false;
}

// delete the nearest parent TR of the provided element
function deleteParentRow(elem) {
    if ($(elem).parents("tr").length > 0) {
        var id = $(elem).parents("tr").first().attr('id');
        console.debug("deleteParentRow id:" + id);
        deleteRow('#' + id);
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

function setToolTipContents(targetElem) {
    $targetElem = $(targetElem);
    var fieldOff = $targetElem.offset();
    var noteOff = $('#notice').offset();
    $('#notice').offset({
        left : noteOff.left,
        top : fieldOff.top
    });

    // tooltip content can either be in 'tooltipcontent' attribute or in a
    // separate div
    var label = "";
    var content = "";
    if ($targetElem.attr('tooltipcontent')) {
        content = $targetElem.attr('tooltipcontent');
        // tooltip label can either be in atttribute, otherwise will be set to
        // the first h2
        label = $targetElem.attr('tiplabel') || "";
        if (label) {
            label = "<h2>" + label + "</h2>";
        }
        if (content[0] == "#") {
            content = $(content).html();
        }
    } else {
        console
                .error("unable to bind tooltip - no tooltip element or tooltipcontent found");
    }
    $('#notice').html(label + "<div id='noticecontent'>" + content + "</div>");
}

// expand those nodes where children are selected
function switchLabel(field, type) {
    var label = "#" + $(field).attr('id') + '-label';
    if ($(field).attr(type) != undefined && $(label) != undefined) {
        $(label).text($(field).attr(type));
    }
}


$.validator.addMethod("formatUS", function(value, element) {
    return this.optional(element) || value.match(/^(0[1-9]|1[012]|[1-9])\/(0[1-9]|[12][0-9]|3[01]|[1-9])\/(19|20)\d\d$/);
}, "The date format mm/dd/yyyy is expected");

$.validator.addMethod("latLong", function(value, element) {
    return value.match(/^(((\-?)(\d+)(\.?)(\d*))|)$/);
}, "a valid lat/long in the format DEG.Min/Sec (eg. -67.892068) required");

$.validator.addMethod("reasonableDate",
        function(value, element) {
            var intVal = parseInt(value);
            // allow -1 for internal management of things that don't have dates
            return (intVal == value && (intVal == -1 || intVal > 1000
                    && intVal < 3000));
        }, "a date in the last millenia is expected");

$.validator
        .addMethod(
                "isbn",
                function(value, element) {
                    if ($(element).is(':hidden'))
                        return true; // skip validation if not showing
                    return value
                            .match(/^(((\d+)-?(\d+)-?(\d+)-?([\dX]))|((978|979)-?(\d{9}[\dXx]))|)$/);
                }, "you must include a valid 10/13 Digit ISBN");

$.validator.addMethod("issn", function(value, element) {
    if ($(element).is(':hidden'))
        return true;// skip validation if not showing
    return value.match(/^((\d{4})-?(\d{3})(\d|X|x)|)$/);
}, "you must include a valid 8 Digit ISSN");

$.validator
        .addMethod(
                "descriptiveTitle",
                function(value, element) {
                    return !value
                            .match(/^(\s*)(dataset|collection|project|document|image|coding sheet|ontology)(\s*)$/i);
                }, "please select a more descriptive title");

$.validator.addMethod("float", function(value, element) {
    return value.match(/^(((\-?)(\d+)(\.?)(\d*))|)$/);
}, "a valid lat/long in the format DEG.Min/Sec (eg. -67.892068) required");

$.validator
        .addMethod(
                "validIdRequired",
                function(value, element) {
                    if (parseInt(value) != undefined && parseInt(value) > 0) {
                        return true;
                    } else if (evaluateAutocompleteRowAsEmpty(element, 0)) {
                        return true;
                    }
                    return false;
                },
                function(value, element) {
                    var msg = "";
                    $("input[type=text]:visible",
                            $($(element).attr("autocompleteParentElement")))
                            .each(
                                    function() {
                                        if ($(this).val() != '') {
                                            msg += " "
                                                    + $(this).attr("placeholder")
                                                    + ":" + $(this).val();
                                        }
                                    });
                    msg += "  is not a valid, registered user.  If you do not wish to add or specify a user, leave all fields in this section blank.";
                    return msg;
                });

// http://stackoverflow.com/questions/1260984/jquery-validate-less-than
$.validator.addMethod('lessThanEqual', function(value, element, param) {
    if (this.optional(element))
        return true;
    var i = parseInt(value);
    var j = parseInt($(param).val());
    return i <= j;
}, "This value must be less than the maximum value");

$.validator.addMethod('greaterThanEqual', function(value, element, param) {
    if (this.optional(element))
        return true;
    var i = parseInt(value);
    var j = parseInt($(param).val());
    return i >= j;
}, "This value must be greater than the minimum value");

$.validator.addMethod('asyncFilesRequired', function(value, elem) {
    return $('tr', '#files').not('.noFiles').size() > 0;
}, "At least one file upload is required.");

// $.validator's built-in number rule does not accept leading decimal points
// (e.g.'.12' vs. '0.12'), so we replace with our own
$.validator.addMethod('number', function(value, element) {
    return this.optional(element)
            || /^-?(?:\d+|\d{1,3}(?:,\d{3})+)?(?:\.\d+)?$/.test(value);
}, $.validator.messages.number);

// $.validator.addClassRules("radiocarbonDate", {range:[0,100000]});
// $.validator.addClassRules("julianYear", {range:[-99900, 2100]});

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
            lessThanEqual : endElem,
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
            greaterThanEqual : endElem,
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
    case "none":
        break;
    }
}

/*
 * AJAX BOOKMARKING FUNCTIONS
 */

function bookmarkResource(resourceId, link) {
    $waitingElem = $("<img src='" + getURI('images/ui-anim_basic_16x16.gif')
            + "' />");
    var $link = $(link);
    $link.replaceWith($waitingElem);
    var newtext = undefined;
    if ($link.children("span.bookmark:first").length != 0) {
        newtext = "un-bookmark";
    }
    $.getJSON(getBaseURI() + "resource/bookmarkAjax?resourceId=" + resourceId,
            function(data) {
                if (data.success) {
                    updateBookmarkTag($waitingElem,
                            "resource/removeBookmark?resourceId=" + resourceId,
                            "removeBookmark(" + resourceId + ", this)",
                            "images/bookmark.gif", newtext);
                }
            });
}

function removeBookmark(resourceId, link) {
    $waitingElem = $("<img src='" + getURI('images/ui-anim_basic_16x16.gif')
            + "' />");
    var $link = $(link);
    var newtext = undefined;
    if ($link.children("span.bookmark:first").length != 0) {
        newtext = "bookmark";
    }
    $link.replaceWith($waitingElem);
    $.getJSON(getBaseURI() + "resource/removeBookmarkAjax?resourceId="
            + resourceId, function(data) {
        if (data.success) {
            updateBookmarkTag($waitingElem, "resource/bookmark?resourceId="
                    + resourceId, "bookmarkResource(" + resourceId + ", this)",
                    "images/unbookmark.gif", newtext);
        }

    });
}

function updateBookmarkTag($elem, url, strOnclick, imgSrc, txt) {
	var css = "";
    if (txt != undefined & txt != '') {
    	css = txt;
        txt = "<span class='bookmark'>&nbsp;" + txt + "</span>";
    } else {
        txt = "";
    }
    
    var img = "<img src='" + getURI(imgSrc) + "' />";
    var parent = $elem.parent().get(0);

    if (parent.tagName.toLowerCase() == 'li') {
    	img = "";
    	var $parent = $(parent);
    	$parent.removeClass();
    	$parent.addClass(css);
    }
    var newElem = $("<a href='" + getURI(url) + "' onclick='" + strOnclick
            + "; return false;'>" + img + txt
            + "</a>");
    $elem.replaceWith(newElem);
}

function formSubmitDisable(optionalMsg) {
    var waitmsg = optionalMsg;
    var $button = $('#submitButton');
    if (!waitmsg)
        waitmsg = "Please wait...";
    if ($button.data('oldVal') == undefined) {
        $button.data('oldVal', $('#submitButton').val());
    }
    $button.val(waitmsg);
    $button.attr('disabled', 'disabled');
}

// enable the save button and replace it's former label (e.g. from 'please wait'
// to 'save')
function formSubmitEnable() {
    var $button = $('#submitButton');
    var oldVal = $button.data('oldVal');
    // it's likely formSubmitDisable was called at least once before now, but
    // don't assume
    if (oldVal) {
        $button.val($button.data('oldVal'));
    }
    $button.removeAttr('disabled');
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
 * Google Maps Support
 */

// update the 'public' latlong controls based on the values of the invisible
// latlong text fields
function populateLatLongTextFields() {
    $("#d_minx").val(Geo.toLon($("#minx").val()));
    $("#d_miny").val(Geo.toLat($("#miny").val()));
    $("#d_maxx").val(Geo.toLon($("#maxx").val()));
    $("#d_maxy").val(Geo.toLat($("#maxy").val()));
}

/**
 * Sensory Data Support
 */
// TODO: handle edit scenario
// TODO: handle new row scenario
// display the proper fields that correspond to the current value of the
// supplend scanner technology dropdown element.
function showScannerTechFields(elemScannerTech) {
    // get the parent element of
    $(elemScannerTech).siblings('.scantech-fields-tof').hide();
    $(elemScannerTech).siblings('.scantech-fields-phase').hide();
    $(elemScannerTech).siblings('.scantech-fields-tri').hide();

    // determine which div to show based on teh value of the scanner tech
    var divmap = {
        'TIME_OF_FLIGHT' : '.scantech-fields-tof',
        'PHASE_BASED' : '.scantech-fields-phase',
        'TRIANGULATION' : '.scantech-fields-tri'
    };

    if ($(elemScannerTech).val()) {
        $(elemScannerTech).siblings(divmap[$(elemScannerTech).val()]).show();

        $(elemScannerTech).parent().find('.scantech-fields-tof');
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

function disableMap() {
    $('#large-google-map').addClass('opaque');
    $('#gzoom-control').hide();
    $('#mapResetButton').hide();
}

function enableMap() {
    $('#large-google-map').removeClass('opaque');
    $('#gzoom-control').show();
    $('#mapResetButton').show();
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

// update the project json variable and update the inherited sections
function projectChangedCallback(data) {
    project = data;
    // if user picked blank option, then clear the sections
    if (!project.id) {
        project = getBlankProject();
    }

    if (project.resourceType == 'INDEPENDENT_RESOURCES_PROJECT') {
        project = getBlankProject();
    }

    json = convertToFormJson(project);
    updateInheritableSections(json);
}

/**
 * Testing Support
 */
// http://stackoverflow.com/questions/1038746/equivalent-of-string-format-in-jquery
function sprintf() {
    var s = arguments[0];
    for ( var i = 0; i < arguments.length - 1; i++) {
        var reg = new RegExp("\\{" + i + "\\}", "gm");
        s = s.replace(reg, arguments[i + 1]);
    }
    return s;
}

function initializeView() {
    console.debug('initialize view');
    if($('#large-google-map').length) {
        var mapdiv = $('#large-google-map')[0];
        TDAR.maps.initMapApi();
        TDAR.maps.setupMap(mapdiv, mapdiv);
        
    }
    
    initializeTooltipContent();
}

function initializeTooltipContent() {
    if (typeof formId != "undefined") {
        console.debug('delegating tooltips');
        $(formId).delegate("[tooltipcontent]", "mouseenter", function() {
            setToolTipContents(this);
        });
        $(formId).delegate("[tooltipcontent]", "focusin", function() {
            setToolTipContents(this);
        });
    }
}

function applyZebraColors(optionalRoot) {
    var root = document;
    if (optionalRoot)
        root = optionalRoot;

    $('table.zebracolors tbody tr:even', root).addClass("even");
    $('table.zebracolors tbody tr:odd', root).addClass("odd");
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

function getBrowserMajorVersion() {
    var browserMajorVersion = 1;
    try {
        browserMajorVersion = parseInt($.browser.version);
    } catch (e) {
    }
    return browserMajorVersion;
}

function setupDocumentEditForm() {
    // wire up document-type selection to dependent fields
    $("#showmorecite").hide('slow');
    $("#show-more-types").hide();
    $("#link-more").click(function() {
        $('#showmorecite').show('show');
        $('#showCite').hide();
        return false;
    });
    $('#documentTypeBOOK').click(function() {
        switchType("book");
    });
    $('#documentTypeBOOK_SECTION').click(function() {
        switchType("book_section");
    });
    $('#documentTypeJOURNAL_ARTICLE').click(function() {
        switchType("journal_article");
    });
    $('#documentTypeTHESIS').click(function() {
        switchType("thesis");
    });
    $('#documentTypeCONFERENCE_PRESENTATION').click(function() {
        switchType("conference");
    });
    $('#documentTypeOTHER').click(function() {
        switchType("other");
    });
    switchType($("input[@name='document.documentType']:radio:checked").val());
}


function switchType(doctype) {
    try {
        doctype = doctype.toLowerCase();
    } catch (ex) {
    }
    console.debug('switchType:start:' + doctype);

    $("#t-title2-journal").hide();
    $("#t-title2-book").hide();
    $("#t-series").hide();
    $("#t-jtitle").hide();
    $("#t-isbn").hide();
    $("#t-issn").hide();
    $("#t-pub").hide();
    $("#t-vol").hide();
    $("#t-start-end").hide();
    $("#t-institution").hide();
    $("#t-degree").hide();

    if (doctype == 'book_section') {
        $("#t-title2-book").show();
    }
    if (doctype == 'journal_article') {
        $("#t-title2-journal").show();
    }
    switchLabel($("#publisher-hints"), doctype);
    switchLabel($("#publisherLocation-hints"), doctype);

    if (doctype == 'book_section') {
        $("#t-title2").show();
        $("#t-isbn").show();
        $("#t-start-end").show();
    }
    if (doctype == 'book_section' || doctype == 'book'
            || doctype == 'book_section') {
        $("#t-series").show();
        $("#t-isbn").show();
        $("#t-pub").show();
    }

    if (doctype == 'book_section' || doctype == 'book_section') {
        $("#t-start-end").show();
    }

    if (doctype == 'journal_article') {
        $("#t-title2").show();
        $("#t-issn").show();
        $("#t-vol").show();
        $("#t-start-end").show();
    }

    if (doctype == 'thesis') {
        $("#t-pub").show();
        $("#t-degree").show();
    }

    if (doctype == 'conference') {
        $("#t-pub").show();
    }

    if (doctype == 'other') {
        $("#t-series").show();
        $("#t-isbn").show();
        $("#t-pub").show();
        $("#t-vol").show();
        $("#t-start-end").show();
    }

    if (!$('#title2').is(':hidden')) {
        $('#title2').addClass("required");
    } else {
        $('#title2').removeClass("required");
    }

    // console.debug('switchType:end:' + doctype);

}

function switchLabel(field, type) {
    // console.debug('switchLabel('+field+','+type+')');
    var label = "#" + $(field).attr('id') + '-label';
    if ($(field).attr(type) != undefined && $(label) != undefined) {
        $(label).text($(field).attr(type));
    }
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
    $categoryIdSelect.siblings(".waitingSpinner").css('visibility', 'visible');
    $.get(getBaseURI() + "resource/ajax/column-metadata-subcategories", {
        "categoryVariableId" : $categoryIdSelect.val()
    }, function(data_, textStatus) {
        var data = jQuery.parseJSON(data_);

        var result = "";
        for ( var i = 0; i < data.length; i++) {
            result += "<option value=\"" + data[i]['value'] + "\">"
                    + data[i]['label'] + "</option>\n";
        }

        $categoryIdSelect.siblings(".waitingSpinner").css('visibility',
                'hidden');
        $subCategoryIdSelect.html(result);
    });
}

function setAdhocTarget(elem) {
    console.log(elem);
    adhocTarget = $(elem).closest("div");
    return false;
}

var adhocTarget = null;

function showTooltip(x, y, contents) {
    $('<div id="flottooltip">' + contents + '</div>').css({
        position : 'absolute',
        display : 'none',
        top : y + 5,
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
    }
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

    for (i = 0; i < rowList.length; i++) {
        $("#" + rowList[i]["id"]).appendTo("#files");
    }
}

//populate a coding sheet / ontology field from an adhoc add-resource child page. 
//for now, let's assume there's never more than one adhoc child in play...
function populateTarget(obj) {
    if(typeof(adhocTarget) == 'undefined') return;

    console.log("populateTarget called.   adHocTarget:%s", adhocTarget);
    $('input[type=hidden]', adhocTarget).val(obj.id);
    $('input[type=text]', adhocTarget).val(obj.title);
    adhocTarget = null;
}


function toggleCopyrightHolder() {

    $("#copyrightHolderTable input[type!='radio']").val("");

    $("#copyrightPerson").toggle();
    $("#copyrightInstitution").toggle();
    $("#copyright_holder_institution_name").toggleClass("required");
    $("#copyright_holder_person_first_name").toggleClass("required");
    $("#copyright_holder_person_last_name").toggleClass("required");
}

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
         $(form)
                 .validate(
                         {
                             errorLabelContainer : $("#error ul"),
                             wrapper: "li",
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
                             //invalidHandler : $.watermark.showAll,
                             showErrors : function(errorMap, errorList) {
                                 this.defaultShowErrors();
                                 //spawn a modal widget and copy the errorLabelContainer contents (a separate div) into the widget's body section
                                 //TODO: docs say this is only called when errorList is not empty - can we remove this check?
                                 if (typeof errorList !== "undefined" && errorList.length > 0) {
                                     $('#validationErrorModal .modal-body p').empty().append($('#error').html());
                                     $('#validationErrorModal').modal();
                                     
                                 }
                             },
                             submitHandler : function(f) {
                                 var $button = $('input[type=submit]', f);
                                 $button.siblings(".waitingSpinner").css(
                                         'visibility', 'visible');
                                 // prevent multiple form submits (e.g. from
                                 // double-clicking the submit button)
                                 $button.attr('disabled', 'disabled');
                                 f.submit();
                             },
                             
                             highlight: function(element, errorClass, validClass) {
                                 $(element).closest("div.control-group").addClass("error");
                             },
                             unhighlight:function(element, errorClass, validClass) {
                                 $(element).closest("div.control-group").removeClass("error");
                             }
                         });
    
         $(form).delegate(
                 "input.error",
                 "change blur",
                 function() {
                     if ($("div.errorDialog:visible") == undefined
                             || $("div.errorDialog:visible").length == 0) {
                         $(this).valid();
                         console.log('revalidating...');
                     }
                 });
    
     }
    
    
    

    //setup other form edit controls
    //FIXME: wny is this broken out from  initEditPage?   If anything, break it out even further w/ smaller private functions
    var _setupEditForm = function (form) {
        
        //warn user about leaving before saving
        //FIXME: FormNavigate.js has bugs and is not being maintained. need to find/write replacement.
        //$(form).FormNavigate("Leaving the page will cause any unsaved data to be lost!");

        //initialize form validation
        _setupFormValidate(form);
        
        //prepwork prior to form submit (trimming fields)
        $(form).submit(function(f) {
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
            $button.siblings(".waitingSpinner").css('visibility', 'visible');

            return true;
        });


        $('.coverageTypeSelect', "#coverageTable").each(function(i, elem) {
            prepareDateFields(elem);
        });

        if ($(form.id + '_uploadedFiles').length > 0) {
            var validateUploadedFiles = function() {
                if ($(form.id + "_uploadedFiles").val().length > 0) {
                    $("#reminder").hide();
                }
            };
            $(form.id + '_uploadedFiles').change(validateUploadedFiles);
            validateUploadedFiles();
        }

        Modernizr.addTest('cssresize', Modernizr.testAllProps('resize'));
        
        if (!Modernizr.cssresize) {
            $('textarea.resizable:not(.processed)').TextAreaResizer();
        }

        $("#coverageTable").delegate(".coverageTypeSelect", "change", function() {
            console.log('called delegate');
            prepareDateFields(this);
        });
        showAccessRightsLinkIfNeeded();
        $('.fileProxyConfidential').change(showAccessRightsLinkIfNeeded);

    }
    
    
    var _applyTreeviews = function() {
        console.debug("applying tdar-treeviews v3")
        var $treeviews = $(".tdar-treeview");
        // Hack: a bug in Treeview plugin causes 'expand/collapse' icon to not show
        // for the last LI if it contains a sublist. So we arbitrarily
        // add an invisible LI to the end of each treeview to sidestep the bug.
        
        //$treeviews.append('<li style="display:none !important">&nbsp</li>');

        $treeviews.treeview({
                collapsed : true
        });

        // expand ancestors if any children are selected
        $treeviews.find("input:checked").parents(".hitarea").trigger("click");
       
    };

    
    
    
    //public: initialize the edit page form
    var _initEditPage = function(form) {
        
        //Multi-submit prevention disables submit button, so it will be disabled if we get here via back button. So we explicitly enable it. 
        var $button = $("#submitButton");
        $button.removeAttr('disabled').removeClass("waitingSpinner");
        
        $("#fakeSubmitButton").click(function() {$("#submitButton").click();});
        //FIXME: we should figure out if a) $.ready is not always called for bfcached pages, and b)if not, where to hook into 'pageshow' event.
        //I'm 99% sure the following code in this pageshow handler will never execute because caller runs in a $.ready handler.
        $(window).bind("pageshow", function() {
            $button.removeAttr('disabled').removeClass("waitingSpinner");
        });
        
        //init map
//        loadTdarMap();

        //init repeatrows
        TDAR.repeatrow.registerRepeatable(".repeatLastRow");
        
        //init person/institution buttons
        $("table.creatorProxyTable").delegate('.creator-toggle-button', "click",function(event){
        	var $this = $(this);
        	var $top = $this.parents("tr").first();
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
        delegateKeyword("#siteNameKeywordTable", "sitename", "SiteNameKeyword");
        delegateKeyword("#uncontrolledSiteTypeKeywordTable", "siteType", "SiteTypeKeyword");
        delegateKeyword("#uncontrolledCultureKeywordTable", "culture", "CultureKeyword");
        delegateKeyword("#temporalKeywordTable", "temporal", "TemporalKeyword");
        delegateKeyword("#otherKeywordTable", "other", "OtherKeyword");
        delegateKeyword("#geographicKeywordTable", "geographic", "GeographicKeyword");
        applyInstitutionAutocomplete($('#txtResourceProviderInstitution'), true);
        applyInstitutionAutocomplete($('#publisher'), true);
        $('#resourceCollectionTable').on(
                ".collectionAutoComplete",
                "focusin",
                function() {
                    applyCollectionAutocomplete($(".collectionAutoComplete", $('#resourceCollectionTable')), {showCreate:true});
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
        
        //ahad: toggle person/institution for copyright holder
        $("#copyright_holder_type_person").change(toggleCopyrightHolder);
        $("#copyright_holder_type_institution").change(toggleCopyrightHolder);
    
        $('#subnavbar').scrollspy();
        
        applyWatermarks(form);
        
        //FIXME: other init stuff that is separate function for some reason 
        _setupEditForm(form);

        _applyTreeviews();
        
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
    
    return {
        "initEditPage":_initEditPage,
        "initFormValidation": _setupFormValidate,
        "applyTreeviews": _applyTreeviews,
        "initializeView": _initializeView
    };
}();