/*
 * $Id$
 * 
 * Common JS functions used in tDAR (with dependency on JQuery).  
 * Mostly have to do with adding new rows for multi-valued fields, etc.
 */      

//Define a dummy console for browsers that don't support logging
if (!window.console) console = {};
console.log = console.log || function(){};
console.warn = console.warn || function(){};
console.debug = console.debug || function(){};
console.error = console.error || function(){};
console.info = console.info || function(){};
console.trace = function(){};
// To quickly disable all console messages, uncomment the following line
// console.log = console.debug = console.warn = console.error = console.info =
// function() {};
// or simply turn off the mundane console messages
// console.debug = function(){};


var g_consoleLogRemoteCount = 0;
console.logRemote = console.logRemote || function(msg) {
    try {
        if(g_consoleLogRemoteCount++ < 10) { //let's not DDOS tDAR just because jim wrote an infinite loop.
            console.log("sending to server:" + msg);
            $.post(getBaseURI() + 'resource/ajax/log-debug', {logMessage: msg});
        } else {
            console.error("Too many remote log messages. not sending:" + msg);
        }
    }catch(ignored) {}
};

// add trim() support for browsers such as IE
var TDAR = {};
// FIXME: jquery does this already
TDAR.trim = function(str) {
    return str.replace(/^\s+|\s+$/g, '');
};

TDAR.ellipsify = function(str, maxlength) {
    var newString = str;
    if(str.length > maxlength-3) {
        newString = str.substring(0,maxlength-3) + "...";
    }
    return newString;
};

// Compare two arrays. return true if A and B contain same elements (
// http://stackoverflow.com/questions/1773069/using-jquery-to-compare-two-arrays
jQuery.extend({
    compareArray: function (arrayA, arrayB, ignoreOrder) {
        console.trace("comparArray: " + arrayA + " vs. " + arrayB);
        if (arrayA.length != arrayB.length) { return false; }
        // ignore order by default
        if(typeof(ignoreOrder) == 'undefined') ignoreOrder = true;
        var a = arrayA, b = arrayB;
        if(ignoreOrder) {
            console.trace("comparArray: ignoring order");
            a = jQuery.extend(true, [], arrayA);
            b = jQuery.extend(true, [], arrayB);
            a.sort(); 
            b.sort();
        }
        for (var i = 0, l = a.length; i < l; i++) {
            console.trace("comparArray: comparing " + a[i] + " and " + b[i]);
            if (a[i] != b[i]) { 
                return false;
            }
        }
        return true;
    }
});

//I'm assuming this function fails if function body has blocks? Probably not a big concern since event attributes are one-liners that rarely use them.
function getFunctionBody(func) {
    var m = func.toString().match(/\{([\s\S]*)\}/m)[1];
    return m;
}

// replace last occurance of str in attribute with rep
function replaceAttribute(elem, attrName, str, rep) {
    if(!$(elem).attr(attrName)) return;
    var oldval = $(elem).attr(attrName);
    if(typeof oldval == "function") {
        oldval = getFunctionBody(oldval);
        // console.debug("converting function to string:" + oldval );
        
    }
    if(oldval.indexOf(str) != -1){
        var beginPart = oldval.substring(0, oldval.lastIndexOf(str));
        var endPart = oldval.substring(oldval.lastIndexOf(str) + str.length, oldval.length);
        var newval = beginPart + rep + endPart;
        $(elem).attr(attrName, newval);
        // console.debug('attr:' + attrName + ' oldval:' + oldval + ' newval:' +
		// newval);
    }
}

function repeatRow(tableId,rowAddedCallback) {
    var rowReference = $('#' + tableId + " > tbody > tr:last");
    var clonedRow = rowReference.clone();

    // we assume that the table row will have an ID that follows the convention
	// _num_, and we
    // will use this same convention for choosing the next ID addribute for the
	// row as well as
    // any element inside the row that uses the same convention for the NAME and
	// ID attributes.
    var rex = /_(\d+)_/i;
    var match=rex.exec(rowReference.attr("id"));
    var currentId = parseInt(match[1]); // the last occurance _num_ is our
										// current id

    var nextId = currentId + 1; 
    var newRowId = nextId;
    if (rowReference.attr("id") != undefined && rowReference.attr("id").indexOf("_") != -1)	{
		while ("a" != "b") {
			newRowId = rowReference.attr("id").substring(0,rowReference.attr("id").lastIndexOf('_' + currentId + '_')) + "_" + nextId + '_'; 
			if ($(newRowId).length == 0) break;
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
    // TODO: ensure that this section works in IE6-- there may be issues with
	// changing ID attribute programatically
    clonedRow.find('*').each(function() {
        var elem = this;
        // skip any tags that with the repeatRowSkip attribute
        if(!$(elem).hasClass('repeatRowSkip')) {
            $(["id", "name"]).each(function(i,attrName){
                // replace occurances of [num]
                replaceAttribute(elem, attrName, '[' + currentId + ']', '[' + nextId + ']');
                
                // replace occurances of _num_
                replaceAttribute(elem, attrName, '_' + currentId + '_', '_' + nextId + '_');
            });
        }
    });
    
    rowReference.after(clonedRow);
    // FIXME: uniformly name forms tdarForm to add tdarForm context?
    // clear all input fields from the cloned row (except buttons)
    clearRow('#' + newRowId);
    // set focus on the first input field.
    $(":input:first", clonedRow).focus();

    repeatRowPostCloneActions(clonedRow);   
    
    if(rowAddedCallback) {
        var clonedRowId = clonedRow.attr("id");
        console.trace("row added, calling callback with:" + clonedRowId);
        rowAddedCallback(clonedRow.attr("id"));
    }
    
    if ($('#' + tableId).attr('callback') != undefined) {
        console.log("about to execute callback");
    	try {
    		eval($('#' + tableId).attr('callback') + "("+ clonedRow.attr("id") + ")");
    	} catch (e) {console.log(e)}
    }
    
    return false;
}

function repeatRowPostCloneActions(clonedRow) {
    // TODO: instead of explicitly adding these 'post-clone' actions, maybe add
	// an event listener model (somehow)
    // wire up autocomplete to any auto-complete-able fields in the new row
    $.each(clonedRow.find(".nameAutoComplete, .userAutoComplete"),
          function(k,v) {applyAutoComplete('#'+v.id);
      });
    
    $.each(clonedRow.find("[watermark]"), 
        function(k,v) { $(this).watermark($(this).attr("watermark"));
      });

    
    // wire up any institution auto-complete-able fields
    // console.debug('post-clone action for:' + clonedRow);
    $.each(clonedRow.find(".institution"),
            function(k,v) {
                applyInstitutionAutoComplete('#'+v.id);
                // console.debug('applying autocomplete to:' + v.id);
        });
    
    $.each(clonedRow.find(".annotationAutoComplete"),
            function(k,v) {
                applyKeywordAutocomplete('#'+v.id, 'annotationkey');
    });
    
    $.each(clonedRow.find(".sitenameAutoComplete"), function(k,v) { applyKeywordAutocomplete('#'+v.id, 'keyword', {keywordType: 'SiteNameKeyword'}); });
    $.each(clonedRow.find(".siteTypeKeywordAutocomplete"), function(k,v) { applyKeywordAutocomplete('#'+v.id, 'keyword', {keywordType: 'SiteTypeKeyword'}); });
    $.each(clonedRow.find(".cultureKeywordAutocomplete"), function(k,v) { applyKeywordAutocomplete('#'+v.id, 'keyword', {keywordType: 'CultureKeyword'}); });
    $.each(clonedRow.find(".temporalKeywordAutocomplete"), function(k,v) { applyKeywordAutocomplete('#'+v.id, 'keyword', {keywordType: 'TemporalKeyword'}); });
    $.each(clonedRow.find(".otherKeywordAutocomplete"), function(k,v) { applyKeywordAutocomplete('#'+v.id, 'keyword', {keywordType: 'OtherKeyword'}); });
    $.each(clonedRow.find(".geographicKeywordAutocomplete"), function(k,v) { applyKeywordAutocomplete('#'+v.id, 'keyword', {keywordType: 'GeographicKeyword'}); });

    
}

function initializeRepeatRow() {
	$(".repeatLastRow").each(function(index) {
		var msg = "add another";
		if ($(this).attr('addAnother') != undefined) msg = $(this).attr('addAnother');
		var extraClass= "";
		if ($(this).hasClass("tableFormat"))extraClass = "normalTop";
		$(this).after("<button type=button  class='addAnother "+extraClass+"' onClick=\"repeatRow(\'" + this.id+"\')\"><img src='/images/add.gif'>" + msg + "</button>");
	});
    // create sidebar tooltips for any elements that have tooltipcontent
	// attribute
	$('[tooltipcontent*=]').each(function(index) {
		$(this).bind('mouseenter',function() {setToolTipContents(this);});
		$(this).bind('focusin',function() {setToolTipContents(this);});
	});
    /*
	 * $('.accordion .head').click(function() { $(this).next().toggle('fast');
	 * return false; }).next().hide();
	 */

}

function clearRow(rowId) {
	try {
	      if (global_formNavigate != undefined) { global_formNavigate = false; }
		} catch(e){}
    // FIXME: do we need to renumber IDs afterwards if they delete from the
	// middle?
    $("input[type!=button],textarea", rowId).not('input[type=checkbox],input[type=radio]').val("");
    // uncheck any checkboxes/radios
    // FIXME: original fixme asked for radio:first to be selected, but I think a
	// safer bet is to have them all unselected (thoughts?)
    $("input[type=checkbox],input[type=radio]", rowId).prop("checked", false);
    
    $("select", rowId).val($("select option:first", rowId).val());
    var parent = $(rowId).parents("table")[0];
    if ($(parent).attr('callback') != undefined) {
    	try {
    	    // FIXME: push this callback call up to deleteRow. Otherwise
			// callbacks get called twice each time user adds a row.
    		eval($(parent).attr('callback') + "('"+ rowId + "')");
    	} catch (e) {console.log(e)}
    }
    
}

function deleteRow(rowId) {
	try {
      if (global_formNavigate != undefined) { global_formNavigate = false; }
	} catch(e){}
	if ($(rowId).parent().children().size() > 1) {
		// FIXME: do we need to renumber IDs afterwards if they delete from the
		// middle?
		$(rowId).remove();
	} else {
		clearRow(rowId);
	}
	return false;
}

// delete the nearest parent TR of the provided element
function deleteParentRow(elem) {
    if($(elem).parents("tr").length>0) {
        var id = $(elem).parents("tr").first().attr('id');
        console.debug("deleteParentRow id:" + id);
        deleteRow('#' + id);
    }
}


function refreshInputDisplay() {
    var selectedInputMethod = $('#inputMethodId').val();
    var showUploadDiv = (selectedInputMethod=='file');
    $('#uploadFileDiv').toggle(showUploadDiv);
    $('#textInputDiv').toggle(!showUploadDiv);
}

/*
 * these are utility functions to help with managing complex valdation of
 * contacts and credit
 */

function getLastName(element) {
    return getSiblingElement(element,'last');
}

function getEmail(element) {
    return getSiblingElement(element,'email');
}

function getRole(element) {
    return getSiblingElement(element,'role');
}

function getInstitution(element) {
    return getSiblingElement(element,'institution');
}

function getPersId(element) {
    var sib = getSiblingElement(element,'ids');
    // FIXME: depending on the element, the id field either matches 'ids' or
	// 'person.id'. we should consolidate to one field
    if(sib=='') sib = getSiblingElement(element, 'person.id');
    console.debug("getPersId result:");
    console.debug(sib);
    return sib;
}

function getFirstName(element) {
    return getSiblingElement(element,'first');
}

function getSiblingElement(element,name) {
    var ret = '';
    $.each($(element).parents("tr").find("input,select"), function(k,v) {
        if (v.name.toLowerCase().indexOf(name) != -1) {
          ret = $("#" + v.id);
          return false;
        }
    });
    return ret;
}

function getPersCalc(element) {
    return $($(element).parents("tr").find(".hiddenCalc"));
}

function setCalcPersVal(element) {
    if (element != undefined) {
        var e = getPersCalc(element);
        var val = 0;
        if (getRole(element).val() != undefined && getRole(element).val().length > 0) val++;
        if (getLastName(element).val() != undefined && getLastName(element).val().length > 0) val++;
        if (getFirstName(element).val() != undefined && getFirstName(element).val().length > 0) val++;
        e.val(val);
        e.valid();
    }
}

// ensure that the provided element contains a valid user tdar user id
function setCalcUserVal(element) {
    var e = getPersId(element);
    
    // are the first name and last name empty?
    var fname = getFirstName(element);
    var lname = getLastName(element);
    // reset the userid if the first and last name are blank (we assume the user
	// wants to 'delete' that row)
    if($.trim(fname.val()).length==0 && $.trim(fname.val()).length==0) {
        console.debug("first and lastname blank: resetting userid to -1");
        e.val(-1);
    }
    
    
    // we might temporarily lose focus if the user selects a valid useraccount
	// via the dropdown
    // and the mouse, thus causing a brief moment where the userid is invalid
	// and causing validation
    // to fail. So, we just delay a bit and run validation on userid again.
    setTimeout(function() {e.valid();}, 200);
}


function personAdded(id) {
    console.trace("person added " + id);
    $(".creatorInstitution", "#" + id).hide();
    $(".creatorPerson", "#" + id).show();
}

function institutionAdded(id) {
    console.trace("institution added " + id);
    // hide the person record
    $(".creatorPerson", "#" + id).hide();
    $(".creatorInstitution", "#" + id).show();
}



function setToolTipContents(targetElem, tooltipElem) {
	var fieldOff = $(targetElem).offset();
	var noteOff = $('#notice').offset();
	$('#notice').offset({left: noteOff.left,top: fieldOff.top});
	var label, content;
	// where is the tooltip content, in the 'tooltipcontent' attribute or in a
	// separate div?
	if(tooltipElem) {
        label = $(tooltipElem).find('h2').html();
        if(!label) label = $(targetElem).find('h3').html(); // try to re-use the
															// h3 of the target,
															// if it exists, as
															// the label
        content = $(tooltipElem).find('div').html();
	} else if($(targetElem).attr('tooltipcontent')) {
	    label = $(targetElem).attr('tiplabel');
	    content = $(targetElem).attr('tooltipcontent');
	} else {
	    console.error("unable to bind tooltip - no tooltip element orr tooltipcontent found")
	}
	$('#notice').html("<h2>" + label + "</h2><div id='noticecontent'>" +  content + "</div>");
}


// find any standalone tooltip divs
function initializeTooltips() {
    $('[tooltipfor*=]').each(
            function(index) {
                // console.debug('found tooltip');
                var tooltipElem = this;
                $(tooltipElem).addClass("hidden");
                // in case you want to use the same toolitip for multiple
                // elements (e.g. "tooltipfor='id1,id2,id3'")
                var targetElems = $.map($(tooltipElem).attr('tooltipfor')
                        .split(','), function(str) {
                    return $('#' + str);
                });
                // bind targets to tooltips
                $.each(targetElems, function(i, targetElem) {
                    $(targetElem).bind('mouseenter',function() {setToolTipContents(targetElem, tooltipElem);});
                    $(targetElem).bind('focusin',function() {setToolTipContents(targetElem, tooltipElem);});
                });
            });
    console.trace('done binding tooltips');
};

// expand those nodes where children are selected
$(function(){
    $(".treeview input:checked").parents(".expandable").children(".hitarea").trigger("click");    
});


function switchLabel(field,type) {
  var label = "#" + $(field).attr('id') + '-label';
  if($(field).attr(type) != undefined && $(label) != undefined) {
	$(label).text($(field).attr(type));
  }
}


$.validator.addMethod("multiPersRole", function(value, element) {
	console.log(getPersCalc(element).val());
	var val = getPersCalc(element).val();
	if (val == "") {
		val = 0;
	} else {
		val = parseInt(val);
	}
    return (val  == 0 || val > 2);
}, "first name, last name, and role are required");
/*
 * 
 * $.validator.addMethod("multiPers", function(value, element) { return
 * (parseInt(getPersCalc(element).val()) > 1 ||
 * parseInt(getPersCalc(element).val()) == 0); }, "first and last name are
 * required");
 * 
 */


$.validator.addMethod("latLong", function(value, element) {
	return value.match(/^(((\-?)(\d+)(\.?)(\d*))|)$/);
}, "a valid lat/long in the format DEG.Min/Sec (eg. -67.892068) required");

$.validator.addMethod("reasonableDate", function(value, element) {
	// FIXME: not just 4 digits... > ???
	return value.match(/^(((\d{4}))|)$/);
}, "a date in the last millenia is expected");

$.validator.addMethod("isbn", function(value, element) {
	return value.match(/^(((\d+)-?(\d+)-?(\d+)-?([\dX]))|((978|979)-?(\d{9}[\dXx]))|)$/);
}, "you must include a valid 10/13 Digit ISBN");

$.validator.addMethod("issn", function(value, element) {
	return value.match(/^((\d{4})-?(\d{3})(\d|X|x)|)$/);
}, "you must include a valid 8 Digit ISSN");


$.validator.addMethod("float", function(value, element) {
	return value.match(/^(((\-?)(\d+)(\.?)(\d*))|)$/);
	}, "a valid lat/long in the format DEG.Min/Sec (eg. -67.892068) required");


$.validator.addMethod("rowNotEmpty", function(value, element) {
    if(parseInt(value) != undefined && parseInt(value) > 0) {
        return true;
    }
    else if(    (getLastName(element).val() != undefined && getLastName(element).val().length > 0) ||
                 (getFirstName(element).val() != undefined && getFirstName(element).val().length > 0) ) {
        return false;
    }
    return true;
}, "valid user required");

// http://stackoverflow.com/questions/1260984/jquery-validate-less-than
$.validator.addMethod('lessThanEqual', function(value, element, param) {
    if (this.optional(element)) return true;
    var i = parseInt(value);
    var j = parseInt($(param).val());
    return i <= j;
}, "This value must be less than the maximum value");

$.validator.addMethod('greaterThanEqual', function(value, element, param) {
    if (this.optional(element)) return true;
    var i = parseInt(value);
    var j = parseInt($(param).val());
    return i >= j;
}, "This value must be greater than the minimum value");

$.validator.addMethod('asyncFilesRequired', function(value, elem) {
    return $('tr', '#files').not('.noFiles').size() > 0; 
}, "At least one file upload is required.");


// $.validator.addClassRules("radiocarbonDate", {range:[0,100000]});
// $.validator.addClassRules("julianYear", {range:[-99900, 2100]});

// called whenever date type changes
function prepareDateFields(selectElem){
    var startElem = $(selectElem).siblings('.coverageStartYear');
    var endElem = $(selectElem).siblings('.coverageEndYear');
    $(startElem).rules("remove");
    $(endElem).rules("remove");
    switch ($(selectElem).val()) {
    case "CALENDAR_DATE":
        $(startElem).rules("add", {range:[-99900, 2100], lessThanEqual:endElem, required:function(){return $(endElem).val()!= "";}});
        $(endElem).rules("add", {range:[-99900, 2100], required:function(){return $(startElem).val()!= "";}});
        break;
    case "RADIOCARBON_DATE":
        $(startElem).rules("add", {range:[0,100000], greaterThanEqual:endElem, required:function(){return $(endElem).val()!= "";}});
        $(endElem).rules("add", {range:[0,100000], required:function(){return $(startElem).val()!= "";}});
        break;
    case "none":
        break;
    }
}
$(function() {
    $('.coverageTypeSelect').change(function(){prepareDateFields(this);});
});

function coverageRowAdded(rowElem) {
    var selectElem = $('.coverageTypeSelect', rowElem).change(function(){prepareDateFields(this);}).change();
}


function bookmarkResource(resourceId, link) {
	$.getJSON(getBaseURI() + "resource/bookmarkAjax?resourceId=" + resourceId, function(data) {
  		if (data.success) {
  			var _link = $(link);
  			_link.attr("href", getBaseURI() + "resource/removeBookmark?resourceId=" + resourceId);
  			_img = _link.children("img");
  			_img.attr("src", getBaseURI() + "images/bookmark.gif");
  			$(_img).next().replaceWith("<span>un-bookmark</span>");
  			_link.removeAttr("onclick");
  			_link.click(function(){ removeBookmark(resourceId, link); return false; });
		}
  	});
}

function removeBookmark(resourceId, link) {
	$.getJSON(getBaseURI() + "resource/removeBookmarkAjax?resourceId=" + resourceId, function(data) {
  		if (data.success) {
  			var _link = $(link);
			_link.attr("href", getBaseURI() +  "resource/bookmark?resourceId=" + resourceId);
			_img = _link.children("img");
			_img.attr("src", getBaseURI() + "images/unbookmark.gif");
			$(_img).next().replaceWith("<span>bookmark</span>");
			_link.removeAttr("onclick");
			_link.click(function(){ bookmarkResource(resourceId, link); return false;});
  		}
  	});
}

function deleteResource() {
	return confirm("Really delete this resource?  This cannot be undone.");	
}


// return first sibling whose id contains specified substring (put your element
// in a div/span to limit the scope of the search)
function findSibling(element, str) {
    var sibs = $(element).siblings();
    var result = '';
    $.each(sibs, function(k,v) {
        if(v.id.toLowerCase().indexOf(str.toLowerCase()) != -1) {
            result = $("#" + v.id);;
            return false;
        }
    });
    return result;
}


function formSubmitDisable(optionalMsg) {
    var waitmsg = optionalMsg;
    if(!waitmsg) waitmsg = "Please wait...";
    if ($('#submitButton').data('oldVal') == undefined) {
      $('#submitButton').data('oldVal', $('#submitButton').val());
    }
    $('#submitButton').val(waitmsg);
    
    $('#submitButton').attr('disabled', 'disabled');
}

//enable the save button and replace it's former label (e.g. from 'please wait' to 'save')
function formSubmitEnable() {
    var oldVal = $('#submitButton').data('oldVal');
    //it's likely formSubmitDisable was called at least once before now, but don't assume
    if(oldVal) {
        $('#submitButton').val($('#submitButton').data('oldVal'));
    }
    $('#submitButton').removeAttr('disabled');
}

function applyWatermarks() {
$("[watermark]").each(function() {
    $(this).watermark($(this).attr("watermark"));
  });
}

// show the access rights reminder if any files are marked as confidential or if the resource is embargoed
function showAccessRightsLinkIfNeeded() {
    if($('#cbConfidential').is(':checked') || $(".fileProxyConfidential:checked").length > 0 || $('#resourceAvailability').val() == 'Embargoed') {
        $('#divConfidentialAccessReminder').removeClass("hidden");
    } else {
        $('#divConfidentialAccessReminder').addClass("hidden");
    }
}



/*
 * downward inheritance support
 */
var repeatFields = ['uncontrolledCultureKeywords','siteNameKeywords', 'uncontrolledSiteTypeKeywords',
    'geographicKeywords', 'otherKeywords', 'temporalKeywords'];


function populateSection(elem, formdata) {
    $(elem).populate(formdata, {resetForm:false, phpNaming:false, repeatFields: repeatFields});
}

// convert a serialized project into the json format needed by the form.
function convertToFormJson(rawJson) {
    // create a skeleton of what we need
    var obj = {
            
            title: rawJson.title,
            id:rawJson.id,
            resourceType:rawJson.resourceType,
            investigationInformation: {
                investigationTypeIds: $.map(rawJson.investigationTypes, function(v){return v.id;}) || []
            },
            siteInformation: {
                siteNameKeywords: $.map(rawJson.siteNameKeywords, function(v){return v.label;}),
                approvedSiteTypeKeywordIds: $.map(rawJson.approvedSiteTypeKeywords, function(v){return v.id;}) || [],
                uncontrolledSiteTypeKeywords: $.map(rawJson.uncontrolledSiteTypeKeywords, function(v){return v.label;})
            },
            materialInformation: {
                materialKeywordIds: $.map(rawJson.materialKeywords, function(v){return v.id;}) || []
            },
            culturalInformation: {
                approvedCultureKeywordIds: $.map(rawJson.approvedCultureKeywords, function(v){return v.id;}) || [],
                uncontrolledCultureKeywords: $.map(rawJson.uncontrolledCultureKeywords, function(v){return v.label;})
            },
            spatialInformation: {
                geographicKeywords: $.map(rawJson.geographicKeywords, function(v) {return v.label;}),
                'p_maxy':null, // FIXME: I don't think these p_**** fields are
								// used/needed
                'p_minx':null,
                'p_maxx':null,
                'p_miny':null
            },
            temporalInformation: {
                temporalKeywords: $.map(rawJson.temporalKeywords, function(v) {return v.label;}),
                'calendarDate.startDate': null,
                'calendarDate.endDate': null,
                'radiocarbonDate.startDate': null,
                'radiocarbonDate.endDate': null
            },
            otherInformation: {
                otherKeywords: $.map(rawJson.otherKeywords, function(v){return v.label;})
            }
    };
    
    
    if(rawJson.calendarDate) {
        obj.temporalInformation['calendarDate.startDate'] = rawJson.calendarDate.startDate;
        obj.temporalInformation['calendarDate.endDate'] = rawJson.calendarDate.endDate;
    }
    
    if(rawJson.radiocarbonDate) {
        obj.temporalInformation['radiocarbonDate.startDate'] = rawJson.radiocarbonDate.startDate;
        obj.temporalInformation['radiocarbonDate.endDate'] = rawJson.radiocarbonDate.endDate;
    }

    // FIXME: update the parent latlong box (i.e. the red box not the brown
	// box)..p_miny, pmaxy, etc. etc.
    // console.warn(rawJson.firstLatitudeLongitudeBox)
    if(rawJson.firstLatitudeLongitudeBox) {
        obj.spatialInformation['minx'] = rawJson.firstLatitudeLongitudeBox.minObfuscatedLongitude;
        obj.spatialInformation['maxx'] = rawJson.firstLatitudeLongitudeBox.maxObfuscatedLongitude;
        obj.spatialInformation['miny'] = rawJson.firstLatitudeLongitudeBox.minObfuscatedLatitude;
        obj.spatialInformation['maxy'] = rawJson.firstLatitudeLongitudeBox.maxObfuscatedLatitude;
    }
    
    return obj;
}

function disableSection(selector) {
    $(selector + ' :input').not(".alwaysEnabled").attr('disabled', true);
    $(selector + ' label').not(".alwaysEnabled").addClass('disabled');
    $('.addAnother, .minus', selector).hide();
}

function enableSection(selector) {
    $(selector + ' :input').removeAttr('disabled');
    $(selector + ' label').removeClass('disabled');
    $('.addAnother, .minus', selector).show();
}


// remove all but the first item of a repeatrow table.
function resetRepeatRowTable(id, newSize) {
    var table = $('#' + id);
    table.hide();
    if(!newSize) newSize = 1;
    table.find("tr:not(:first)").remove();
    // change the id/name for each element in first row that matches _num_
	// format to _0_
    var firstRow = table.find("tr:first");
    resetIndexedAttributes(firstRow);
    if(newSize > 1) {
        for(var i = 1; i < newSize; i++) {
            repeatRow(id);
        }
    }
    table.show();
}

// modify id/name attribute in element and children if they follow 'indexed'
// pattern
// e.g. <input name='my_input_field[12]'> becomes <input
// name='my_input_field[0]'>
function resetIndexedAttributes(elem) {
    var rex = /^(.+[_|\[])([0-9]+)([_|\]])$/;  // string ending in _num_ or
												// [num]
    var replacement = "$10$3"; // replace foo_bar[5] with foo_bar[0]
    $(elem).add("tr, :input", elem).each(function(i,v){
        var id = $(v).attr("id");
        var name = $(v).attr("name");
        if(id) {
            var newid = id.replace(rex, replacement);
            console.trace(id + " is now " + newid);
            $(v).attr("id", newid);
        }
        if(name) {
            var newname = name.replace(rex, replacement);
            console.trace(name + " is now " + newname);
            $(v).attr("name", newname);
        }
    });
}

// TODO: make this a jquery plugin?
// clears (not resets) the selected elements
function clearFormSection(selector) {
 // Use a whitelist of fields to minimize unintended side effects.
    $('input:text, input:password, input:file', selector).val('');  
    // De-select any checkboxes, radios and drop-down menus
    $(':input', selector).prop('checked', false).prop('selected', false);    
}

// return skeleton project
function getBlankProject() {
    var skeleton = {"approvedCultureKeywords":[]
        ,"approvedSiteTypeKeywords":[]
        ,"calendarDate":null
        ,"cultureKeywords":[]
        ,"dateRegistered":{}
        ,"description": null
        ,"firstLatitudeLongitudeBox":null
        ,"geographicKeywords":[]
        ,"id":null
        ,"investigationTypes":[]
        ,"materialKeywords":[]
        ,"otherKeywords":[]
        ,"radiocarbonDate":null
        ,"resourceType":null
        ,"siteNameKeywords":[]
        ,"siteTypeKeywords":[]
        ,"submitter":null
        ,"temporalKeywords":[]
        ,"title": null
        ,"uncontrolledCultureKeywords":[]
        ,"uncontrolledSiteTypeKeywords":[]}
    return skeleton;
}

// return true if the repeatrows contained in the selector match the list of
// strings
// FIXME: these are terrible function names
function inheritingRepeatRowsIsSafe(rootElementSelector, values) {
    var repeatRowValues = $.map( $('input[type=text]', rootElementSelector), function(v,i) {
        if($(v).val()) return $(v).val();
    });
    return repeatRowValues.length == 0 || $.compareArray(repeatRowValues, values);
}

// FIXME: these are terrible function names
// return true if this section can 'safely' inherit specified values. 'safe'
// means that the target values are empty or the same
// as the incoming values.
function inheritingCheckboxesIsSafe(rootElementSelector, values) {
    var checkedValues = $.map($(':checkbox:checked', rootElementSelector),function(v,i){
        return $(v).val();
    });
    var isSafe = checkedValues.length == 0 || $.compareArray(checkedValues, values); 
    return isSafe;
}

function inheritingMapIsSafe(rootElementSelector, spatialInformation) {
    // FIXME: pretty sure that rootElementSelector isn't needed. either ditch it
	// or make the fields retrievable by name instead of id
    var si = spatialInformation;
    // compare parent coords to this form's current coords. seems like overkill
	// but isn't.
    var jsonVals = [si.minx, si.miny, si.maxx, si.maxy]; // strip out nulls
    var formVals = [];
    formVals = formVals.concat($('#minx'));
    formVals = formVals.concat($('#miny'));
    formVals = formVals.concat($('#maxx'));
    formVals = formVals.concat($('#maxy'));
    
    formVals = $.map(formVals, function(item){ if($(item).val()) return $(item).val();});
    return  formVals.length == 0 || $.compareArray(jsonVals, formVals, false); // don't
																				// ignore
																				// array
																				// order
																				// in
																				// comparison
}

function inheritingDatesIsSafe(rootElementSelector, temporalInformation) {
    var ti = temporalInformation;
    var jsonVals = [ti['calendarDate.startDate'], ti['calendarDate.endDate'], ti['radiocarbonDate.startDate'], ti['radiocarbonDate.endDate']];
    var formVals = [];
    formVals =  formVals.concat($('#calendarYearStart').val());
    formVals =  formVals.concat($('#calendarYearEnd').val());
    formVals =  formVals.concat($('#radiocarbonYearStart').val());
    formVals =  formVals.concat($('#radiocarbonYearEnd').val());
    var formValsAllBlank = false;
    for(var i = 0; i < formVals.length; i++) {
        if(formVals[i] == "") formVals[i] = null;
        formValsAllBlank = formValsAllBlank || !formVals[i];
    }
    return formValsAllBlank || $.compareArray(jsonVals, formVals, false);
}

function inheritInvestigationInformation(formId, json){
    disableSection('#divInvestigationInformation');
    clearFormSection('#divInvestigationInformation');
    populateSection(formId, json.investigationInformation);
}

function inheritSiteInformation(formId, json){
    disableSection('#divSiteInformation');
    clearFormSection('#divSiteInformation');
    resetRepeatRowTable('siteNameKeywordTable', json.siteInformation['siteNameKeywords'].length);
    resetRepeatRowTable('uncontrolledSiteTypeKeywordTable', json.siteInformation['uncontrolledSiteTypeKeywords'].length);
    populateSection(formId, json.siteInformation);
}

function inheritMaterialInformation(formId, json){
    disableSection('#divMaterialInformation');
    clearFormSection('#divMaterialInformation');
    populateSection(formId, json.materialInformation);
}

function inheritCulturalInformation(formId, json){
    disableSection('#divCulturalInformation');
    clearFormSection('#divCulturalInformation');
    resetRepeatRowTable('uncontrolledCultureKeywordTable', json.culturalInformation['uncontrolledCultureKeywords'].length);
    populateSection(formId, json.culturalInformation);
}

function inheritSpatialInformation(formId, json){
    disableSection('#divSpatialInformation');
    disableMap();
    
    clearFormSection('#divSpatialInformation');
    resetRepeatRowTable('geographicKeywordTable', json.spatialInformation['geographicKeywords'].length);
    populateSection(formId, json.spatialInformation);
    
    // clear the existing redbox and draw new one;
    if(GZoomControl.G.oZoomArea) {
        // TODO: reset the map to default zoom and default location
        GZoomControl.G.oMap.removeOverlay(GZoomControl.G.oZoomArea);        
    }
    drawMBR();
}

function inheritTemporalInformation(formId, json){
    disableSection('#divTemporalInformation');
    clearFormSection('#divTemporalInformation');
    resetRepeatRowTable('temporalKeywordTable', json.temporalInformation['temporalKeywords'].length);
    populateSection(formId, json.temporalInformation);
}

function inheritOtherInformation(formId, json){
    disableSection('#divOtherInformation');
    clearFormSection('#divOtherInformation');
    resetRepeatRowTable('otherKeywordTable', json.otherInformation['otherKeywords'].length);
    populateSection(formId, json.otherInformation);

}


function bindCheckboxToInheritSection(cbSelector, divSelector, isSafeCallback, inheritSectionCallback, enableSectionCallback) {
    $(cbSelector).change(function(e){
        var cb = this;
        var divid = divSelector;
        var proceed = true;
        if($(cb).is(":checked")) {
            // check if inheriting would overrwrite existing values
            var isSafe = isSafeCallback();
            if(!isSafe) {
                proceed = confirm("Inheriting this section will overwrite existing values. Continue?");
                if(!proceed) {
                    $(cb).removeAttr("checked");
                }
            }
            if(proceed) {
                inheritSectionCallback();
            }
        }
        else {
            console.trace(divid + " cleared");
            if(enableSectionCallback) {
                enableSectionCallback();
            } else {
                enableSection(divid);
            }
        }
    });
    
}

/**
 * Google Maps Support
 */

// googlemaps.js
/* FIXME: modify this file to be a function that gets invoked */
var map = null; 
var gzControl = null;
var boundBox = false;
/*
 * this writing is very important. note the "(" after addDomListener should
 * match ")" after this whole function "}" I cannot directly write this function
 * as load() and call this in the body load. The reason is that the
 * popcalendar.js also init the window.
 */
GEvent.addDomListener(window,'load',function(){
    // alert("action="+action);
    if (GBrowserIsCompatible() && document.getElementById("large-google-map") != undefined) {       
        map = new GMap2(document.getElementById("large-google-map"));
        // alert("load map"+map);
        // map.setCenter(new GLatLng(37.4419, -122.1419), 13);
        // add terrain map type as well.
        map.addMapType(G_PHYSICAL_MAP);
        // add the controls
        map.addControl(new GLargeMapControl());
        var ovmap = new GOverviewMapControl();
        ovmap.setMapType(G_PHYSICAL_MAP); 
        GEvent.addListener(map, 'maptypechanged', function(){ 
            ovmap.setMapType(map.getCurrentMapType()); 
        }); 
        map.addControl(ovmap);
        map.addControl(new GMapTypeControl());
        map.addControl(new GScaleControl()); // hpcao added 2008-04-30
        
        
        
        gzControl = new GZoomControl(
            /* first set of options is for the visual overlay. */
            { nOpacity:.2, sBorder:"2px solid red" },
            /* second set of optionis is for everything else */
            {
                sButtonHTML:"<div id='selectARegion'>Select Region</div>",
                sButtonZoomingHTML:"<div id='selectARegion'>Select Region</div>"
            },
// sButtonHTML:"<img src='"+ getBaseURI() +"images/select-region.png' />",
// sButtonZoomingHTML:"<img src='"+ getBaseURI() +"images/select-region.png'
// />",
// oButtonStartingStyle:{width:'24px',height:'24px'}

            /* third set of options specifies callbacks */
            { buttonClick:function(){}, dragStart:function(){}, 
            dragging:function(x1,y1,x2,y2){}, 
            dragEnd:function(nw,ne,se,sw,nwpx,nepx,sepx,swpx) {
            // hpcao changed this after Allen changed the name of the four
			// spatial coordinates
                $("#minx").val(sw.lng());
                $("#miny").val(sw.lat());
                $("#maxx").val(ne.lng());
                $("#maxy").val(ne.lat());

                $("#d_minx").val(Geo.toDMS(sw.lng()));
                $("#d_miny").val(Geo.toDMS(sw.lat()));
                $("#d_maxx").val(Geo.toDMS(ne.lng()));
                $("#d_maxy").val(Geo.toDMS(ne.lat()));
boundBox = true;
            }
        }
        );
    
        clearControl = new ClearControl();
        
        // hpcao adds this condition
        // based on the action (edit or view), we determine whether or
                // not to allow users to draw a bounding box for the latitude.
                var path = window.location.pathname;
                var action = path.substring(path.lastIndexOf("/") + 1);
                if(action.indexOf("edit") != -1 || action.indexOf("add") != -1
                        || path.indexOf("search") != -1) { 
                    map.addControl(gzControl, new GControlPosition(G_ANCHOR_BOTTOM_LEFT,new GSize(5,85)));
                    map.addControl(clearControl, new GControlPosition(G_ANCHOR_BOTTOM_LEFT,new GSize(5,45)));
        }
        // set the starting location and zoom
        var bName = navigator.appName;
        var bVersion = parseFloat(navigator.appVersion);
        if (bName == "Microsoft Internet Explorer") {               
            map.setCenter(new GLatLng(40, -97.00), 4, G_PHYSICAL_MAP);    // other
																			// choices:
																			// G_SATELLITE_MAP,
																			// G_HYBRID_MAP
        } else {
            map.setCenter(new GLatLng(40, -97.00), 4, G_PHYSICAL_MAP);    // G_HYBRID_MAP
        }
        map.enableDoubleClickZoom();
    }// end of if
    
    // hpcao added this to draw a box
    // only when the type is register (register project/datasources), this box
	// is not shows
    // in other cases (editing and showing metadata), this box would be shown.
    // if(mypagetype!="register"){
        drawMBR();
        drawMBR("p_","#996633");
    // }

    }
);

function ClearControl() {}

    ClearControl.prototype = new GControl();
    ClearControl.prototype.initialize = function(map) {
          var container = document.createElement("div");
          var zoomInDiv = document.createElement("div");
          container.appendChild(zoomInDiv);
          zoomInDiv.id = "mapResetButton";
          zoomInDiv.appendChild(document.createTextNode("Reset"));
          GEvent.addDomListener(zoomInDiv, "click", function() {
              try {
                  map.removeOverlay(GZoomControl.G.oZoomArea);
                  document.getElementById("minx").value = "";
                  document.getElementById("miny").value = "";
                  document.getElementById("maxx").value = "";
                  document.getElementById("maxy").value = "";

                  document.getElementById("d_minx").value = "";
                  document.getElementById("d_miny").value = "";
                  document.getElementById("d_maxx").value = "";
                  document.getElementById("d_maxy").value = "";
} catch (e) {}
          });

          map.getContainer().appendChild(container);
          return container;
        }
// }





// This function must be called after the map is displayed.
// If put this to the head script, the map cannot be zoomed to U.S. map.

// edit project and edit dataset cannot use this, so get this out
// to project registration and dataset registration
// zoomToUS();

// the following two functions work for "Locate" some spatial box
function locateCoords(){
    var minx = document.getElementById("minx").value;
    var miny = document.getElementById("miny").value;
    var maxx = document.getElementById("maxx").value;
    var maxy = document.getElementById("maxy").value;
    var minx1 = parseFloat(minx.replace(/^\s*|\s*$/g,""));
    var miny1 = parseFloat(miny.replace(/^\s*|\s*$/g,""));
    var maxx2 = parseFloat(maxx.replace(/^\s*|\s*$/g,""));
    var maxy2 = parseFloat(maxy.replace(/^\s*|\s*$/g,""));
    // var minx1 =
	// parseFloat(document.searchForm.ui_tf_jsp_minx_minx.value.replace(/^\s*|\s*$/g,""));
    // var miny1 =
	// parseFloat(document.searchForm.ui_tf_jsp_miny_miny.value.replace(/^\s*|\s*$/g,""));
    // var maxx2 =
	// parseFloat(document.searchForm.ui_tf_jsp_maxx_maxx.value.replace(/^\s*|\s*$/g,""));
    // var maxy2 =
	// parseFloat(document.searchForm.ui_tf_jsp_maxy_maxy.value.replace(/^\s*|\s*$/g,""));

    // alert(minx1);
    if(minx1 == null || minx1 == "" || miny1 == null || miny1 == "" || maxx2 == "" || maxx2 == null || maxy2 == null || maxy2 == ""){
        alert("Please fill all four coordinate fields");
    }
    else{
        // alert("call make selection...");
        makeSelection(minx1, miny1, maxx2, maxy2);
    }
}
function makeSelection(x1, y1, x2, y2) {
    // alert ("makeSelection...");

    document.getElementById("minx").value=x1;
    document.getElementById("miny").value=y1;
    document.getElementById("maxx").value=x2;
    document.getElementById("maxy").value=y2;

    // draw a red box on top of the map
    var colour = "#00OOFF";
    var width  = 2;
    var pts = [];
    pts[0] = new GLatLng(y1, x1);
    pts[1] = new GLatLng(y1, x2);
    pts[2] = new GLatLng(y2, x2);
    pts[3] = new GLatLng(y2, x1);
    pts[4] = new GLatLng(y1, x1);
    // alert ("makeSelection 2 ...");

    var G = GZoomControl.G;
    if (G.oZoomArea != null) G.oMap.removeOverlay(G.oZoomArea);

    // alert (G.style.sOutlineColor);
    G.oZoomArea = new GPolyline(pts, G.style.sOutlineColor, G.style.nOutlineWidth+1, .4);

    var bounds = new GLatLngBounds();
    bounds.extend(pts[0]);
    bounds.extend(pts[1]);
    bounds.extend(pts[2]);
    bounds.extend(pts[3]);
    map.setZoom(map.getBoundsZoomLevel(bounds));

    map.panTo(new GLatLng((y1+y2)/2, (x1+x2)/2));
    map.addOverlay(G.oZoomArea);
}


// draw a red box on top of the map
function drawMBR(prefix,colour){        
    // x1 = min longitude
    // x2 = max longitude
    // y1 = min latitude
    // y2 = max latitude
    if (document.getElementById("large-google-map") == undefined ) return;
    if (prefix == undefined) prefix = "";

    // make sure that the form name of the document is
    // "resourceRegistrationForm"
    // and it has minx, miny, maxx, and maxy
    try {
    var x1 = parseFloat(document.getElementById(prefix + "minx").value.replace(/^\s*|\s*$/g,""));
    var y1 = parseFloat(document.getElementById(prefix + "miny").value.replace(/^\s*|\s*$/g,""));
    var x2 = parseFloat(document.getElementById(prefix + "maxx").value.replace(/^\s*|\s*$/g,""));
    var y2 = parseFloat(document.getElementById(prefix + "maxy").value.replace(/^\s*|\s*$/g,""));

    if(isNaN(x1)||isNaN(y1)||isNaN(x2)||isNaN(y2)) return;

    // alert("is NOT NaN");
    var width  = 2;
    var pts = [];
    pts[0] = new GLatLng(y1, x1);
    pts[1] = new GLatLng(y1, x2);
    pts[2] = new GLatLng(y2, x2);
    pts[3] = new GLatLng(y2, x1);
    pts[4] = new GLatLng(y1, x1);       

    // alert("hi1");
    var G = GZoomControl.G;
    if (colour == undefined ) colour = G.style.sOutlineColor;
    var zoomarea = new GPolyline(pts, colour, G.style.nOutlineWidth+1, .4);
    
    if (prefix == "" ) {
        try {
            if (G.oZoomArea != null) G.oMap.removeOverlay(G.oZoomArea);
        } catch (e) {}
        var style = G.style;
        G.oZoomArea = zoomarea;
    }

    var bounds = new GLatLngBounds();
    bounds.extend(pts[0]);
    bounds.extend(pts[1]);
    bounds.extend(pts[2]);
    bounds.extend(pts[3]); 
    map.setZoom(map.getBoundsZoomLevel(bounds));

    map.panTo(new GLatLng((y1+y2)/2, (x1+x2)/2));
    map.addOverlay(zoomarea);
    } catch (e) {}
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
            'TIME_OF_FLIGHT':'.scantech-fields-tof',
            'PHASE_BASED':'.scantech-fields-phase',
            'TRIANGULATION':'.scantech-fields-tri'
    };
    
    if($(elemScannerTech).val()) {
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
    $(scannerTechElem).change(
            function() {
                var elem = this;
                showScannerTechFields(elem);
            }
    );
}




function applyTreeviews() {
    // FIXME: There is a bug in the Treeview plugin that causes the
	// 'expand/collapse' icon to not to not show when the last item in the list
	// has sublist.
    // so we tack on an invisible LI and it magically shows up again.
    $('#approvedSiteTypeKeywordIds_Treeview,#approvedCultureKeywordIds_Treeview').append(
        '<li style="display:none !important">&nbsp</li>'
    );

    $("#approvedSiteTypeKeywordIds_Treeview").treeview({
        collapsed: true,
        persist: "cookie",
        cookieId: "tdar-treeview-site-type"
    });
    
    $("#approvedCultureKeywordIds_Treeview").treeview({
        collapsed: true,
        persist: "cookie",
        cookieId: "tdar-treeview-culture"
    });
    
    // expand those nodes where children are selected
    $(".treeview input:checked").parents(".expandable").children(".hitarea").trigger("click");
}




function displayAsyncError(handler,msg) {
    var errorMsg = ("<p>tDAR experienced errors while uploading your file.  Please try again, or if the error persists, please save this record without the "
        + " file attachment and notify a tDAR Administrator.</p>");
    if(msg) errorMsg = msg;
    var jqAsyncFileUploadErrors = $('#divAsyncFileUploadErrors');
     jqAsyncFileUploadErrors.html(errorMsg);
     jqAsyncFileUploadErrors.show();
    try {
     handler.removeNode(handler.uploadRow);
    } catch(ex) {}
    setTimeout(function(){jqAsyncFileUploadErrors.fadeOut(1000);}, 10000);
}

function sendFileIfAccepted(event, files, index, xhr, handler, callBack) {
    if(!files || files.length==0) {
        handler.removeNode(handler.uploadRow);
        return false;
    } 
    var accepted = false;
    if(files[0].fileName) {
        for(var i =0; i < files.length; i++) {
            accepted = fileAccepted(files[i].fileName);
            if(!accepted) break;
        }

    }  
    else { 
        accepted = fileAccepted(files[index].name);
   }

    if(accepted) {
        asyncUploadStarted();
        callBack();
    } else {
        handler.uploadRow.find('.file_upload_progress').html('<span class="error">Sorry, this file type is not accepted</span>');
        setTimeout(function () {
            handler.removeNode(handler.uploadRow);
        }, 5000);
        return;
    }
}



function showAsyncReminderIfNeeded() {
    // if we have files in the downloaded table we don't need the reminder shown
    $('#reminder').show();
    if( $('#files').find('tr').length > 1) $('#reminder').hide(); 
}

// grab a ticket (if needed) from the server prior to yielding control to the
// file processor.
// FIXME: on-demand ticket grabbing only works when multiFileRequest turned on.
// Make it work even when turned off.


// additional form data sent with the ajax file upload. for now we only need the
// ticket id
function getFormData() {
    var frmdata = [{name: 'ticketId', value:$('#ticketId').val()}];
    // console.log("getFormData:" + frmdata);
    // console.log(frmdata);
    // $('#txtCurrentId').text($('#ticketId').val());
    // $('#retrievalTicketId').val($('#ticketId').val());
    return frmdata;
}

function asyncUploadStarted() {
    g_asyncUploadCount++;
    formSubmitDisable()
}
    
function asyncUploadEnded() {
    g_asyncUploadCount--;
    if(g_asyncUploadCount  <= 0) {
        formSubmitEnable();
    }
}


    
    
    
/**
 * Autocomplete Support
 */ 
    

    function applyAutoComplete(selector,usersOnly) {
            if(selector=='.nameAutoComplete') {
                $(selector).each(function(i,v) {
                    // console.log("registering:" + v);
                });
            }
            $(selector).autocomplete({
                source: function(request, response) {
                    console.trace('autocomplete called');
                    var lemail = (usersOnly) ? '' : getEmail(this.element).val();
                    $.ajax({
                        url: getBaseURI() + "lookup/person",
                        dataType: "jsonp",
                        data: {
                            email:  lemail,
                            firstName: getFirstName(this.element).val(),
                            lastName: getLastName(this.element).val(),
                            registered: usersOnly,
                            institution: getInstitution(this.element).val()
                        },
                        success: function(data) {
                            response($.map(data.people, function(item) {
                                if (item.institution == undefined) {
                                  item.institution = {};
                                  item.institution.name = '';
                                }
                                return {
                                    label: "<p style='min-height:5em'><img class='silhouette' src=\"" + getBaseURI() + "images/man_silhouette_clip_art_9510.jpg\" width=\"40\"/>"+
                                        "<span class='name'>" + item.properName + "(" + item.email + ")</span>" +
                                        "<br/><span class='institution'>"+ item.institution.name + "</span></p>"
                                    ,
                                    value: function() { 
                                        // this may seem weird, it's because the
										// select function below
                                        // is actually setting the value for us.
                                        // At this point 'this.value' is the
										// contents of the input box.
                                        return this.value;
                                    },
                                    firstName: item.firstName,
                                    lastName: item.lastName,
                                    email: item.email,
                                    institution: item.institution.name,
                                    authid: item.id
                                }
                            }))
                        }
                    })
                },
                minLength: 2,
                select: function(event, ui) {
                            // 'this' is the input box element.
                            getEmail(this).val(ui.item.email);
                            getFirstName(this).val(ui.item.firstName);
                            getLastName(this).val(ui.item.lastName);
                            $(getInstitution(this)).val(ui.item.institution);
                            getPersId(this).val(ui.item.authid);
                },
                open: function() {
                    $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
                    $("ul.ui-autocomplete li a").each(function(){
                      var htmlString = $(this).html().replace(/&lt;/g, '<');
                      htmlString = htmlString.replace(/&gt;/g, '>');
                      $(this).html(htmlString);
                      });
                      $("ul.ui-autocomplete").css("width",$(this).parent().width());
                },
                close: function() {
                    $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
                }
            })
        }; 

        // FIXME:ditch lookupType and instead pass querystring parms via data
        function applyKeywordAutocomplete(selector, lookupType, extraData) {
            var lookupUrl = getBaseURI() + "lookup/" + lookupType;
            console.trace("lookup url:" + lookupUrl);
            $(selector).autocomplete({
                source: function(request,response) {
                    console.trace('keyword autocomplete');
                    $.ajax({
                        url: lookupUrl,
                        dataType: "jsonp",
                        data: $.extend({term: request.term}, extraData),
                        success: function(data) {
                            var values = $.map(data.items, function(item){
                                if(item.key) return {value:item.key, id:item.id};
                                else return {value:item.label, id:item.id};
                            });;
                            
                            response(values);
                        }
                    });
                },
                minLength: 2,
                select: function(data){}
            });
        }

        function applyInstitutionAutoComplete(selector,usersOnly) {
            $(selector).autocomplete({
                source: function(request, response) {
                    var t = $.trim(request.term);
                    console.trace('institution autocomplete called, input:"' + t + '"');
                    if(t.length <= 2) {
                        console.trace('trimmed input too short, not sending ajax');
                        response({});
                        
                    } else {    
                        $.ajax({
                            url: getBaseURI() + "lookup/institution",
                            dataType: "jsonp",
                            data: {
                                institution:  request.term
                            },
                            success: function(data) {
                                response($.map(data.institutions, function(item) {
                                    return {
                                        value:item.name,
                                        id:item.id
                                    };
                                }));
                            }
                        });
                    }
                },
                minLength: 2,
                select: function(event, ui) {
                            console.trace('institution selected:'+ ui.item.value);
                            // TODO: here is where we set the id for the hidden
                            // instition id field
                        }
            });
        }; 
        
        
// INHERITANCE
        function applyInheritance(project,resource) {
        // if we are editing, set up the initial form values
        if(project) {
            json = convertToFormJson(project);
            updateInheritableSections(json);
        }
    
        // update the inherited form values when project selection changes.
        $('#projectId').change(function(e){
            var sel = this;
            console.trace('project changed. new value:' + $(sel).val()); 
            if($(sel).val() != '' && $(sel).val() > 0) {
                console.trace('about to make ajax call for project info');
                $.ajax({
                    url: getBaseURI()  + "project/json",
                    dataType: "jsonp",
                    data: {resourceId: $(sel).val()},
                    success: projectChangedCallback,
                    error: function(msg){console.error("error");}
                });
            }
            else {
                project = getBlankProject();
                json = convertToFormJson(project);
                updateInheritableSections(json);
            }
        });
        
      processInheritance(formId);
      }
        
function processInheritance(formId) {
    // ---- bind inheritance tracking checkboxes
    
    bindCheckboxToInheritSection(
        '#cbInheritingSiteInformation',
        '#divSiteInformation',
        function() {
            var allKeywords = json.siteInformation.siteNameKeywords.concat(json.siteInformation.uncontrolledSiteTypeKeywords);
            return inheritingCheckboxesIsSafe('#divSiteInformation', json.siteInformation.approvedSiteTypeKeywordIds)
                    && inheritingRepeatRowsIsSafe('#divSiteInformation', allKeywords);
        },
        function() {inheritSiteInformation(formId, json)}
    );
    
    bindCheckboxToInheritSection(
        '#cbInheritingTemporalInformation',
        '#divTemporalInformation',
        function() {
            return inheritingRepeatRowsIsSafe('#temporalKeywordTable', json.temporalInformation.temporalKeywords)
                    && inheritingDatesIsSafe('#divTemporalInformation', json.temporalInformation)
        }, 
        function() {inheritTemporalInformation(formId, json);}
    );

    bindCheckboxToInheritSection(
        '#cbInheritingCulturalInformation',
        '#divCulturalInformation',
        function() {return inheritingCheckboxesIsSafe('#divCulturalInformation', json.culturalInformation.approvedCultureKeywordIds) 
                            && inheritingRepeatRowsIsSafe('#divCulturalInformation', json.culturalInformation.uncontrolledCultureKeywords);
        },
        function() {inheritCulturalInformation(formId, json);}
    );

    bindCheckboxToInheritSection(
        '#cbInheritingOtherInformation',
        '#divOtherInformation',
        function() {return inheritingRepeatRowsIsSafe('#divOtherInformation', json.otherInformation.otherKeywords)},
        function() {inheritOtherInformation(formId, json);}
    );

    bindCheckboxToInheritSection(
        '#cbInheritingInvestigationInformation',
        '#divInvestigationInformation',
        function() {return inheritingCheckboxesIsSafe('#divInvestigationInformation', json.investigationInformation.investigationTypeIds)},
        function() {inheritInvestigationInformation(formId, json)}
    );

    bindCheckboxToInheritSection(
        '#cbInheritingMaterialInformation', 
        '#divMaterialInformation', 
        function(){
            return inheritingCheckboxesIsSafe('#divMaterialInformation', json.materialInformation.materialKeywordIds);
        }, 
        function() {inheritMaterialInformation(formId, json)}
   );
    
    bindCheckboxToInheritSection(
        '#cbInheritingSpatialInformation',
        '#divSpatialInformation',
        function(){
            return inheritingMapIsSafe('#divSpatialInformation', json.spatialInformation)
                        && inheritingRepeatRowsIsSafe('#geographicKeywordTable', json.spatialInformation.geographicKeywords);
        }, 
        function(){inheritSpatialInformation(formId, json);},
        function() {
            enableSection('#divSpatialInformation');
            enableMap();
        }
    );
    
    // FIXME:gzoom-control doesn't exist when this code fires on pageload. so we
	// wait a moment before trying checking to see if the google map controls
	// should be hidden
    setTimeout(function(e) {
        if($('#cbInheritingSpatialInformation').is(':checked')) {
            disableMap()
        }
    }, 100);
    
        
} 

function disableMap() {
    $('#large-google-map').addClass('opaque');
    $('#gzoom-control').hide();
    $('#mapResetButton').hide()
}

function enableMap() {
    $('#large-google-map').removeClass('opaque');
    $('#gzoom-control').show();
    $('#mapResetButton').show()
}


// update the project json variable and update the inherited sections
function projectChangedCallback(data) {
    console.trace("project lookup success");
    project = data;
    // if user picked blank option, then clear the sections
    if(!project.id) {
        console.trace('clearing inherited sections');
        project =  getBlankProject();
    }
    
    if(project.resourceType == 'INDEPENDENT_RESOURCES_PROJECT')  {
        project = getBlankProject()
    }
    
    json = convertToFormJson(project);
    updateInheritableSections(json);
}


function enableAll() {
    enableSection('#divInvestigationInformation');
    enableSection('#divSiteInformation');
    enableSection('#divMaterialInformation');
    enableSection('#divCulturalInformation');
    enableSection('#divSpatialInformation');
    enableSection('#divTemporalInformation');
    enableSection('#divOtherInformation');
}


function updateInheritableSections(json) {
    console.trace('updating inheritable sections with information from project:' + json.title);
    
    // indicate in each section which project the section will inherit from.
    var labelText = "Inherit values from parent project";
    if(json && json.title && TDAR.trim(json.title) != "") {
        labelText = 'Inherit values from parent project "' + TDAR.ellipsify(json.title, 60) + '"';
    }
    $('.inheritlabel label').text(labelText);
    
    // show or hide the text of each inheritable section based on checkbox
	// state.
    if($('#cbInheritingInvestigationInformation').is(':checked')) {
        inheritInvestigationInformation(formId, json);
    }

    if($('#cbInheritingSiteInformation').is(':checked')) {
        inheritSiteInformation(formId, json);
    }
    
    if($('#cbInheritingMaterialInformation').is(':checked')) {
        inheritMaterialInformation(formId, json);
    }
    
    if($('#cbInheritingCulturalInformation').is(':checked')) {
        inheritCulturalInformation(formId, json);
    }
    
    if($('#cbInheritingSpatialInformation').is(':checked')) {
        inheritSpatialInformation(formId, json);
    }
    
    if($('#cbInheritingTemporalInformation').is(':checked')) {
        inheritTemporalInformation(formId, json);
    }
    
    if($('#cbInheritingOtherInformation').is(':checked')) {
        inheritOtherInformation(formId, json);
    }
}


/**
 * Testing Support
 */
// http://stackoverflow.com/questions/1038746/equivalent-of-string-format-in-jquery
function sprintf() {
    var s = arguments[0];
    for (var i = 0; i < arguments.length - 1; i++) {       
        var reg = new RegExp("\\{" + i + "\\}", "gm");             
        s = s.replace(reg, arguments[i + 1]);
    }
    return s;
}


function testify(formSelector) {
    var simpleInputs = $(":input:not([type=checkbox],[type=radio])", formSelector);
    var checkedInputs = $(":checked", formSelector);
    
    // get rid of struts checkbox state cruft
    simpleInputs = simpleInputs.map(function(ignored, elem){
        var attr = $(elem).attr('name');
        if(typeof attr == 'undefined') return null;
        if(attr.indexOf('__checkbox')==0)  return null;
        return elem;
    });
    console.log("HashMap<String,String> valMap = new HashMap<String,String>();");
    $.each(simpleInputs, function(index, elem) {
        if($(elem).val() && $(elem).val().length > 0) {
            var str = sprintf('valMap.put("{0}", "{1}");', $(elem).attr('name'), $(elem).val());
            console.log(str);
        }
    });
    
    $.each(checkedInputs, function(index,elem){
        var str = sprintf('valMap.put("{0}", "true"); //setting checkbox/radio', $(elem).attr('name'));
        console.log(str);
    });
    
}


/* ASYNC FILE UPLOAD SUPPORT */
function applyAsync(formId) {
	console.trace("apply async called");
    $(formId).fileUploadUI({
        multiFileRequest:true,  // FIXME: parts of this code aren't prepared for
								// request-per-file uploads yet.
        // dropZone:$('#divAsycFileUploadDropzone'),
        uploadTable: $('#uploadFiles'),
        fileInputFilter: $('#fileAsyncUpload'),
        downloadTable: $('#files'),
        url: getBaseURI() + "upload/upload",
        beforeSend: function initFileUpload(event, files, index, xhr, handler, callBack) {
            console.log('initFileUpload');
            if($('#ticketId').val()){
                sendFileIfAccepted(event, files, index, xhr, handler, callBack);
            } else {
                // grab a ticket and set the ticket id back to the hidden form
				// field
                var ticketUrl = getBaseURI() + "upload/grab-ticket";
                $.ajax({
                    url: ticketUrl,
                    dataType: 'json',
                    type: 'POST', //important!  otherwise browser may return cached ticketid
                    success: function(data) {
                        $('#ticketId').val(data.id);
                        sendFileIfAccepted(event, files, index, xhr, handler, callBack); 
                        }
                  });
            }
        },
        formData: getFormData,
        buildUploadRow: function (files, index) {
            console.log('building upload row');
            console.log(files);
            if (index) { 
                // browser doesn't support multi-file uploads, so this
                // callback called once per file
                return $('<tr><td>' + files[index].name + '<\/td>' +
                        '<td class="file_upload_progress"><div><\/div><\/td>' +
                        '<td class="file_upload_cancel">' +
                        '<button type="button" class="ui-state-default ui-corner-all" title="Cancel" onclick="return false;">' +
                        '<span class="ui-icon ui-icon-cancel">Cancel<\/span>' +
                        '<\/button><\/td><\/tr>');
            } else { 
                // browser supports multi-file uploads
                return $('<tr><td> Uploading ' + files.length + ' files<\/td>' +
                        '<td class="file_upload_progress"><div><\/div><\/td>' +
                        '<td class="file_upload_cancel">' +
                        '<button type="button" class="ui-state-default ui-corner-all" title="Cancel" onclick="return false;">' +
                        '<span class="ui-icon ui-icon-cancel">Cancel<\/span>' +
                        '<\/button><\/td><\/tr>');
            }
            
        },
        buildDownloadRow: function (jsonObject) {
          $("#files .noFiles").hide();
          var toReturn = "";
          var existingNumFiles = $('#files tr').not('.noFiles').length;
          console.debug("Existing number of files: " + existingNumFiles);
          for (var fileIndex=0; fileIndex < jsonObject.files.length; fileIndex++) {
            var row = $('#queuedFileTemplate').clone();
            row.find('*').each(function() {
                var elem = this;
                // skip any tags with the repeatRowSkip attribute
                $.each(["id", "onclick", "name", "for"], function(i,attrName){
                    replaceAttribute(elem, attrName, '{ID}', fileIndex + existingNumFiles);
                });
                
                $.each(["value", "onclick"], function(i,attrName){
                    replaceAttribute(elem, attrName, '{FILENAME}', jsonObject.files[fileIndex].name);
                });

                // nodeType=3 is hardcoded for "TEXT" node
                if ($(this).contents().length == 1 && $(this).contents()[0].nodeType == 3) {
                	var txt = $(this).text();
                	if (txt.indexOf("{FILENAME}") != -1) {
                		$(this).text($(this).text().replace(/\{FILENAME\}/g,jsonObject.files[fileIndex].name));
                	}
                	if (txt.indexOf("{FILESIZE}") != -1) {
                		$(this).text($(this).text().replace(/\{FILESIZE\}/g,jsonObject.files[fileIndex].size));
                	}
                }
            });

            toReturn += $(row).find("tbody").html();
          }
          return $(toReturn);
        },
        onComplete: function(event, files, index, xhr, handler) {
            showAsyncReminderIfNeeded();
            asyncUploadEnded();
            applyWatermarks();
            // FIXME: onError registration doesn't appear to be called even when
			// status <> 200;
            if(xhr.status && xhr.status != 200) {
                displayAsyncError(handler);
            }
            console.log("complete");
        },
        onError: function(event, files, index, xhr, handler) {
            asyncUploadEnded();
            // For JSON parsing errors, the load event is saved as
			// handler.originalEvent:
            if (handler.originalEvent) {
                /* handle JSON parsing errors ... */
                displayAsyncError(handler + " " + $(xhr));
                console.error("json parsing error");
                console.error(event);
                console.log(handler);
                console.log(files);
                console.log(event);
            } else {
                /* handle XHR upload errors ... */
                displayAsyncError(handler);
                console.error("xhr upload error");
                console.error(event);
            }
        },
        onAbort: function(event, files, index, xhr, handler) {
            asyncUploadEnded();
            handler.removeNode(handler.uploadRow);
        }
        
    });
    
    //Ensure that the ticketid is blank if there are no pending file uploads.  For example, a user begins uploading their first file,
    //but then either cancels the upload or the upload terminates abnormally. 
    $(formId).submit(function() {
        if($('tr', '#files').not('.noFiles').size() == 0) {
            $('#ticketId').val('');
        }        
    });
    
	console.log("apply async done");
}

function updateFileAction(rowId, value) {
    if (!value) {
        value = "MODIFY_METADATA";
    }
    var fileActionElement = $(rowId + " .fileAction");
    if (value == "MODIFY_METADATA") {
        var existingValue = fileActionElement.val();
        // no-op if it is an ADD/DELETE/REPLACE
        if ($.inArray(existingValue, ["ADD", "DELETE", "REPLACE"]) >= 0) {
            return;
        }
    }
    fileActionElement.val(value);
}

function deleteAsyncFileRow(rowId, newUpload, self) {
	var buttonText = $(self).find('.ui-button-text');
	// console.debug("button text is: " + buttonText.html());
	
	var fileAction = $(rowId + " .fileAction");
	if (buttonText.html() == 'delete') {
	    if(confirmAsyncFileRowDeletion(rowId, newUpload, self)) {
	        buttonText.html('undelete');
	        $(rowId + " .filename").addClass('deleted-file');
	        $(fileAction).attr("prev",fileAction.val());
	        fileAction.val(newUpload ? "NONE" : "DELETE");
	    }
	}
	else { //text is 'un-delete',  change to 'delete' and revert previous action.
		buttonText.html('delete');
		$(rowId + " .filename").removeClass('deleted-file');
		$(fileAction).val(fileAction.attr("prev"));
	}

	if ($("#files tr").length == 1) {
		$("#files .noFiles").show();
	}
}

function confirmAsyncFileRowDeletion(rowId, newUpload, self) {
    var reallyDelete = confirm('Are you sure?  Select OK to delete file, or CANCEL to retain this file.');
    console.log("answer to really delete:" + reallyDelete);
    var stacktrace="unavailable";
    var filename="unknown";
    try {stacktrace = printStackTrace(" --> ");}catch(ignored){}
    try {filename=$(self).closest('tr').find('.filename').text();}catch(ignored){}
    if(reallyDelete) {
        console.logRemote("Async Upload: user explicitly DELETED file at row:" + rowId + " name:" + filename + "stacktrace:" + stacktrace );
    } else {
        console.logRemote("Async Upload: user cancelled delete.  row:" + rowId + " name:" + filename + "stacktrace:" + stacktrace );
    }
    
    return reallyDelete;
}


function replaceFile(rowId,replacementRowId) {
    var row = $(rowId);
    var existingFilename = row.find(".filename").html();
    var replacementFilename = $(replacementRowId).find('.replacefilename').html();
    // message to let the user know that this file is being used to replace an existing file.
    $(replacementRowId).find("td:first").append("<div class='ui-state-default'><span class='tdar-ui-icon ui-icon ui-icon-info'></span>Replacing <b>" + existingFilename + "</b> with <b>" + replacementFilename + "</b>.</div>");
    // FIXME: simplify this logic through the use of effective CSS classes
    // clears out the delete button on the replacement row for the pending file
    $(replacementRowId).find("td:last").html("");
    // clear out all name attributes for the FileProxy hidden inputs on the replacement row
    $(replacementRowId).find("input").removeAttr("name");
    // clear out the replacement row's confidential checkbox div
    $(replacementRowId).find(".proxyConfidentialDiv").html("");
    row.find(".fileAction").val("REPLACE");
    // set the replacement filename on the existing FileProxy
    row.find(".fileReplaceName").val(replacementFilename);
    row.find("td:first").append("<div class='ui-state-default'><span class='tdar-ui-icon ui-icon ui-icon-info'></span>Replacing with <b>" + replacementFilename + "</b></div>");
}


function replaceDialog(rowId,filename) {
    var contents = "<b>select a file to replace:</b><br/><ul>";
    var replacementFiles = $('#files .newrow');
    if (replacementFiles.length == 0) {
        contents +="<li>Please upload a file and then choose the replace option</li>";
    }
    replacementFiles.each(function(i) {
            var replacementRowId = "#" + $(this).attr("id");
            var filename = $(this).find('.replacefilename').html();
            contents += "<li><input type='radio' name='replaceWith' value='"+replacementRowId+"'><span>"+filename+"</span></li>";
        });

    contents +="</ul>";
    var $dialog = $('<div />').html(contents).dialog({
            title: 'Replace File',
            buttons: {
                'replace': function() {
                    replaceFile(rowId , $("input:radio[name=replaceWith]:checked").val());
                    $(this).dialog('close'); 
                },
                'cancel': function() {
                    $(this).dialog('close');
                }
            }
        });
}
function initializeView() {
    applyZebraColors();
    $('.collapsible').click(function() {
            $(this).next().toggle('fast');
            return false;
        });
    initializeTooltips();
}

function applyZebraColors() {
    $('.zebracolors tbody tr:even').addClass("even");
    $('.zebracolors tbody tr:odd').addClass("odd");
}

/**
 * SINGLE INIT FUNCTION
 */
function initializeEdit() {
	// TODO: perftest on IE
	initializeRepeatRow();
	console.trace('applying institution auto completes');

	console.trace("applying annotation key autocomplete");
	console.trace("applying keywords autocomplete");
	applyKeywordAutocomplete('.annotationAutoComplete', 'annotationkey');
	applyAutoComplete(".nameAutoComplete");
	applyAutoComplete(".userAutoComplete",true);
	applyInstitutionAutoComplete(".institution");
	applyKeywordAutocomplete(".sitenameAutoComplete", "keyword", {keywordType:'SiteNameKeyword'});
	applyKeywordAutocomplete(".siteTypeKeywordAutocomplete", "keyword", {keywordType:'SiteTypeKeyword'});
	applyKeywordAutocomplete(".cultureKeywordAutocomplete", "keyword", {keywordType:'CultureKeyword'});
	applyKeywordAutocomplete(".temporalKeywordAutocomplete", "keyword", {keywordType:'TemporalKeyword'});
	applyKeywordAutocomplete(".otherKeywordAutocomplete", "keyword", {keywordType:'OtherKeyword'});
	applyKeywordAutocomplete(".geographicKeywordAutocomplete", "keyword", {keywordType:'GeographicKeyword'});
	initializeView();
	initializeInheritanceReminders();
}

function initializeInheritanceReminders() {
    $('a.moreInfoToggle').click(function() {
        //toggle the more/hide button, but just for the relevant section
        var elem = this;
        var div = $(elem).closest('.inheritanceExplanation');
        console.debug(div);
        $(".moreInfoToggle", div).toggle();
        $(".inheritanceMoreInfo", div).toggle('fast'); 
    });
}

function sessionTimeoutWarning() {
	// I RUN ONCE A MINUTE
	// sessionTimeout in seconds
	 currentTime += 60;
	 var remainingTime = sessionTimeout - currentTime;
	 if (remainingTime == 300) {
	  var dialog = $('<div id=timeoutDialog></div>')
	    .html("<B>Warning!</B><br/>Your session will timeout in 5 minutes, please save the document you're currently working on").dialog({
	      modal:true,
	      title:"Session Timeout Warning",
	      closeText:"Ok",
	      buttons: { "Ok": function() { $(this).dialog("close"); } } 
	   });
	 }
	 if ($("#timeoutDialog").length != 0 && remainingTime <= 0) {
	   $("#timeoutDialog").html("<B>WARNING!</B><BR>Your Session has timed out, any pending changes will not be saved");
	 } else {
	   setTimeout(sessionTimeoutWarning, 60000);
	 }
	}
