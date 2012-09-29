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
//
if(!window.JSON) JSON={}; 
JSON.stringify = JSON.stringify || function(){};



var TDAR = {};
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

function repeatRow(tableId,rowAddedCallback, resetRights) {
	 //FIXME: this business of optionally enabling the cloned row is screwing up existing stuff.  remove this option and break it out into a separate function.
	var _resetRights = true;
	if(typeof resetRights != 'undefined') {
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
    clearRow('#' + newRowId, _resetRights);
    // set focus on the first input field.
    
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
    	} catch (e) {console.log(e);}
    }
    
    $("input[type=text]:first", clonedRow).focus();
    return false;
}

function repeatRowPostCloneActions(clonedRow) {
    
    $.each(clonedRow.find("[watermark]"), 
        function(k,v) { $(this).watermark($(this).attr("watermark"));
      });

    
}

function initializeRepeatRow() {
	$("table.repeatLastRow").each(function(index) {
		var msg = "add another";
		if ($(this).attr('addAnother') != undefined) msg = $(this).attr('addAnother');
		var extraClass= "";
		if ($(this).hasClass("tableFormat"))extraClass = "normalTop";
		$(this).after("<button type=button  class='addAnother "+extraClass+"' onClick=\"repeatRow(\'" + this.id+"\')\"><img src='/images/add.gif'>" + msg + "</button>");
	});
    // create sidebar tooltips for any elements that have tooltipcontent
	// attribute
}

//FIXME: this business of optionally enabling the cloned row is screwing up existing stuff.  remove this option and break it out into a separate function.
function clearRow(rowId,resetRights) {
	if (resetRights == undefined) {
		resetRights = true;
	}
	try {
	      if (global_formNavigate != undefined) { global_formNavigate = false; }
		} catch(e){}
    // FIXME: do we need to renumber IDs afterwards if they delete from the
	// middle?
    $("input[type!=button],textarea", rowId).not('input[type=checkbox],input[type=radio]').each(function() {
    	$(this).val("");
    	if (resetRights) {
    		$(this).attr("readonly",false);
    		$(this).attr("disabled",false);
		}
    });
    
    $("button,input[type=button],select").each(function() {
    	if (resetRights) {
    		$(this).attr("readonly",false);
    		$(this).attr("disabled",false);
		}
    });
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
    	} catch (e) {console.log(e);}
    }
    
}

function deleteRow(rowId) {
	try {
      if (typeof global_formNavigate != 'undefined') { global_formNavigate = false; }
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
    if(sib=='') sib = getSiblingElement(element, 'user.id');
    
    console.debug("getPersId result:");
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
    setTimeout(function() {
    	if ($(":focus", $(e).parent().parent()).size() == 0) {
    	e.valid();
    }
    	
    }, 200);
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



function setToolTipContents(targetElem) {
	$targetElem = $(targetElem);
	var fieldOff = $targetElem.offset();
	var noteOff = $('#notice').offset();
	$('#notice').offset({left: noteOff.left,top: fieldOff.top});

	// tooltip content can either be in  'tooltipcontent' attribute or in a separate div
	var label = "";
	var content = "";
	if($targetElem.attr('tooltipcontent')) {
	    content = $targetElem.attr('tooltipcontent');
	    //tooltip label can either be in atttribute, otherwise will be set to the first h2
	    label = $targetElem.attr('tiplabel') || "";
	    if(label) {
	    	label = "<h2>" + label + "</h2>";
	    } 
	    if (content[0] == "#") {
	    	content = $(content).html();
	    }
	} else {
	    console.error("unable to bind tooltip - no tooltip element orr tooltipcontent found");
	}
	$('#notice').html(label + "<div id='noticecontent'>" +  content + "</div>");
}



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
	var intVal = parseInt(value);
	return (intVal == value && intVal > 1000 && intVal < 3000 );
}, "a date in the last millenia is expected");

$.validator.addMethod("isbn", function(value, element) {
	return value.match(/^(((\d+)-?(\d+)-?(\d+)-?([\dX]))|((978|979)-?(\d{9}[\dXx]))|)$/);
}, "you must include a valid 10/13 Digit ISBN");

$.validator.addMethod("issn", function(value, element) {
	return value.match(/^((\d{4})-?(\d{3})(\d|X|x)|)$/);
}, "you must include a valid 8 Digit ISSN");

$.validator.addMethod("descriptiveTitle",function(value, element) {
	return !value.match(/^(\s*)(dataset|collection|project|document|image|coding sheet|ontology)(\s*)$/i);
}, "please select a more descriptive title");

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
}, "A valid user record must contain a \"First Name\" and \"Last Name\".  If you do not wish to add or specify a user, leave all fields in this section blank.");

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

// enable the save button and replace it's former label (e.g. from 'please wait'
// to 'save')
function formSubmitEnable() {
    var oldVal = $('#submitButton').data('oldVal');
    // it's likely formSubmitDisable was called at least once before now, but
	// don't assume
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

// show the access rights reminder if any files are marked as confidential or if
// the resource is embargoed
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
var indexExclusions = ['investigationTypeIds', 'approvedSiteTypeKeywordIds', 'materialKeywordIds', 'approvedCultureKeywordIds'];

function populateSection(elem, formdata) {
	
    $(elem).populate(formdata, {resetForm:false, phpNaming:false, phpIndices:true, strutsNaming:true, noIndicesFor: indexExclusions});
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
                coverageDates: rawJson.coverageDates
            },
            otherInformation: {
                otherKeywords: $.map(rawJson.otherKeywords, function(v){return v.label;})
            }
    };

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
            repeatRow(id, null, false); 
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
        ,"cultureKeywords":[]
        ,"dateRegistered":{}
        ,"description": null
        ,"firstLatitudeLongitudeBox":null
        ,"geographicKeywords":[]
        ,"id":null
        ,"investigationTypes":[]
        ,"materialKeywords":[]
        ,"otherKeywords":[]
        ,"resourceType":null
        ,"siteNameKeywords":[]
        ,"siteTypeKeywords":[]
        ,"submitter":null
        ,"temporalKeywords":[]
        ,"coverageDates":[]
        ,"title": null
        ,"uncontrolledCultureKeywords":[]
        ,"uncontrolledSiteTypeKeywords":[]};
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

//return whether it's "safe" to populate the temporal information section with the supplied temporalInformation
//we define "safe" to mean that section is either currently blank or that the supplied temporalInformation is the same as what is already on the form.
function inheritingDatesIsSafe(rootElementSelector, temporalInformation) {
	//are all the fields in this section blank?
	var $coverageTextFields = $('input:text', '#coverageTable')
	var joinedFieldValues = $coverageTextFields.map(function(){return $(this).val();}).toArray().join("");
	
	 //okay to populate if if the form section is blank
	if(joinedFieldValues=="") return true;
	
	//not okay to populate if the incoming list is a different size as the current list
	$tableRows = $('tr', '#coverageTable');
	if(temporalInformation.coverageDates.length != $tableRows.length) return false; 
	
	//at this point it's we need to compare the contents of the form vs. incoming coverage dates
	var concatTemporalInformation = $.map(temporalInformation.coverageDates, function(val, i){return "" + val.startDate + val.endDate + val.description;}).join("");
	var concatRowFields = $.map($tableRows, function(rowElem, i){
		var concatRow = $('.coverageStartYear', rowElem).val();
		concatRow += $('.coverageEndYear', rowElem).val();
		concatRow += $('.coverageDescription', rowElem).val();
		return concatRow;
	}).join("");
	
	return concatTemporalInformation == concatRowFields;
	
}

function inheritInformation(formId, json,sectionId,tableId){
    disableSection(sectionId);
    clearFormSection(sectionId);
    if (tableId != undefined) {
    	if (document.getElementById("uncontrolled" + tableId +"Table" ) != undefined) {
            resetRepeatRowTable('uncontrolled'+tableId+'Table', json['uncontrolled'+tableId+'s'].length);
    	}
    	if (document.getElementById("approved" + tableId +"Table" ) != undefined) {
            resetRepeatRowTable('approved'+tableId+'Table', json['approved'+tableId+'s'].length);
    	}
    	var simpleId = tableId;
    	simpleId[0] = simpleId[0].toLowerCase();
    	if (document.getElementById(simpleId +"Table" ) != undefined) {
            resetRepeatRowTable(simpleId+'Table', json[simpleId+'s'].length);
    	}
    }
    populateSection(formId, json);
}

function inheritSiteInformation(formId, json){
    disableSection('#divSiteInformation');
    clearFormSection('#divSiteInformation');
    resetRepeatRowTable('siteNameKeywordTable', json.siteInformation['siteNameKeywords'].length);
    resetRepeatRowTable('uncontrolledSiteTypeKeywordTable', json.siteInformation['uncontrolledSiteTypeKeywords'].length);
    populateSection(formId, json.siteInformation);
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
    populateLatLongTextFields();  
}

function inheritTemporalInformation() {
//    function() {inheritInformation(formId, json.temporalInformation,"#divTemporalInformation","temporalKeyword");}
	var sectionId = '#divTemporalInformation';
    disableSection(sectionId);
    clearFormSection(sectionId);
    resetRepeatRowTable('temporalKeywordTable', json.temporalInformation.temporalKeywords.length);
    resetRepeatRowTable('coverageTable', json.temporalInformation.coverageDates.length);
    populateSection(formId, json.temporalInformation);
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

//update the 'public' latlong controls based on the values of the invisible latlong text fields
function populateLatLongTextFields() {
    $("#d_minx").val(Geo.toLon($("#minx").val()));
    $("#d_miny").val(Geo.toLat($("#miny").val()));
    $("#d_maxx").val(Geo.toLon($("#maxx").val()));
    $("#d_maxy").val(Geo.toLat($("#maxy").val()));
}

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

                populateLatLongTextFields();
                boundBox = true;
            }
        }
        );
    
        clearControl = new ClearControl();
        
        // hpcao adds this condition
        // based on the action (edit or view), we determine whether or
        // not to allow users to draw a bounding box for the latitude.
		// TODO: decide this at controller level, and then have ftl define a g_isEditing var
		var path = window.location.pathname;
		var action = path.substring(path.lastIndexOf("/") + 1);
        if(action.indexOf("edit") != -1 || action.indexOf("add") != -1 || action.indexOf("save") != -1
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
    };


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


function processLatLong(element) {
    var value = $(element).val();
    var id = $(element).attr('id');
//                value = value.replace(/([a-z]+)/ig,"");
    if (id.indexOf("d_") == 0) id = id.substring(2);
    $("#"+ id).val(Geo.parseDMS(value));
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
    var msgs = "";
    if(files[0].fileName) {
        var i = files.length -1;
        while(i >=0) {
        	var accepted_ = fileAccepted(files[i].fileName);
        	console.log(files[i].fileName +" : " + accepted_);
            if(accepted_ == false) {
                msgs += '<p>Sorry, this file type is not accepted:"'+files[i].fileName+'"</p>';
                files.splice(i,1);
            };
            i--;
        }
        if (files.length > 0) {
        	accepted=true;
        }
    }
    else { 
    	if (index == undefined) {
    		index =0;
    	}
    	console.log(index);
    	console.log(files);
        accepted = fileAccepted(files[index].name);
    }

    if(accepted) {
        asyncUploadStarted();
        callBack();
    } 
    if (!accepted || msgs != ""){
    	if (msgs == "") {
    		msgs = '<p>Sorry, this file type is not accepted: "' +files[index].name+'"</p>';
    	}
    	displayAsyncError(handler,msgs);
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
    formSubmitDisable();
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
    

    function applyPersonAutoComplete(selector,usersOnly, showCreate) {
            //if(selector=='.nameAutoComplete') {
                //$(selector).each(function(i,v) {
                    // console.log("registering:" + v);
                //});
            //}
    		var registered = undefined;
    		if (usersOnly) {
    			registered = true;
    		}
    	
            // for everything that matches this selector in this space
            // if selector does not have attribute autocomplete
            $(selector).autocomplete({
                source: function(request, response) {
                    console.trace('autocomplete called');
                    var lemail = (usersOnly) ? '' : getEmail(this.element).val();
                    var elem = this.element;
                    $.ajax({
                        url: getBaseURI() + "lookup/person",
                        dataType: "jsonp",
                        data: {
                            sortField: 'CREATOR_NAME',
                            email:  lemail,
                            firstName: getFirstName(this.element).val(),
                            lastName: getLastName(this.element).val(),
                            registered: registered,
                            institution: getInstitution(this.element).val()
                        },
                        success: function(data) {
                        	//user may have blurred this input before the server gave results. If so,  dismiss the popup.
                        	if(!$(elem).is(':focus')) {
                        		console.debug("input blurred before autocomplete results returned. returning no elements");
                        		response({});
                        		return;
                        	} 
                        	var values = $.map(data.people, function(item) {
                                if (item.institution == undefined) {
                                    item.institution = {};
                                    item.institution.name = '';
                                  }
                                  return {
                                      label: item.name,  // we're probably going
  														// to ignore this in
  														// custom rendering
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
                                      authid: item.id,
                                      properName: item.properName
                                  };
                              });
                        	if(showCreate) {
                        		values.push({
                        			label: 'ignored',
                        			value: request.term, 
                        			firstName: getFirstName(elem).val(),
                        			lastName: getLastName(elem).val(),
                        			email: lemail,
                        			authid: -1,
                        			properName: getFirstName(elem).val() + ' ' + getLastName(elem).val(),
                        			institution: getInstitution(elem).val()
                        		});
                        	}
                            response(values);
                        }
                    });
                },
                minLength: 3,
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
            }).each(function(idx, elem){
                $(elem).data( "autocomplete" )._renderItem = function( ul, item ) {
                	var htmlSnippet =  "<p style='min-height:4em'><img class='silhouette' src=\"" + getBaseURI() + "images/man_silhouette_clip_art_9510.jpg\" width=\"40\"/>"+
                	"<span class='name'>" + item.properName + "(" + item.email + ")</span>" +
                	"<br/><span class='institution'>"+ item.institution + "</span></p>";
                	if(item.authid == -1) {
                    	htmlSnippet =  "<p style='min-height:4em'><img class='silhouette' src=\"" + getURI("images/man_silhouette_clip_art_9510.jpg") + 
                    	"\" width=\"40\"/>"+
                    	"<span class='name'><em>Create a new person record</em></span> </p>";
                	}
                    return $( "<li></li>" )
                        .data( "item.autocomplete", item )
                        .append( "<a>" + htmlSnippet  + "</a>" )
                        .appendTo( ul );
                };
            });
               
    
    }; 

        // FIXME:ditch lookupType and instead pass querystring parms via data
    function applyKeywordAutocomplete(selector, lookupType, extraData, newOption) {
        console.log('applyKeywordAutocomplete:: selector:' + selector);
        var lookupUrl = getBaseURI() + "lookup/" + lookupType;
        console.trace("lookup url:" + lookupUrl);
        $(selector).autocomplete({
            source: function(request,response) {
                console.trace('keyword autocomplete');
                $.ajax({
                    url: lookupUrl,
                    dataType: "jsonp",
                    data: $.extend({term: request.term, sortField:'LABEL'}, extraData),
                    success: function(data) {
                        var values = $.map(data.items, function(item){
                            if(item.key) return {value:item.key, id:item.id};
                            else return {value:item.label, id:item.id};
                        });
                        if(newOption) {
                        	values.push({label: "(create new keyword: " + request.term + ")", id:-1, value:request.term});
                        }
                        response(values);
                    }
                });
            },
            minLength: 2
        });
    }

    function applyCollectionAutocomplete(selector, newOption) {
        var lookupUrl = getURI("lookup/collection");
        console.debug("lookup url:" + lookupUrl);
        $(selector).autocomplete({
            source: function(request,response) {
                $.ajax({
                    url: lookupUrl,
                    dataType: "jsonp",
                    data: {term: request.term },
                    success: function(data) {
                        console.debug("data returned:"  + JSON.stringify(data));
                        var values = $.map(data.collections, function(item){
                            if(item.name) return {value:item.name, id:item.id};
                            else return {value:item.name, id:item.id};
                        });;
                        //give the user the option to create a new collection w/ this name (TODO: unless name is taken?)
                        if(newOption) {
                        	values.push({id:-1, label:"(create new collection:'" + request.term + "')", value:request.term }); 
                        }
                        response(values);
                    }
                });
            },
            minLength: 2,
            select:function(event, ui){
            	//set the hidden id of the selected resource collection, and prep any extra fields if they are creating a new collection
            	var $elem = $(this);
            	var $tr = $elem.closest('tr');
            	setResourceCollection($tr, ui.item);
            	if(ui.item.id == -1) {
            		prepareAdhocCollectionRow($tr, ui.item);
            	}
            }
        });
    }
    
    function setResourceCollection($tr, item) {
    	console.debug('setting hidden id field in row: ');
    	$('input[type=hidden]', $tr).val(item.id);  //a new reosurceCollection will have an id of -1
    }

    //FIXME: to be honest, I'm punting here.  I have no idea what extra form fields you want to add when creating a new collection.  Maybe none?  
    function prepareAdhocCollectionRow($tr, item) {
    	console.debug('creating a new collection... prepare yourself!!!');
    }
    
    
    function applyInstitutionAutoComplete(selector,usersOnly, newOption) {
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
                            institution:  request.term,
                            sortField: 'CREATOR_NAME'
                        },
                        success: function(data) {
                        	var values = $.map(data.institutions, function(item) {
                                return {
                                    value:item.name,
                                    id:item.id
                                };
                            });
                        	if(newOption) {
                        		values.push({id:-1, label:"(create new institution:'" + request.term + "')", value:request.term }); 
                        	}
                            response(values);
                        }
                    });
                }
            },
            minLength: 2
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
                    data: {id: $(sel).val()},
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
        function() {inheritSiteInformation(formId, json);}
    );
    
    bindCheckboxToInheritSection(
        '#cbInheritingTemporalInformation',
        '#divTemporalInformation',
        function() {
            return inheritingRepeatRowsIsSafe('#temporalKeywordTable', json.temporalInformation.temporalKeywords)
                    && inheritingDatesIsSafe('#divTemporalInformation', json.temporalInformation);
        }, 
        function() {inheritTemporalInformation();}
    );

    bindCheckboxToInheritSection(
        '#cbInheritingCulturalInformation',
        '#divCulturalInformation',
        function() {return inheritingCheckboxesIsSafe('#divCulturalInformation', json.culturalInformation.approvedCultureKeywordIds) 
                            && inheritingRepeatRowsIsSafe('#divCulturalInformation', json.culturalInformation.uncontrolledCultureKeywords);
        },
        function() {inheritInformation(formId, json.culturalInformation,"#divCulturalInformation","CultureKeyword");}
    );

    bindCheckboxToInheritSection(
        '#cbInheritingOtherInformation',
        '#divOtherInformation',
        function() {return inheritingRepeatRowsIsSafe('#divOtherInformation', json.otherInformation.otherKeywords);},
        function() {inheritInformation(formId, json.otherInformation,"#divOtherInformation","otherKeyword");}
    );

    bindCheckboxToInheritSection(
        '#cbInheritingInvestigationInformation',
        '#divInvestigationInformation',
        function() {return inheritingCheckboxesIsSafe('#divInvestigationInformation', json.investigationInformation.investigationTypeIds);},
        function() {inheritInformation(formId, json.investigationInformation,'#divInvestigationInformation');}
    );

    bindCheckboxToInheritSection(
        '#cbInheritingMaterialInformation', 
        '#divMaterialInformation', 
        function(){
            return inheritingCheckboxesIsSafe('#divMaterialInformation', json.materialInformation.materialKeywordIds);
        }, 
        function() {inheritInformation(formId, json.materialInformation,'#divMaterialInformation');}
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
    
    // gzoom-control doesn't exist when this code fires on pageload. so we
	// wait a moment before trying checking to see if the google map controls
	// should be hidden
    setTimeout(function(e) {
        if($('#cbInheritingSpatialInformation').is(':checked')) {
            disableMap();
        }
    }, 100);
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
        project = getBlankProject();
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
    if(json && json.title && $.trim(json.title) != "") {
        labelText = 'Inherit values from parent project "' + TDAR.ellipsify(json.title, 60) + '"';
    }
    $('.inheritlabel label').text(labelText);
    
    // show or hide the text of each inheritable section based on checkbox
	// state.
    if($('#cbInheritingInvestigationInformation').is(':checked')) {
    	inheritInformation(formId, json.investigationInformation,'#divInvestigationInformation');
    }

    if($('#cbInheritingSiteInformation').is(':checked')) {
        inheritSiteInformation(formId, json);
    }
    
    if($('#cbInheritingMaterialInformation').is(':checked')) {
    	inheritInformation(formId, json.materialInformation,'#divMaterialInformation');
    }
    
    if($('#cbInheritingCulturalInformation').is(':checked')) {
    	inheritInformation(formId, json.culturalInformation,"#divCulturalInformation","CultureKeyword");
    }
    
    if($('#cbInheritingSpatialInformation').is(':checked')) {
        inheritSpatialInformation(formId, json);
    }
    
    if($('#cbInheritingTemporalInformation').is(':checked')) {
    	inheritTemporalInformation();
    }
    
    if($('#cbInheritingOtherInformation').is(':checked')) {
    	inheritInformation(formId, json.otherInformation,"#divOtherInformation","otherKeyword");
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
	$(formId).data('nextFileIndex',$('#files tr').not('.noFiles').length);
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
                $.post(ticketUrl, function(data) {
                    $('#ticketId').val(data.id);
                    sendFileIfAccepted(event, files, index, xhr, handler, callBack); // proceed
                                                                                        // w/
                                                                                        // upload
                }, 'json');
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
            var nextIndex = $(formId).data('nextFileIndex');
            console.debug("next file index: " + nextIndex);
            row.find('*').each(function() {
                var elem = this;
                // skip any tags with the repeatRowSkip attribute
                $.each(["id", "onclick", "name", "for", "value"], function(i,attrName){
                	//ensure each download row has a unique index.
                	replaceAttribute(elem, attrName, '{ID}', nextIndex);
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
                $(formId).data('nextFileIndex', nextIndex + 1);
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
    
    // Ensure that the ticketid is blank if there are no pending file uploads.
    // For example, a user begins uploading their first file,
    // but then either cancels the upload or the upload terminates abnormally.
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

function deleteFile(rowId, newUpload, self) {
	console.log("deleteFile called" + rowId + " : " + newUpload);
	var buttonText = $(self).find('.ui-button-text');
	// console.debug("button text is: " + buttonText.html());
	var fileAction = $(rowId + " .fileAction");
	if (buttonText.html() == 'delete') {
		buttonText.html('undelete');
		$(rowId + " .filename").addClass('deleted-file');
		$(fileAction).attr("prev",fileAction.val());
        fileAction.val(newUpload ? "NONE" : "DELETE");
	}
	else {
		buttonText.html('delete');
		$(rowId + " .filename").removeClass('deleted-file');
		$(fileAction).val(fileAction.attr("prev"));
	}

	if ($("#files tr").length == 1) {
		$("#files .noFiles").show();
	}
}

function replaceFile(rowId,replacementRowId) {
    var row = $(rowId);
    var existingFilename = row.find(".filename").html();
    var replacementFilename = $(replacementRowId).find('.replacefilename').html();
    // message to let the user know that this file is being used to replace an
	// existing file.
    $(replacementRowId).find("td:first").append("<div class='ui-state-default'><span class='tdar-ui-icon ui-icon ui-icon-info'></span>Replacing <b>" + existingFilename + "</b> with <b>" + replacementFilename + "</b>.</div>");
    // FIXME: simplify this logic through the use of effective CSS classes
    // clears out the delete button on the replacement row for the pending file
    $(replacementRowId).find("td:last").html("");
    // clear out all name attributes for the FileProxy hidden inputs on the
    // replacement row
    $(replacementRowId).find("input").removeAttr("name");
    // clear out the replacement row's confidential checkbox div
    $(replacementRowId).find(".proxyConfidentialDiv").html("");
    row.find(".fileAction").val("REPLACE");
    // set the replacement filename on the existing FileProxy
    row.find(".fileReplaceName").val(replacementFilename);
    row.find("td:first").append("<div class='ui-state-default'><span class='tdar-ui-icon ui-icon ui-icon-info'></span>Replacing with <b>" + replacementFilename + "</b></div>");
    var table = row.parent();
    table.find(".fileSequenceNumber").each(function(index) {
        $(this).val(index);
    });
}


function replaceDialog(rowId,filename) {
    var contents = "<b>Select the Newly Uploaded File That Should Replace The Existing File:</b><br/><ul>";
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
	console.debug('initialize view');
    applyZebraColors();
    registerMoreInfoText();
    if (typeof formId != "undefined") {
    	console.debug('delegating tooltips')
    	$(formId).delegate("[tooltipcontent]","mouseenter",function(){setToolTipContents(this);});
    	$(formId).delegate("[tooltipcontent]","focusin",function(){setToolTipContents(this);});
    }
}

function applyZebraColors(optionalRoot) {
	var root = document;
	if(optionalRoot) root = optionalRoot;
	
    $('table.zebracolors tbody tr:even', root).addClass("even");
    $('table.zebracolors tbody tr:odd', root).addClass("odd");
}

/**
 * SINGLE INIT FUNCTION
 */
function initializeEdit() {
	//if user gets to the edit page by clicking the 'back' button the submit button may be disabled. 
	$("#submitButton").removeAttr('disabled');

		/// this is for the backwards and forwards page cache
	$(window).bind("pageshow", function() {
		  $("#submitButton").removeAttr('disabled');
		  });

	// TODO: perftest on IE
	initializeRepeatRow();


	delegateCreator("#authorshipTable",false,true);
	delegateCreator("#creditTable",false,true);
	delegateCreator("#divAccessRights",true,false);
	
	delegateAnnotationKey("#resourceAnnotationsTable","annotation","annotationkey");
	delegateKeyword("#siteNameKeywordTable","sitename","SiteNameKeyword");
	delegateKeyword("#uncontrolledSiteTypeKeywordTable","siteType","SiteTypeKeyword");
	delegateKeyword("#uncontrolledCultureKeywordTable","culture","CultureKeyword");
	delegateKeyword("#temporalKeywordTable","temporal","TemporalKeyword");
	delegateKeyword("#otherKeywordTable","other","OtherKeyword");
	delegateKeyword("#geographicKeywordTable","geographic","GeographicKeyword");
	
    applyInstitutionAutoComplete('#txtResourceProviderInstitution', false, true);
	initializeView();
	// FIXME: change to delegate model
	initializeInheritanceReminders();

	$('#resourceCollectionTable').delegate(".collectionAutoComplete","focusin",function(){
    	applyCollectionAutocomplete("#resourceCollectionTable .collectionAutoComplete", true);         
});

}


function delegateCreator(id, user, showCreate) {
	if (user == undefined || user == false) {
		$(id).delegate(".nameAutoComplete","focusin",function(){
		    // TODO: these calls re-regester every row after a row is created,
            // change so that only the new row is registered.
			applyPersonAutoComplete(id + " .nameAutoComplete",false, showCreate);
		});
		$(id).delegate(".institutionAutoComplete","focusin",function(){
			applyInstitutionAutoComplete(id + " .institution", true, true);
		});
	} else {
		$(id).delegate(".userAutoComplete","focusin",function(){
			applyPersonAutoComplete(id + " .userAutoComplete",true,false);
		});
	}
}


// fixme: instead of focusin, look into using a customEvent (e.g. 'rowCreated')
function delegateAnnotationKey(id, prefix, delim) {
    $(id).delegate("."+prefix+"AutoComplete","focusin",function(){
            applyKeywordAutocomplete("."+prefix+"AutoComplete", delim, true);         
    });
}

function delegateKeyword(id,prefix,type) {
	$(id).delegate(".keywordAutocomplete","focusin",function(){
            // TODO: these calls re-regester every row after a row is created,
            // change so that only the new row is registered.
	        console.log('focusin:' + this.id);
			applyKeywordAutocomplete(id + " .keywordAutocomplete", "keyword", {keywordType:type}, true);
	});
	
}

function initializeInheritanceReminders() {
    $('a.moreInfoToggle').click(function() {
        // toggle the more/hide button, but just for the relevant section
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


function setupEditForm(formId,acceptedFiles) {
    $(formId).FormNavigate("Leaving the page will cause any unsaved data to be lost!"); 


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
    
    // Watermark labels *must* be registered before validation rules are
	// applied, otherwise you get nasty conflicts.
    applyWatermarks();
    $(formId).validate({
        errorLabelContainer: $("#error"),
        onkeyup: function() {return ;},
        onclick: function() {return;},
        onfocusout: function(element) {
        return ;
        // I WORK IN CHROME but FAIL in IE & FF
        // if (!dialogOpen) return;
        // if ( !this.checkable(element) && (element.name in this.submitted ||
		// !this.optional(element)) ) {
        // this.element(element);
        // }
        },
        invalidHandler: $.watermark.showAll,
        showErrors: function(errorMap, errorList) {
          this.defaultShowErrors();
          if (errorList != undefined && errorList.length > 0 && this.submitted) {
              dialogOpen = true;
            $("#error").clone().dialog({
              title: 'Please correct the following issues before saving',
              buttons: { "Ok": function() { dialogOpen=false;$(this).dialog("close"); } },
              dialogClass:'errorDialog',
              resizable:false,
              draggable:false
            });
          }
        },
        submitHandler: function(f) {
            //prevent multiple form submits (e.g. from double-clicking the submit button)
            $('input[type=submit]', f).attr('disabled', 'disabled');
            f.submit();
        }
    });

    // trim any type-converted fields prior to submit
    $(formId).submit(function() {
        try {
            $.each($('.reasonableDate, .coverageStartYear, .coverageEndYear, .date, .number'), function() {
                if($(this).val() == undefined || $(this).val() == "") return; 
                // this is essential, or IE will replace null values w/
				// empty-string values, and type-conversion dies.
                var elem = this;
                $(elem).val($.trim($(elem).val()));
            });
        } catch(err){
            console.error("unable to trim:" + err);
        }
        return true;
    });
    
    var uploadField = document.getElementById("fileUploadField");
    if (uploadField != undefined) {
	    var validate = $(uploadField);
	        $(validate).rules("add", {
	            accept: acceptedFiles,
	            messages: {
	                accept: "Please enter a valid file ("+acceptedFiles.replace(/\|/ig,", ")+")"
	            }
	        });
    }
    
    $('.coverageTypeSelect',"#coverageTable").each(function(i, elem){
        prepareDateFields(elem);
    });
    
    if($(formId + '_uploadedFiles').length>0) {
        console.trace("wiring up uploaded file check");
        var validateUploadedFiles = function() {
            if ($(formId + "_uploadedFiles").val().length > 0) {
                $("#reminder").hide();
            }
        };
        $(formId +'_uploadedFiles').change(validateUploadedFiles);
        validateUploadedFiles();
    }	
    

    // FIXME: see if we can sniff this from browser feature instead of browser
	// version
    if ($.browser.msie || $.browser.mozilla && $.browser.mozilla < 4 ) {
        $('textarea.resizable:not(.processed)').TextAreaResizer();
    }

    $("#coverageTable").delegate(".coverageTypeSelect","change",function(){console.log('called delegate');prepareDateFields(this);});
    showAccessRightsLinkIfNeeded();
    $('#cbConfidential').click(showAccessRightsLinkIfNeeded);
    $('#resourceAvailability').change(showAccessRightsLinkIfNeeded);

}

function setupDocumentEditForm() {
	// wire up document-type selection to dependent fields
	    $("#showmorecite").hide('slow');
	    $("#show-more-types").hide();
	    $("#link-more").click(function() { $('#showmorecite').show('show'); $('#showCite').hide(); return false; });
	    $('#documentTypeBOOK').click(function(){switchType("book");});
	    $('#documentTypeBOOK_SECTION').click(function(){switchType("book_section");});
	    $('#documentTypeJOURNAL_ARTICLE').click(function(){switchType("journal_article");});
	    $('#documentTypeMANUSCRIPT').click(function(){switchType("journal_article");}); // TODO:remove
	    $('#documentTypeTHESIS').click(function(){switchType("thesis");});
	    $('#documentTypeCONFERENCE_PRESENTATION').click(function(){switchType("conference");});
	    $('#documentTypeOTHER').click(function(){switchType("other");});
	    switchType($("input[@name='document.documentType']:radio:checked").val());
}

function switchType(doctype) {
    try{doctype=doctype.toLowerCase();}catch(ex){}
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
    $("#t-edition").hide();
    $("#t-institution").hide();
 
    if(doctype == 'book_section') {$("#t-title2-book").show();}
    if(doctype == 'journal_article') {$("#t-title2-journal").show();}
    switchLabel($("#publisher-hints"),doctype);
    switchLabel($("#publisherLocation-hints"),doctype);
 
 
    if (doctype == 'book_section') {
        $("#t-title2").show();
        $("#t-isbn").show();
        $("#t-start-end").show();
    }
    if (doctype == 'book_section' || doctype == 'book' || doctype== 'book_section') {
        $("#t-series").show();
        $("#t-isbn").show();
        $("#t-pub").show();
        $("#t-edition").show();
    }
 
    if ( doctype == 'book_section' || doctype == 'book_section') {
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
        $("#t-edition").show();
    }
 
    if (!$('#title2').is(':hidden')) {
        $('#title2').addClass("required");
    } else {
        $('#title2').removeClass("required");
    }    
    
    // console.debug('switchType:end:' + doctype);
    
}

function switchLabel(field,type) {
    // console.debug('switchLabel('+field+','+type+')');
    var label = "#" + $(field).attr('id') + '-label';
    if($(field).attr(type) != undefined && $(label) != undefined) {
        $(label).text($(field).attr(type));
    }
}

function toggleDiv() {
		 $(this).next().slideToggle('slow');
		 $(this).find("span.ui-icon-triangle-1-e").switchClass("ui-icon-triangle-1-e","ui-icon-triangle-1-s",700);
		 $(this).find("span.ui-icon-triangle-1-s").switchClass("ui-icon-triangle-1-s","ui-icon-triangle-1-e",700);
}

function setupSupportingResourceForm(totalNumberOfFiles, rtype) {
    // the ontology textarea or file upload field is required whenever it is
    // visible AND
    // no ontology rules are already present from a previous upload

    $('#fileInputTextArea').rules("add", {
        required: {depends:isFieldRequired},
        messages: {required: "No "+rtype+" data entered. Please enter "+rtype+" manually or upload a file."}
    });

    $('#fileUploadField').rules("add", {
        required: {depends:isFieldRequired},
        messages: {required: "No "+rtype+" file selected. Please select a file or enter "+rtype+" data manually."}
    });
    
    function isFieldRequired(elem) {
        var noRulesExist =  !( (totalNumberOfFiles > 0) || 
        ($("#fileInputTextArea").val().length > 0 ) ||
        ($("#fileUploadField").val().length > 0));
        return noRulesExist && $(elem).is(":visible");
    }
    
    refreshInputDisplay();
  }

//register 'more-info' sections
function registerMoreInfoText() {
    $('a.morify-toggle').click(function(){
        console.debug('registering click handler');
        var $elem = $(this);
        $elem.text($elem.text() == '(show more)' ? '(show less)' : '(show more)');  //sometimes less is more.
        var root = $elem.closest('.morify-root');
        $('.ellipses', root).toggle();
        $('.morify-remaining', root).toggle('slow');
    });
}


function makeMap(json,mapId,type, value_) {
  var jsonString = json;
  var jsonObj = false;
  var mapString = "";

  if (!json.chartshape) {
    alert("No map elements");
    return;
  }
  mapString = "<map name='"+mapId+"'>";
  var area = false;
  var chart = json.chartshape;
  var values = value_.split("|");
  for (var i = 0; i < chart.length; i++) {
    area = chart[i];
    mapString += "\n  <area name='"  + area.name + "' shape='"  + area.type
      + "' coords='" + area.coords.join(",");
    var val = values[i];

    //FIXME: I don't always consistently work
    //    var offset = values.length - 1;
	//    if (val == undefined && i >= offset && values[i-offset] != undefined) {
	//    	val = values[(i-offset)];
	//    }
	//    console.log(values.length + ' ' + i + "{"+ (i -offset)+ "}" + ' ' + values[(i-offset)]);
    if (val != undefined) {
    	mapString += "' href='" + getURI("search/results") +"?"+type+"=" +val + "&useSubmitterContext=true'";
    }
    mapString += " title='"+val+"'>";;
  }
  mapString += "\n</map>";
  $("#" + mapId +"-img").after(mapString);
}

