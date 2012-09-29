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
console.trace = function() {};
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

var TDAR = {};
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
		console.trace("comparArray: " + arrayA + " vs. " + arrayB);
		if (arrayA.length != arrayB.length) {
			return false;
		}
		// ignore order by default
		if (typeof (ignoreOrder) == 'undefined')
			ignoreOrder = true;
		var a = arrayA, b = arrayB;
		if (ignoreOrder) {
			console.trace("comparArray: ignoring order");
			a = jQuery.extend(true, [], arrayA);
			b = jQuery.extend(true, [], arrayB);
			a.sort();
			b.sort();
		}
		for ( var i = 0, l = a.length; i < l; i++) {
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
		console.trace("row added, calling callback with:" + clonedRowId);
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

	$("input[type=text]:visible:first", clonedRow).focus();
	return false;
}

function repeatRowPostCloneActions(clonedRow) {

	$.each(clonedRow.find("[watermark]"), function(k, v) {
		$(this).watermark($(this).attr("watermark"));
	});

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

$.validator.addMethod("latLong", function(value, element) {
	return value.match(/^(((\-?)(\d+)(\.?)(\d*))|)$/);
}, "a valid lat/long in the format DEG.Min/Sec (eg. -67.892068) required");

$.validator.addMethod("reasonableDate", function(value, element) {
	var intVal = parseInt(value);
	// -1 is allowable
	return (intVal == value && (intVal == -1 || intVal > 1000 && intVal < 3000));
}, "a date in the last millenia is expected");

$.validator.addMethod("isbn", function(value, element) {
	if($(element).is(':hidden')) return true; //skip validation if not showing
	return value.match(/^(((\d+)-?(\d+)-?(\d+)-?([\dX]))|((978|979)-?(\d{9}[\dXx]))|)$/);
}, "you must include a valid 10/13 Digit ISBN");

$.validator.addMethod("issn", function(value, element) {
	if($(element).is(':hidden')) return true;//skip validation if not showing
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
				"This record does not contain a valid, registered tDAR user.  If you do not wish to add or specify a user, leave all fields in this section blank.");

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

// $.validator's built-in number rule does not accept leading decimal points (e.g.'.12' vs. '0.12'), so we replace with our own 
$.validator.addMethod('number', function(value, element){
	return this.optional(element) || /^-?(?:\d+|\d{1,3}(?:,\d{3})+)?(?:\.\d+)?$/.test(value);
}, $.validator.messages.number); 

// $.validator.addClassRules("radiocarbonDate", {range:[0,100000]});
// $.validator.addClassRules("julianYear", {range:[-99900, 2100]});

// called whenever date type changes
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
							"images/bookmark.gif",newtext);
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
					"images/unbookmark.gif",newtext);
		}

	});
}

function updateBookmarkTag($elem, url, strOnclick, imgSrc, txt) {
	if (txt != undefined & txt != '') {
		txt = "<span class='bookmark'>&nbsp;" + txt + "</span>";
	} else {
		txt = "";
	}
	var newElem = $("<a href='" + getURI(url) + "' onclick='" + strOnclick
			+ "; return false;'><img src='" + getURI(imgSrc) + "' />"+txt+"</a>");
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

function applyWatermarks() {
	$("input[watermark]").each(function() {
		$(this).watermark($(this).attr("watermark"));
	});
}

// show the access rights reminder if any files are marked as confidential or if
// the resource is embargoed
function showAccessRightsLinkIfNeeded() {
	if ($('#cbConfidential').is(':checked')
			|| $(".fileProxyConfidential:checked").length > 0
			|| $('#resourceAvailability').val() == 'Embargoed') {
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
GEvent
		.addDomListener(
				window,
				'load',
				function() {
					// alert("action="+action);
					if (GBrowserIsCompatible()
							&& document.getElementById("large-google-map") != undefined) {
						map = new GMap2(document
								.getElementById("large-google-map"));
						// alert("load map"+map);
						// map.setCenter(new GLatLng(37.4419, -122.1419), 13);
						// add terrain map type as well.
						map.addMapType(G_PHYSICAL_MAP);
						// add the controls
						map.addControl(new GLargeMapControl());
						var ovmap = new GOverviewMapControl();
						ovmap.setMapType(G_PHYSICAL_MAP);
						GEvent.addListener(map, 'maptypechanged', function() {
							ovmap.setMapType(map.getCurrentMapType());
						});
						map.addControl(ovmap);
						map.addControl(new GMapTypeControl());
						map.addControl(new GScaleControl()); // hpcao added
						// 2008-04-30

						gzControl = new GZoomControl(
								/*
								 * first set of options is for the visual
								 * overlay.
								 */
								{
									nOpacity : .2,
									sBorder : "2px solid red"
								},
								/* second set of optionis is for everything else */
								{
									sButtonHTML : "<div id='selectARegion'>Select Region</div>",
									sButtonZoomingHTML : "<div id='selectARegion'>Select Region</div>"
								},

								/* third set of options specifies callbacks */
								{
									buttonClick : function() {
									},
									dragStart : function() {
									},
									dragging : function(x1, y1, x2, y2) {
									},
									dragEnd : function(nw, ne, se, sw, nwpx,
											nepx, sepx, swpx) {
										// hpcao changed this after Allen
										// changed the name of the four
										// spatial coordinates
										$("#minx").val(sw.lng());
										$("#miny").val(sw.lat());
										$("#maxx").val(ne.lng());
										$("#maxy").val(ne.lat());

										populateLatLongTextFields();
										boundBox = true;
									}
								});

						clearControl = new ClearControl();

						// TODO: decide this at controller level, and then have
						// ftl define a g_isEditing var
						var path = window.location.pathname;
						var action = path.substring(path.lastIndexOf("/") + 1);
						if (action.indexOf("edit") != -1
								|| action.indexOf("add") != -1
								|| action.indexOf("save") != -1
								|| path.indexOf("search") != -1) {
							map.addControl(gzControl, new GControlPosition(
									G_ANCHOR_BOTTOM_LEFT, new GSize(5, 85)));
							map.addControl(clearControl, new GControlPosition(
									G_ANCHOR_BOTTOM_LEFT, new GSize(5, 45)));
						}
						// set the starting location and zoom

						map.setCenter(new GLatLng(40, -97.00), 4,
								G_PHYSICAL_MAP); // other

						map.enableDoubleClickZoom();
					}// end of if

					drawMBR(); // does this need to be here?
					drawMBR("p_", "#996633");

				});

function ClearControl() {
}
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
		} catch (e) {
		}
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
function locateCoords() {
	var minx = document.getElementById("minx").value;
	var miny = document.getElementById("miny").value;
	var maxx = document.getElementById("maxx").value;
	var maxy = document.getElementById("maxy").value;
	var minx1 = parseFloat(minx.replace(/^\s*|\s*$/g, ""));
	var miny1 = parseFloat(miny.replace(/^\s*|\s*$/g, ""));
	var maxx2 = parseFloat(maxx.replace(/^\s*|\s*$/g, ""));
	var maxy2 = parseFloat(maxy.replace(/^\s*|\s*$/g, ""));

	if (minx1 == null || minx1 == "" || miny1 == null || miny1 == ""
			|| maxx2 == "" || maxx2 == null || maxy2 == null || maxy2 == "") {
		alert("Please fill all four coordinate fields");
	} else {
		// alert("call make selection...");
		makeSelection(minx1, miny1, maxx2, maxy2);
	}
}
function makeSelection(x1, y1, x2, y2) {
	// alert ("makeSelection...");

	document.getElementById("minx").value = x1;
	document.getElementById("miny").value = y1;
	document.getElementById("maxx").value = x2;
	document.getElementById("maxy").value = y2;

	// draw a red box on top of the map
	var pts = [];
	pts[0] = new GLatLng(y1, x1);
	pts[1] = new GLatLng(y1, x2);
	pts[2] = new GLatLng(y2, x2);
	pts[3] = new GLatLng(y2, x1);
	pts[4] = new GLatLng(y1, x1);
	// alert ("makeSelection 2 ...");

	var G = GZoomControl.G;
	if (G.oZoomArea != null)
		G.oMap.removeOverlay(G.oZoomArea);

	// alert (G.style.sOutlineColor);
	G.oZoomArea = new GPolyline(pts, G.style.sOutlineColor,
			G.style.nOutlineWidth + 1, .4);

	var bounds = new GLatLngBounds();
	bounds.extend(pts[0]);
	bounds.extend(pts[1]);
	bounds.extend(pts[2]);
	bounds.extend(pts[3]);
	map.setZoom(map.getBoundsZoomLevel(bounds));

	map.panTo(new GLatLng((y1 + y2) / 2, (x1 + x2) / 2));
	map.addOverlay(G.oZoomArea);
}

// draw a red box on top of the map
function drawMBR(prefix, colour) {
	// x1 = min longitude
	// x2 = max longitude
	// y1 = min latitude
	// y2 = max latitude
	if (document.getElementById("large-google-map") == undefined)
		return;
	if (prefix == undefined)
		prefix = "";

	// make sure that the form name of the document is
	// "resourceRegistrationForm"
	// and it has minx, miny, maxx, and maxy
	try {
		var x1 = parseFloat(document.getElementById(prefix + "minx").value
				.replace(/^\s*|\s*$/g, ""));
		var y1 = parseFloat(document.getElementById(prefix + "miny").value
				.replace(/^\s*|\s*$/g, ""));
		var x2 = parseFloat(document.getElementById(prefix + "maxx").value
				.replace(/^\s*|\s*$/g, ""));
		var y2 = parseFloat(document.getElementById(prefix + "maxy").value
				.replace(/^\s*|\s*$/g, ""));

		if (isNaN(x1) || isNaN(y1) || isNaN(x2) || isNaN(y2))
			return;

		// alert("is NOT NaN");
		var pts = [];
		pts[0] = new GLatLng(y1, x1);
		pts[1] = new GLatLng(y1, x2);
		pts[2] = new GLatLng(y2, x2);
		pts[3] = new GLatLng(y2, x1);
		pts[4] = new GLatLng(y1, x1);

		// alert("hi1");
		var G = GZoomControl.G;
		if (colour == undefined)
			colour = G.style.sOutlineColor;
		var zoomarea = new GPolyline(pts, colour, G.style.nOutlineWidth + 1, .4);

		if (prefix == "") {
			try {
				if (G.oZoomArea != null)
					G.oMap.removeOverlay(G.oZoomArea);
			} catch (e) {
			}
			G.oZoomArea = zoomarea;
		}

		var bounds = new GLatLngBounds();
		bounds.extend(pts[0]);
		bounds.extend(pts[1]);
		bounds.extend(pts[2]);
		bounds.extend(pts[3]);
		map.setZoom(map.getBoundsZoomLevel(bounds));

		map.panTo(new GLatLng((y1 + y2) / 2, (x1 + x2) / 2));
		map.addOverlay(zoomarea);
	} catch (e) {
	}
}

function processLatLong(element) {
	var value = $(element).val();
	var id = $(element).attr('id');
	// value = value.replace(/([a-z]+)/ig,"");
	if (id.indexOf("d_") == 0)
		id = id.substring(2);
	$("#" + id).val(Geo.parseDMS(value));
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

function applyTreeviews() {
	// FIXME: There is a bug in the Treeview plugin that causes the
	// 'expand/collapse' icon to not to not show when the last item in the list
	// has sublist.
	// so we tack on an invisible LI and it magically shows up again.
	$(
			'#approvedSiteTypeKeywordIds_Treeview,#approvedCultureKeywordIds_Treeview')
			.append('<li style="display:none !important">&nbsp</li>');

	$("#approvedSiteTypeKeywordIds_Treeview").treeview({
		collapsed : true,
		persist : "cookie",
		cookieId : "tdar-treeview-site-type"
	});

	$("#approvedCultureKeywordIds_Treeview").treeview({
		collapsed : true,
		persist : "cookie",
		cookieId : "tdar-treeview-culture"
	});

	// expand those nodes where children are selected, this was in an "onReady"
	// function, but not sure it needs to be there
	$(".treeview input:checked").parents(".expandable").children(".hitarea")
			.trigger("click");

}

function buildRequestData(element) {
	var data = {};
	console.trace("autocompleteParentElement: "
			+ element.attr("autocompleteParentElement"));
	if (element.attr("autocompleteParentElement")) {
		$("[autocompleteName]", element.attr("autocompleteParentElement"))
				.each(
						function(index, val) {
							var $val = $(val);
							data[$val.attr("autocompleteName")] = $val.val();
							console.trace("autocompleteName: "
									+ $val.attr("autocompleteName") + "=="
									+ $val.val());
						});
	}
	console.trace(data);
	return data;
}

function applyDataElements(element, item) {
	var $element = $(element);
	if ($element.attr("autocompleteParentElement") != undefined) {
		$("[autocompleteName]", $element.attr("autocompleteParentElement"))
				.each(function(index, val) {
					var $val = $(val);
					var newvalue = item[$val.attr("autocompleteName")];
					if (newvalue != undefined) {
						var valueToSet = newvalue;
						if (newvalue.constructor === String) {

						} else {
							if (newvalue['name'] != undefined) {
								valueToSet = newvalue['name'];
							}
							if (newvalue['label'] != undefined) {
								valueToSet = newvalue['label'];
							}
						}
						
						$val.val(valueToSet);
						 console.trace("setting: " + val.name +  "["+$val.attr("autocompleteName")+"]" + ":" + valueToSet);
						$val.attr("autoVal", valueToSet);
					}
				});
		if ($element.attr("autocompleteName") != undefined) {
			item.value = $element.attr("autoVal");
		}
		var $idElement = $($element.attr("autocompleteIdElement"));

		if (item["id"] != undefined) {
			$idElement.val(item["id"]);
		}
	}
}

/**
 * Autocomplete Support
 */

function applyPersonAutoComplete(selector, usersOnly, showCreate) {
	var options = {};
	options.url = "lookup/person";
	options.dataPath = "data.people";
	options.retainInputValueOnSelect = true;
	options.sortField = 'CREATOR_NAME';
	options.showCreate = showCreate;
	options.minLength = 3;

	options.enhanceRequestData = function(requestData) {
		if (usersOnly) {
			requestData.registered = true;
		}
		// var lemail = (usersOnly) ? '' : getEmail(
		// this.element).val();
	};

	options.customRender = function(ul, item) {
		console.trace(item);
		var institution = "";
		if (item.institution != undefined && item.institution.name != undefined) {
			institution = item.institution.name;
		}
		var htmlSnippet = "<p style='min-height:4em'><img class='silhouette' src=\""
				+ getBaseURI()
				+ "images/man_silhouette_clip_art_9510.jpg\" width=\"40\"/>"
				+ "<span class='name'>"
				+ htmlEncode(item.properName)
				+ "("
				+ htmlEncode(item.email)
				+ ")</span><br/><span class='institution'>"
				+ htmlEncode(institution)
				+ "</span></p>";
		if (item.id == -1 && options.showCreate) {
			htmlSnippet = "<p style='min-height:4em'><img class='silhouette' src=\""
					+ getURI("images/man_silhouette_clip_art_9510.jpg")
					+ "\" width=\"40\"/>"
					+ "<span class='name'><em>Create a new person record</em></span> </p>";
		}
		return $("<li></li>").data("item.autocomplete", item).append(
				"<a>" + htmlSnippet + "</a>").appendTo(ul);
	};
	applyGenericAutocomplete(selector, options);
}
function evaluateAutocompleteRowAsEmpty(element, minCount) {
	var req = buildRequestData($(element));
	var total = 0;
	//FIXME:  I think 'ignored' is irrelevant as defined here.  Can we remove this?
	var ignored = new Array();
	if (minCount != undefined) {
		ignored = minCount;
	}

	var $idElement = $($(element).attr("autocompleteIdElement"));
	var allowNew = $idElement.attr("allowNew");

	var nonempty = 0;
	for ( var p in req) {
		total++;
		if ($.inArray(p, ignored) == -1 && req[p] != undefined && req[p] != '') {
			nonempty++;
		}
	}
	console.trace("req size:" + total + " nonEmpty:" + nonempty + " ignored:"
			+ ignored);
	if (nonempty == 0) {
		return true;
	}

	if (allowNew != undefined && allowNew == "true"
			&& ($idElement.val() == "" || $idElement.val() == -1)) {
		return true;
	}

	return false;
}

function applyGenericAutocomplete(selector, options) {
	var $selector = $(selector);

	// if there's a change in the autocomplete, reset the ID to ""
	$selector
			.change(function() {
				var $element = $(this);
				if (($element.attr("autoVal") != undefined && $element
						.attr("autoVal") != $element.val())
						|| evaluateAutocompleteRowAsEmpty(
								this,
								options.ignoreRequestOptionsWhenEvaluatingEmptyRow == undefined ? []
										: options.ignoreRequestOptionsWhenEvaluatingEmptyRow)) {

					var $idElement = $($element.attr("autocompleteIdElement"));
					$idElement.val("");
				}
				return true;
			});

	if (options.showCreate) {
		var $idElement = $($(this).attr("autocompleteIdElement"));
		$idElement.attr("allowNew", "true");
	}

	var autoResult = $selector
			.autocomplete({
				source : function(request, response) {
					var $elem = $(this.element);
					
					//is another ajax request in flight?
					var oldResponseHolder = $elem.data('responseHolder');
					if(oldResponseHolder) {
						//cancel the previous search
						console.trace("cancelling previous search");
						oldResponseHolder.callback({}); 
						
						//swap out the no-op before the xhrhhtp.success callback calls it
						oldResponseHolder.callback = function(){
							console.trace("an ajax success callback just called a now-defunct response callback");
						};
					}

					var requestData = {};
					// add requestData that's passed from the
					// options
					if (options.requestData != undefined) {
						$.extend(requestData, options.requestData);
					}

					// add the sortField
					if (options.sortField != undefined) {
						requestData.sortField = options.sortField;
					}

					// hard-code map for term
					if (request.term != undefined) {
						requestData.term = request.term;
					}
					// more generic map for any form based
					// autocomplete elements
					$.extend(requestData, buildRequestData(this.element));

					// final callback for using custom method
					if (options.enhanceRequestData != undefined) {
						options.enhanceRequestData(requestData, request);
					}
					
					//add a closure to ajax request that wraps the response callback. This way we can swap it out for a no-op if a new source() request
					//happens before the existing is complete.
					var responseHolder = {};
					responseHolder.callback = response;
					$elem.data('responseHolder', responseHolder);
					

					var ajaxRequest = {
						url: getBaseURI() + options.url,
						dataType: "jsonp",
						data: requestData,
						success: function(data) {
							if (!$elem.is(':focus')) {
								console
										.debug("input blurred before autocomplete results returned. returning no elements");
								responseHolder.callback({});
								return;
							}
							// if there's a custom dataMap
							// function, use that, otherwise not
							if (options.customDisplayMap == undefined) {
								options.customDisplayMap = function(item) {
									if (item.name  != undefined && options.dataPath != 'person') {
										item.label = htmlEncode(item.name);
									}
									return item;
								};
							}
							var values = $.map(eval(options.dataPath),
									options.customDisplayMap);
							// show create function

							// enable custom data to be pushed onto values
							if (options.addCustomValuesToReturn != undefined) {
								var extraValues = options
										.addCustomValuesToReturn(options,
												requestData);
								// could be push, need to test
								values = values.concat(extraValues);
							}
							console
									.log(options.dataPath
											+ " autocomplete returned "
											+ values.length);

							if (options.showCreate != undefined && options.showCreate == true	) {
								var createRow = buildRequestData($elem);
								createRow.value = request.term;
								// allow for custom phrasing
								if (options.showCreatePhrase != undefined) {
									createRow.label = "("
											+ options.showCreatePhrase + ": "
											+ request.term + ")";
								}
								createRow.id = -1;
								values.push(createRow);
							}
							responseHolder.callback(values);
						},
						complete: function() {
							
							$elem.removeData('responseHolder');
							console.trace("clearing responseholder. this should be blank -->" + $elem.data('reponseHolder') );
						}
					};
					$.ajax(ajaxRequest);
				},
				minLength: options.minLength || 0,
				select: function(event, ui) {
					// 'this' is the input box element.
					$elem = $(this);
					applyDataElements(this, ui.item);
					
					//cancel any pending searches once the user selects an item
					var responseHolder = $elem.data('responseHolder');
					if(responseHolder) {
						responseHolder.callback();
						responseHolder.callback = function(){};
					}
				},
				open: function() {
					$(this).removeClass("ui-corner-all").addClass(
							"ui-corner-top");
					if (options.customRender != undefined) {
						$("ul.ui-autocomplete li a").each(
								function() {
									var htmlString = $(this).html().replace(
											/&lt;/g, '<');
									htmlString = htmlString.replace(/&gt;/g,
											'>');
									$(this).html(htmlString);
								});
					}
					$("ul.ui-autocomplete").css("width",
							$(this).parent().width());
				},
				close: function() {
					$(this).removeClass("ui-corner-top").addClass(
							"ui-corner-all");
				}
			});
	if (options.customRender != undefined) {
		autoResult.each(function(idx, elem) {
			// handle custom rendering of result
			$(elem).data("autocomplete")._renderItem = options.customRender;
		});
	}
};

function cancelSearchRequest($elem) {
	
}

function htmlEncode(value){
  if (value == undefined || value == '') return "";
	  return $('<div/>').text(value).html();
}

function htmlDecode(value){
  if (value == undefined || value == '') return "";
	  return $('<div/>').html(value).text();
}
	
function applyKeywordAutocomplete(selector, lookupType, extraData, newOption) {
	var options = {};
	options.url = "lookup/" + lookupType;
	options.enhanceRequestData = function(requestData) {
		$.extend(requestData, extraData);
	};

	options.dataPath = "data.items";
	options.sortField = 'LABEL';
	options.showCreate = newOption;
	options.showCreatePhrase = "Create a new keyword";
	options.minLength = 2;
	applyGenericAutocomplete(selector, options);
}

function applyCollectionAutocomplete(selector, newOption) {
	var options = {};
	options.url = "lookup/collection";
	options.dataPath = "data.collections";
	options.sortField = 'TITLE';
	options.showCreate = true;
	options.showCreatePhrase = "Create a new collection";
	options.minLength = 2;
	applyGenericAutocomplete(selector, options);
}

function displayResourceAutocomplete(item) {
	var label = "";
	if (item.name) {
		label = item.name;
	}
	if (item.title) {
		label = item.title;
	}
	item.value = label + " (" + item.id + ") ";
	return item;
}

function applyResourceAutoComplete(selector, type) {
	var options = {};
	options.url = "lookup/resource";
	options.dataPath = "data.resources";
	options.sortField = 'TITLE';
	options.enhanceRequestData = function(requestData) {
		if (requestData["subCategoryId"] != undefined
				&& requestData["subCategoryId"] != ''
				&& requestData["subCategoryId"] != -1) {
			requestData["sortCategoryId"] = requestData["subCategoryId"];
		}
		requestData.resourceTypes = type;
	};
	options.ignoreRequestOptionsWhenEvaluatingEmptyRow = [ "subCategoryId",
			"sortCategoryId" ];
	options.minLength = 0;
	options.customDisplayMap = displayResourceAutocomplete;
	options.customRender = function(ul, item) {
		var description = "";
		console.trace(item);
		if (item.description != undefined) {
			description = item.description;
		}
		var link = "";
		if (item.urlNamespace) {
			// link = "<b onClick=\"openWindow('"+ getBaseURI() +
			// item.urlNamespace + "/view/" + item.id +"\')\">view</b>";
		}
		return $("<li></li>").data("item.autocomplete", item).append(
				"<a  title=\"" + htmlDecode(description) + "\">" + htmlEncode(item.value) + link
						+ "</a>").appendTo(ul);
	};

	applyGenericAutocomplete(selector, options);
//	var lookupUrl = getURI("lookup/resource");

}

function applyInstitutionAutoComplete(selector, newOption) {
	var options = {};
	options.url = "lookup/institution";
	options.dataPath = "data.institutions";
	options.sortField = 'CREATOR_NAME';
	options.enhanceRequestData = function(requestData) {
		requestData.institution = requestData.term;
	};
	options.showCreate = true;
	options.minLength = 2;
	options.showCreatePhrase = "Create new institution";
	applyGenericAutocomplete(selector, options);
};

// INHERITANCE

/*
 * DOWNWARD INHERITANCE SUPPORT
 */
var indexExclusions = [ 'investigationTypeIds', 'approvedSiteTypeKeywordIds',
		'materialKeywordIds', 'approvedCultureKeywordIds' ];

function populateSection(elem, formdata) {

	$(elem).populate(formdata, {
		resetForm : false,
		phpNaming : false,
		phpIndices : true,
		strutsNaming : true,
		noIndicesFor : indexExclusions
	});
}

// convert a serialized project into the json format needed by the form.
function convertToFormJson(rawJson) {
	// create a skeleton of what we need
	var obj = {

		title : rawJson.title,
		id : rawJson.id,
		resourceType : rawJson.resourceType,
		investigationInformation : {
			investigationTypeIds : $.map(rawJson.investigationTypes,
					function(v) {
						return v.id;
					})
					|| []
		},
		siteInformation : {
			siteNameKeywords : $.map(rawJson.siteNameKeywords, function(v) {
				return v.label;
			}),
			approvedSiteTypeKeywordIds : $.map(
					rawJson.approvedSiteTypeKeywords, function(v) {
						return v.id;
					})
					|| [],
			uncontrolledSiteTypeKeywords : $.map(
					rawJson.uncontrolledSiteTypeKeywords, function(v) {
						return v.label;
					})
		},
		materialInformation : {
			materialKeywordIds : $.map(rawJson.materialKeywords, function(v) {
				return v.id;
			}) || []
		},
		culturalInformation : {
			approvedCultureKeywordIds : $.map(rawJson.approvedCultureKeywords,
					function(v) {
						return v.id;
					})
					|| [],
			uncontrolledCultureKeywords : $.map(
					rawJson.uncontrolledCultureKeywords, function(v) {
						return v.label;
					})
		},
		spatialInformation : {
			geographicKeywords : $.map(rawJson.geographicKeywords, function(v) {
				return v.label;
			}),
			'p_maxy' : null, // FIXME: I don't think these p_**** fields are
			// used/needed
			'p_minx' : null,
			'p_maxx' : null,
			'p_miny' : null
		},
		temporalInformation : {
			temporalKeywords : $.map(rawJson.temporalKeywords, function(v) {
				return v.label;
			}),
			coverageDates : rawJson.coverageDates
		},
		otherInformation : {
			otherKeywords : $.map(rawJson.otherKeywords, function(v) {
				return v.label;
			})
		}
	};

	// FIXME: update the parent latlong box (i.e. the red box not the brown
	// box)..p_miny, pmaxy, etc. etc.
	// console.warn(rawJson.firstLatitudeLongitudeBox)
	if (rawJson.firstLatitudeLongitudeBox) {
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
	if (!newSize)
		newSize = 1;
	table.find("tr:not(:first)").remove();
	// change the id/name for each element in first row that matches _num_
	// format to _0_
	var firstRow = table.find("tr:first");
	resetIndexedAttributes(firstRow);
	if (newSize > 1) {
		for ( var i = 1; i < newSize; i++) {
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
	var rex = /^(.+[_|\[])([0-9]+)([_|\]])$/; // string ending in _num_ or
	// [num]
	var replacement = "$10$3"; // replace foo_bar[5] with foo_bar[0]
	$(elem).add("tr, :input", elem).each(function(i, v) {
		var id = $(v).attr("id");
		var name = $(v).attr("name");
		if (id) {
			var newid = id.replace(rex, replacement);
			console.trace(id + " is now " + newid);
			$(v).attr("id", newid);
		}
		if (name) {
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
		"uncontrolledCultureKeywords" : [],
		"uncontrolledSiteTypeKeywords" : []
	};
	return skeleton;
}

// return true if the repeatrows contained in the selector match the list of
// strings
// FIXME: these are terrible function names
function inheritingRepeatRowsIsSafe(rootElementSelector, values) {
	var repeatRowValues = $.map($('input[type=text]', rootElementSelector),
			function(v, i) {
				if ($(v).val())
					return $(v).val();
			});
	return repeatRowValues.length == 0
			|| $.compareArray(repeatRowValues, values);
}

// FIXME: these are terrible function names
// return true if this section can 'safely' inherit specified values. 'safe'
// means that the target values are empty or the same
// as the incoming values.
function inheritingCheckboxesIsSafe(rootElementSelector, values) {
	var checkedValues = $.map($(':checkbox:checked', rootElementSelector),
			function(v, i) {
				return $(v).val();
			});
	var isSafe = checkedValues.length == 0
			|| $.compareArray(checkedValues, values);
	return isSafe;
}

function inheritingMapIsSafe(rootElementSelector, spatialInformation) {
	// FIXME: pretty sure that rootElementSelector isn't needed. either ditch it
	// or make the fields retrievable by name instead of id
	var si = spatialInformation;
	// compare parent coords to this form's current coords. seems like overkill
	// but isn't.
	var jsonVals = [ si.minx, si.miny, si.maxx, si.maxy ]; // strip out nulls
	var formVals = [];
	formVals = formVals.concat($('#minx'));
	formVals = formVals.concat($('#miny'));
	formVals = formVals.concat($('#maxx'));
	formVals = formVals.concat($('#maxy'));

	formVals = $.map(formVals, function(item) {
		if ($(item).val())
			return $(item).val();
	});
	return formVals.length == 0 || $.compareArray(jsonVals, formVals, false); // don't
	// ignore
	// array
	// order
	// in
	// comparison
}

// return whether it's "safe" to populate the temporal information section with
// the supplied temporalInformation
// we define "safe" to mean that section is either currently blank or that the
// supplied temporalInformation is the same as what is already on the form.
function inheritingDatesIsSafe(rootElementSelector, temporalInformation) {
	// are all the fields in this section blank?
	var $coverageTextFields = $('input:text', '#coverageTable');
	var joinedFieldValues = $coverageTextFields.map(function() {
		return $(this).val();
	}).toArray().join("");

	// okay to populate if if the form section is blank
	if (joinedFieldValues == "")
		return true;

	// not okay to populate if the incoming list is a different size as the
	// current list
	$tableRows = $('tr', '#coverageTable');
	if (temporalInformation.coverageDates.length != $tableRows.length)
		return false;

	// at this point it's we need to compare the contents of the form vs.
	// incoming coverage dates
	var concatTemporalInformation = $.map(temporalInformation.coverageDates,
			function(val, i) {
				return "" + val.startDate + val.endDate + val.description;
			}).join("");
	var concatRowFields = $.map($tableRows, function(rowElem, i) {
		var concatRow = $('.coverageStartYear', rowElem).val();
		concatRow += $('.coverageEndYear', rowElem).val();
		concatRow += $('.coverageDescription', rowElem).val();
		return concatRow;
	}).join("");

	return concatTemporalInformation == concatRowFields;

}

function inheritInformation(formId, json, sectionId, tableId) {
	disableSection(sectionId);
	clearFormSection(sectionId);
	if (tableId != undefined) {
		if (document.getElementById("uncontrolled" + tableId + "Table") != undefined) {
			resetRepeatRowTable('uncontrolled' + tableId + 'Table',
					json['uncontrolled' + tableId + 's'].length);
		}
		if (document.getElementById("approved" + tableId + "Table") != undefined) {
			resetRepeatRowTable('approved' + tableId + 'Table', json['approved'
					+ tableId + 's'].length);
		}
		var simpleId = tableId;
		simpleId[0] = simpleId[0].toLowerCase();
		if (document.getElementById(simpleId + "Table") != undefined) {
			resetRepeatRowTable(simpleId + 'Table', json[simpleId + 's'].length);
		}
	}
	populateSection(formId, json);
}

function inheritSiteInformation(formId, json) {
	disableSection('#divSiteInformation');
	clearFormSection('#divSiteInformation');
	resetRepeatRowTable('siteNameKeywordTable',
			json.siteInformation['siteNameKeywords'].length);
	resetRepeatRowTable('uncontrolledSiteTypeKeywordTable',
			json.siteInformation['uncontrolledSiteTypeKeywords'].length);
	populateSection(formId, json.siteInformation);
}

function inheritSpatialInformation(formId, json) {
	disableSection('#divSpatialInformation');
	disableMap();

	clearFormSection('#divSpatialInformation');
	resetRepeatRowTable('geographicKeywordTable',
			json.spatialInformation['geographicKeywords'].length);
	populateSection(formId, json.spatialInformation);

	// clear the existing redbox and draw new one;
	if (GZoomControl.G.oZoomArea) {
		// TODO: reset the map to default zoom and default location
		GZoomControl.G.oMap.removeOverlay(GZoomControl.G.oZoomArea);
	}
	drawMBR();
	populateLatLongTextFields();
}

function inheritTemporalInformation() {
	// function() {inheritInformation(formId,
	// json.temporalInformation,"#divTemporalInformation","temporalKeyword");}
	var sectionId = '#divTemporalInformation';
	disableSection(sectionId);
	clearFormSection(sectionId);
	resetRepeatRowTable('temporalKeywordTable',
			json.temporalInformation.temporalKeywords.length);
	resetRepeatRowTable('coverageTable',
			json.temporalInformation.coverageDates.length);
	populateSection(formId, json.temporalInformation);
}

function bindCheckboxToInheritSection(cbSelector, divSelector, isSafeCallback,
		inheritSectionCallback, enableSectionCallback) {
	$(cbSelector)
			.change(
					function(e) {
						var cb = this;
						var divid = divSelector;
						var proceed = true;
						if ($(cb).is(":checked")) {
							// check if inheriting would overrwrite existing
							// values
							var isSafe = isSafeCallback();
							if (!isSafe) {
								proceed = confirm("Inheriting this section will overwrite existing values. Continue?");
								if (!proceed) {
									$(cb).removeAttr("checked");
								}
							}
							if (proceed) {
								inheritSectionCallback();
							}
						} else {
							console.trace(divid + " cleared");
							if (enableSectionCallback) {
								enableSectionCallback();
							} else {
								enableSection(divid);
							}
						}

						updateSelectAllCheckboxState();
					});

}

function applyInheritance(project, resource) {
	// if we are editing, set up the initial form values
	if (project) {
		json = convertToFormJson(project);
		updateInheritableSections(json);
	}

	// update the inherited form values when project selection changes.
	$('#projectId').change(function(e) {
		var sel = this;
		console.trace('project changed. new value:' + $(sel).val());
		if ($(sel).val() != '' && $(sel).val() > 0) {
			console.trace('about to make ajax call for project info');
			$.ajax({
				url : getBaseURI() + "project/json",
				dataType : "jsonp",
				data : {
					id : $(sel).val()
				},
				success : projectChangedCallback,
				error : function(msg) {
					console.error("error");
				}
			});
		} else {
			project = getBlankProject();
			json = convertToFormJson(project);
			updateInheritableSections(json);
		}
		enableOrDisableInheritAllSection();
		updateInheritanceCheckboxes();
	});
	updateInheritanceCheckboxes();
	enableOrDisableInheritAllSection();
	processInheritance(formId);
}

function processInheritance(formId) {
	// ---- bind inheritance tracking checkboxes

	bindCheckboxToInheritSection(
			'#cbInheritingSiteInformation',
			'#divSiteInformation',
			function() {
				var allKeywords = json.siteInformation.siteNameKeywords
						.concat(json.siteInformation.uncontrolledSiteTypeKeywords);
				return inheritingCheckboxesIsSafe('#divSiteInformation',
						json.siteInformation.approvedSiteTypeKeywordIds)
						&& inheritingRepeatRowsIsSafe('#divSiteInformation',
								allKeywords);
			}, function() {
				inheritSiteInformation(formId, json);
			});

	bindCheckboxToInheritSection('#cbInheritingTemporalInformation',
			'#divTemporalInformation', function() {
				return inheritingRepeatRowsIsSafe('#temporalKeywordTable',
						json.temporalInformation.temporalKeywords)
						&& inheritingDatesIsSafe('#divTemporalInformation',
								json.temporalInformation);
			}, function() {
				inheritTemporalInformation();
			});

	bindCheckboxToInheritSection(
			'#cbInheritingCulturalInformation',
			'#divCulturalInformation',
			function() {
				return inheritingCheckboxesIsSafe('#divCulturalInformation',
						json.culturalInformation.approvedCultureKeywordIds)
						&& inheritingRepeatRowsIsSafe(
								'#divCulturalInformation',
								json.culturalInformation.uncontrolledCultureKeywords);
			}, function() {
				inheritInformation(formId, json.culturalInformation,
						"#divCulturalInformation", "CultureKeyword");
			});

	bindCheckboxToInheritSection('#cbInheritingOtherInformation',
			'#divOtherInformation', function() {
				return inheritingRepeatRowsIsSafe('#divOtherInformation',
						json.otherInformation.otherKeywords);
			}, function() {
				inheritInformation(formId, json.otherInformation,
						"#divOtherInformation", "otherKeyword");
			});

	bindCheckboxToInheritSection('#cbInheritingInvestigationInformation',
			'#divInvestigationInformation', function() {
				return inheritingCheckboxesIsSafe(
						'#divInvestigationInformation',
						json.investigationInformation.investigationTypeIds);
			}, function() {
				inheritInformation(formId, json.investigationInformation,
						'#divInvestigationInformation');
			});

	bindCheckboxToInheritSection('#cbInheritingMaterialInformation',
			'#divMaterialInformation', function() {
				return inheritingCheckboxesIsSafe('#divMaterialInformation',
						json.materialInformation.materialKeywordIds);
			}, function() {
				inheritInformation(formId, json.materialInformation,
						'#divMaterialInformation');
			});

	bindCheckboxToInheritSection('#cbInheritingSpatialInformation',
			'#divSpatialInformation', function() {
				return inheritingMapIsSafe('#divSpatialInformation',
						json.spatialInformation)
						&& inheritingRepeatRowsIsSafe(
								'#geographicKeywordTable',
								json.spatialInformation.geographicKeywords);
			}, function() {
				inheritSpatialInformation(formId, json);
			}, function() {
				enableSection('#divSpatialInformation');
				enableMap();
			});

	// gzoom-control doesn't exist when this code fires on pageload. so we
	// wait a moment before trying checking to see if the google map controls
	// should be hidden
	setTimeout(function(e) {
		if ($('#cbInheritingSpatialInformation').is(':checked')) {
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
	if (!project.id) {
		console.trace('clearing inherited sections');
		project = getBlankProject();
	}

	if (project.resourceType == 'INDEPENDENT_RESOURCES_PROJECT') {
		project = getBlankProject();
	}

	json = convertToFormJson(project);
	updateInheritableSections(json);
}

function updateInheritanceCheckboxes() {
	var projectId = $('#projectId').val();
	if (projectId <= 0) {
		$('.inheritlabel').find('label').addClass('disabled');
		$('.inheritlabel').find(':checkbox').prop('disabled', true);
		$('.inheritlabel').find(':checkbox').prop('checked', false);
		enableAll();
	} else {
		$('.inheritlabel').find('label').removeClass('disabled');
		$('.inheritlabel').find(':checkbox').prop('disabled', false);
	}
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
	console
			.trace('updating inheritable sections with information from project:'
					+ json.title);

	// indicate in each section which project the section will inherit from.
	var labelText = "Inherit values from parent project";
	var selectedProjectName = "No project selected";
	if (json.id > 0) {
		labelText = 'Inherit values from parent project "'
				+ TDAR.ellipsify(json.title, 60) + '"';
		selectedProjectName = "Inherit project metadata from " + json.title;
	}
	$('.inheritlabel label').text(labelText);
	$('#lblCurrentlySelectedProject').text(selectedProjectName);

	// show or hide the text of each inheritable section based on checkbox
	// state.
	if ($('#cbInheritingInvestigationInformation').is(':checked')) {
		inheritInformation(formId, json.investigationInformation,
				'#divInvestigationInformation');
	}

	if ($('#cbInheritingSiteInformation').is(':checked')) {
		inheritSiteInformation(formId, json);
	}

	if ($('#cbInheritingMaterialInformation').is(':checked')) {
		inheritInformation(formId, json.materialInformation,
				'#divMaterialInformation');
	}

	if ($('#cbInheritingCulturalInformation').is(':checked')) {
		inheritInformation(formId, json.culturalInformation,
				"#divCulturalInformation", "CultureKeyword");
	}

	if ($('#cbInheritingSpatialInformation').is(':checked')) {
		inheritSpatialInformation(formId, json);
	}

	if ($('#cbInheritingTemporalInformation').is(':checked')) {
		inheritTemporalInformation();
	}

	if ($('#cbInheritingOtherInformation').is(':checked')) {
		inheritInformation(formId, json.otherInformation,
				"#divOtherInformation", "otherKeyword");
	}
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

function testify(formSelector) {
	var simpleInputs = $(":input:not([type=checkbox],[type=radio])",
			formSelector);
	var checkedInputs = $(":checked", formSelector);

	// get rid of struts checkbox state cruft
	simpleInputs = simpleInputs.map(function(ignored, elem) {
		var attr = $(elem).attr('name');
		if (typeof attr == 'undefined')
			return null;
		if (attr.indexOf('__checkbox') == 0)
			return null;
		return elem;
	});
	console
			.log("HashMap<String,String> valMap = new HashMap<String,String>();");
	$.each(simpleInputs, function(index, elem) {
		if ($(elem).val() && $(elem).val().length > 0) {
			var str = sprintf('valMap.put("{0}", "{1}");',
					$(elem).attr('name'), $(elem).val());
			console.log(str);
		}
	});

	$.each(checkedInputs, function(index, elem) {
		var str = sprintf(
				'valMap.put("{0}", "true"); //setting checkbox/radio', $(elem)
						.attr('name'));
		console.log(str);
	});

}

/* ASYNC FILE UPLOAD SUPPORT */

function displayAsyncError(handler, msg) {
	var errorMsg = ("<p>tDAR experienced errors while uploading your file.  Please try again, or if the error persists, please save this record without the "
			+ " file attachment and notify a tDAR Administrator.</p>");
	if (msg)
		errorMsg = msg;
	var jqAsyncFileUploadErrors = $('#divAsyncFileUploadErrors');
	jqAsyncFileUploadErrors.html(errorMsg);
	jqAsyncFileUploadErrors.show();
	try {
		handler.removeNode(handler.uploadRow);
	} catch (ex) {
	}
	setTimeout(function() {
		jqAsyncFileUploadErrors.fadeOut(1000);
	}, 10000);
}

function sendFileIfAccepted(event, files, index, xhr, handler, callBack) {
	if (!files || files.length == 0) {
		handler.removeNode(handler.uploadRow);
		return false;
	}
	var accepted = false;
	var msgs = "";
	if (files[0].fileName) {
		var i = files.length - 1;
		while (i >= 0) {
			var accepted_ = fileAccepted(files[i].fileName);
			console.log(files[i].fileName + " : " + accepted_);
			if (accepted_ == false) {
				msgs += '<p>Sorry, this file type is not accepted:"'
						+ files[i].fileName + '"</p>';
				files.splice(i, 1);
			}
			;
			i--;
		}
		if (files.length > 0) {
			accepted = true;
		}
	} else {
		if (index == undefined) {
			index = 0;
		}
		console.log(index + " : " + files);
		accepted = fileAccepted(files[index].name);
	}

	if (accepted) {
		asyncUploadStarted();
		callBack();
	}
	if (!accepted || msgs != "") {
		if (msgs == "") {
			msgs = '<p>Sorry, this file type is not accepted: "'
					+ files[index].name + '"</p>';
		}
		displayAsyncError(handler, msgs);
		return;
	}
}

function showAsyncReminderIfNeeded() {
	// if we have files in the downloaded table we don't need the reminder shown
	$('#reminder').show();
	if ($('#files').find('tr').length > 1)
		$('#reminder').hide();
}

// grab a ticket (if needed) from the server prior to yielding control to the
// file processor.
// FIXME: on-demand ticket grabbing only works when multiFileRequest turned on.
// Make it work even when turned off.

// additional form data sent with the ajax file upload. for now we only need the
// ticket id
function getFormData() {
	var frmdata = [ {
		name : 'ticketId',
		value : $('#ticketId').val()
	} ];
	// console.log("getFormData:" + frmdata);
	// console.log(frmdata);
	return frmdata;
}

function asyncUploadStarted() {
	g_asyncUploadCount++;
	formSubmitDisable();
}

function asyncUploadEnded() {
	g_asyncUploadCount--;
	if (g_asyncUploadCount <= 0) {
		formSubmitEnable();
	}
}

function applyAsync(formId) {
	$(formId).data('nextFileIndex', $('#files tr').not('.noFiles').length);
	console.trace("apply async called");
	$(formId)
			.fileUploadUI(
					{
						multiFileRequest : true, // FIXME: parts of this code
						// aren't prepared for
						// request-per-file uploads yet.
						// dropZone:$('#divAsycFileUploadDropzone'),
						uploadTable : $('#uploadFiles'),
						fileInputFilter : $('#fileAsyncUpload'),
						downloadTable : $('#files'),
						url : getBaseURI() + "upload/upload",
						beforeSend : function initFileUpload(event, files,
								index, xhr, handler, callBack) {
							console.log('initFileUpload');
							if ($('#ticketId').val()) {
								sendFileIfAccepted(event, files, index, xhr,
										handler, callBack);
							} else {
								// grab a ticket and set the ticket id back to
								// the hidden form
								// field
								var ticketUrl = getBaseURI()
										+ "upload/grab-ticket";
								$.post(ticketUrl, function(data) {
									$('#ticketId').val(data.id);
									sendFileIfAccepted(event, files, index,
											xhr, handler, callBack); // proceed
									// w/
									// upload
								}, 'json');
							}
						},
						formData : getFormData,
						buildUploadRow : function(files, index) {
							console.log('building upload row');
							console.log(files);
							if (index) {
								// browser doesn't support multi-file uploads,
								// so this
								// callback called once per file
								return $('<tr><td>'
										+ files[index].name
										+ '<\/td>'
										+ '<td class="file_upload_progress"><div><\/div><\/td>'
										+ '<td class="file_upload_cancel">'
										+ '<button type="button" class="ui-state-default ui-corner-all" title="Cancel" onclick="return false;">'
										+ '<span class="ui-icon ui-icon-cancel">Cancel<\/span>'
										+ '<\/button><\/td><\/tr>');
							} else {
								// browser supports multi-file uploads
								return $('<tr><td> Uploading '
										+ files.length
										+ ' files<\/td>'
										+ '<td class="file_upload_progress"><div><\/div><\/td>'
										+ '<td class="file_upload_cancel">'
										+ '<button type="button" class="ui-state-default ui-corner-all" title="Cancel" onclick="return false;">'
										+ '<span class="ui-icon ui-icon-cancel">Cancel<\/span>'
										+ '<\/button><\/td><\/tr>');
							}

						},
						buildDownloadRow : function(jsonObject) {
							$("#files .noFiles").hide();
							var toReturn = "";
							var existingNumFiles = $('#files tr').not(
									'.noFiles').length;
							console.debug("Existing number of files: "
									+ existingNumFiles);

							$("button.replaceButton.ui-state-disabled").each(
									function() {
										var $this = $(this);
										$this.removeClass("ui-state-disabled");
										$this.addClass("ui-state-default");
										$this.removeAttr('disabled');
									});

							for ( var fileIndex = 0; fileIndex < jsonObject.files.length; fileIndex++) {
								var row = $('#queuedFileTemplate').clone();
								var nextIndex = $(formId).data('nextFileIndex');
								console.debug("next file index: " + nextIndex);
								row
										.find('*')
										.each(
												function() {
													var elem = this;
													// skip any tags with the
													// repeatRowSkip attribute
													$
															.each(
																	[
																			"id",
																			"onclick",
																			"name",
																			"for",
																			"value" ],
																	function(i,
																			attrName) {
																		// ensure
																		// each
																		// download
																		// row
																		// has a
																		// unique
																		// index.
																		replaceAttribute(
																				elem,
																				attrName,
																				'{ID}',
																				nextIndex);
																	});

													$
															.each(
																	[ "value",
																			"onclick" ],
																	function(i,
																			attrName) {
																		replaceAttribute(
																				elem,
																				attrName,
																				'{FILENAME}',
																				jsonObject.files[fileIndex].name);
																	});

													// nodeType=3 is hardcoded
													// for "TEXT" node
													if ($(this).contents().length == 1
															&& $(this)
																	.contents()[0].nodeType == 3) {
														var txt = $(this)
																.text();
														if (txt
																.indexOf("{FILENAME}") != -1) {
															$(this)
																	.text(
																			$(
																					this)
																					.text()
																					.replace(
																							/\{FILENAME\}/g,
																							jsonObject.files[fileIndex].name));
														}
														if (txt
																.indexOf("{FILESIZE}") != -1) {
															$(this)
																	.text(
																			$(
																					this)
																					.text()
																					.replace(
																							/\{FILESIZE\}/g,
																							jsonObject.files[fileIndex].size));
														}
													}
													$(formId).data(
															'nextFileIndex',
															nextIndex + 1);
												});

								toReturn += $(row).find("tbody").html();
							}
							return $(toReturn);
						},
						onComplete : function(event, files, index, xhr, handler) {
							showAsyncReminderIfNeeded();
							asyncUploadEnded();
							applyWatermarks();
							// FIXME: onError registration doesn't appear to be
							// called even when
							// status <> 200;
							if (xhr.status && xhr.status != 200) {
								displayAsyncError(handler);
							}
							console.log("complete");
						},
						onError : function(event, files, index, xhr, handler) {
							asyncUploadEnded();
							// For JSON parsing errors, the load event is saved
							// as
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
						onAbort : function(event, files, index, xhr, handler) {
							asyncUploadEnded();
							handler.removeNode(handler.uploadRow);
						}

					});

	// Ensure that the ticketid is blank if there are no pending file uploads.
	// For example, a user begins uploading their first file,
	// but then either cancels the upload or the upload terminates abnormally.
	$(formId).submit(function() {
		if ($('tr', '#files').not('.noFiles').size() == 0) {
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
		if ($.inArray(existingValue, [ "ADD", "DELETE", "REPLACE" ]) >= 0) {
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
		$(fileAction).attr("prev", fileAction.val());
		fileAction.val(newUpload ? "NONE" : "DELETE");
	} else {
		buttonText.html('delete');
		$(rowId + " .filename").removeClass('deleted-file');
		$(fileAction).val(fileAction.attr("prev"));
	}

	if ($("#files tr").length == 1) {
		$("#files .noFiles").show();
	}
}

function replaceFile(rowId, replacementRowId) {
	var row = $(rowId);
	var existingFilename = row.find(".filename").html();
	var replacementFilename = $(replacementRowId).find('.replacefilename')
			.html();
	// message to let the user know that this file is being used to replace an
	// existing file.
	$(replacementRowId)
			.find("td:first")
			.append(
					"<div class='ui-state-default'><span class='tdar-ui-icon ui-icon ui-icon-info'></span>Replacing <b>"
							+ existingFilename
							+ "</b> with <b>"
							+ replacementFilename + "</b>.</div>");
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
	row
			.find("td:first")
			.append(
					"<div class='ui-state-default'><span class='tdar-ui-icon ui-icon ui-icon-info'></span>Replacing with <b>"
							+ replacementFilename + "</b></div>");
	var table = row.parent();
	table.find(".fileSequenceNumber").each(function(index) {
		$(this).val(index);
	});
}

function replaceDialog(rowId, filename) {
	var contents = "<b>Select the Newly Uploaded File That Should Replace The Existing File:</b><br/><ul>";
	var replacementFiles = $('#files .newrow');
	if (replacementFiles.length == 0) {
		contents += "<li>Please upload a file and then choose the replace option</li>";
	}
	replacementFiles.each(function(i) {
		var replacementRowId = "#" + $(this).attr("id");
		var filename = $(this).find('.replacefilename').html();
		contents += "<li><input type='radio' name='replaceWith' value='"
				+ replacementRowId + "'><span>" + filename + "</span></li>";
	});

	contents += "</ul>";
	var $dialog = $('<div />')
			.html(contents)
			.dialog(
					{
						title : 'Replace File',
						buttons : {
							'replace' : function() {
								replaceFile(
										rowId,
										$(
												"input:radio[name=replaceWith]:checked")
												.val());
								$(this).dialog('close');
							},
							'cancel' : function() {
								$(this).dialog('close');
							}
						}
					});
}

function initializeView() {
	console.debug('initialize view');
	applyZebraColors();
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

/**
 * SINGLE INIT FUNCTION
 */
function initializeEdit() {
	// if user gets to the edit page by clicking the 'back' button the submit
	// button may be disabled.
	var $button = $("#submitButton");
	$button.removeAttr('disabled');
	$button.removeClass("waitingSpinner");
	// / this is for the backwards and forwards page cache
	$(window).bind("pageshow", function() {
		var $button = $("#submitButton");
		$button.removeAttr('disabled');
		$button.removeClass("waitingSpinner");
	});

	initializeRepeatRow();

	delegateCreator("#authorshipTable", false, true);
	delegateCreator("#creditTable", false, true);
	delegateCreator("#divAccessRights", true, false);

	delegateAnnotationKey("#resourceAnnotationsTable", "annotation",
			"annotationkey");
	delegateKeyword("#siteNameKeywordTable", "sitename", "SiteNameKeyword");
	delegateKeyword("#uncontrolledSiteTypeKeywordTable", "siteType",
			"SiteTypeKeyword");
	delegateKeyword("#uncontrolledCultureKeywordTable", "culture",
			"CultureKeyword");
	delegateKeyword("#temporalKeywordTable", "temporal", "TemporalKeyword");
	delegateKeyword("#otherKeywordTable", "other", "OtherKeyword");
	delegateKeyword("#geographicKeywordTable", "geographic",
			"GeographicKeyword");

	applyInstitutionAutoComplete('#txtResourceProviderInstitution', true);
	initializeView();

	$('#resourceCollectionTable').delegate(
			".collectionAutoComplete",
			"focusin",
			function() {
				applyCollectionAutocomplete(
						"#resourceCollectionTable .collectionAutoComplete",
						true);
			});

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
					applyPersonAutoComplete(id + " .nameAutoComplete", false,
							showCreate);
				});
		$(id).delegate(".institutionAutoComplete", "focusin", function() {
			applyInstitutionAutoComplete(id + " .institution", true);
		});
	} else {
		$(id).delegate(".userAutoComplete", "focusin", function() {
			applyPersonAutoComplete(id + " .userAutoComplete", true, false);
		});
	}
}

// fixme: instead of focusin, look into using a customEvent (e.g. 'rowCreated')
function delegateAnnotationKey(id, prefix, delim) {
	$(id).delegate("." + prefix + "AutoComplete", "focusin", function() {
		applyKeywordAutocomplete("." + prefix + "AutoComplete", delim, true);
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

function setupEditForm(formId, acceptedFiles) {
	$(formId).FormNavigate(
			"Leaving the page will cause any unsaved data to be lost!");

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
	setupFormValidate(formId);
	// trim any type-converted fields prior to submit
	
	
	
	$(formId)
			.submit(
					function(f) {
						try {
							$
									.each(
											$('.reasonableDate, .coverageStartYear, .coverageEndYear, .date, .number'),
											function() {
												if ($(this).val() == undefined
														|| $(this).val() == "")
													return;
												// this is essential, or IE will
												// replace null values w/
												// empty-string values, and
												// type-conversion dies.
												var elem = this;
												$(elem).val(
														$.trim($(elem).val()));
											});
						} catch (err) {
							console.error("unable to trim:" + err);
						}
						
						
						var $button = $('input[type=submit]', f);
						$button.siblings(".waitingSpinner").css('visibility','visible');
						
						return true;
					});

	var uploadField = document.getElementById("fileUploadField");
	if (uploadField != undefined) {
		var validate = $(uploadField);
		$(validate).rules(
				"add",
				{
					accept : acceptedFiles,
					messages : {
						accept : "Please enter a valid file ("
								+ acceptedFiles.replace(/\|/ig, ", ") + ")"
					}
				});
	}

	$('.coverageTypeSelect', "#coverageTable").each(function(i, elem) {
		prepareDateFields(elem);
	});

	if ($(formId + '_uploadedFiles').length > 0) {
		console.trace("wiring up uploaded file check");
		var validateUploadedFiles = function() {
			if ($(formId + "_uploadedFiles").val().length > 0) {
				$("#reminder").hide();
			}
		};
		$(formId + '_uploadedFiles').change(validateUploadedFiles);
		validateUploadedFiles();
	}

	if ($.browser.msie || $.browser.mozilla && getBrowserMajorVersion() < 4) {
		$('textarea.resizable:not(.processed)').TextAreaResizer();
	}

	$("#coverageTable").delegate(".coverageTypeSelect", "change", function() {
		console.log('called delegate');
		prepareDateFields(this);
	});
	showAccessRightsLinkIfNeeded();
	$('#cbConfidential').click(showAccessRightsLinkIfNeeded);
	$('#resourceAvailability').change(showAccessRightsLinkIfNeeded);

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

function setupFormValidate(formId) {
	$(formId)
			.validate(
					{
						errorLabelContainer : $("#error"),
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
						invalidHandler : $.watermark.showAll,
						showErrors : function(errorMap, errorList) {
							this.defaultShowErrors();
							if (errorList != undefined && errorList.length > 0
									&& this.submitted) {
								dialogOpen = true;
								$("#error")
										.clone()
										.dialog(
												{
													title : 'Please correct the following issues before saving',
													buttons : {
														"Ok" : function() {
															dialogOpen = false;
															$(this).dialog(
																	"close");
														}
													},
													dialogClass : 'errorDialog',
													resizable : false,
													draggable : false
												});
							}
						},
						submitHandler : function(f) {
							var $button = $('input[type=submit]', f);
							$button.siblings(".waitingSpinner").css('visibility','visible');
							// prevent multiple form submits (e.g. from
							// double-clicking the submit button)
							$button.attr('disabled', 'disabled');
							f.submit();
							}
					});

	$(formId).delegate("input.error","change blur",function() {
		if ($("div.errorDialog:visible") == undefined || $("div.errorDialog:visible").length == 0) {
		$(this).valid();
		console.log('revalidating...');
		}
	});

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
	$("#t-edition").hide();
	$("#t-institution").hide();

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
		$("#t-edition").show();
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

function selectAllInheritanceClicked(elem) {
	var $elem = $(elem);
	var checked = $elem.prop('checked');
	var $sectionCheckboxes = $('.inheritlabel input[type=checkbox]');

	// make all of the section checkboxes just like this checkbox.
	if (checked) {
		// check all of the unchecked sections
		$sectionCheckboxes.not(':checked').each(function() {
			$(this).click();
		});
	} else {
		// uncheck all of the checked sections
		$sectionCheckboxes.filter(':checked').each(function() {
			$(this).click();
		});
	}

}

// determine whether to enable or disable the 'inherit all' checkbox based upon
// the currently selected parent project.
function enableOrDisableInheritAllSection() {
	var $cbSelectAllInheritance = $('#cbSelectAllInheritance');
	var projectId = $('#projectId').val();
	if (projectId > 0) {
		enableSection('#divInheritFromProject');

	} else {
		$cbSelectAllInheritance.prop('checked', false);
		disableSection('#divInheritFromProject');
	}
}

// properly set the state of the 'inherit all' checkbox. In short, only check if
// all inherit-sections are checked.
function updateSelectAllCheckboxState() {
	var $cbSelectAllInheritance = $('#cbSelectAllInheritance');

	var $uncheckedBoxes = $('.inheritlabel input[type=checkbox]').not(
			":checked");
	$cbSelectAllInheritance.prop('checked', $uncheckedBoxes.length == 0);
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
	$categoryIdSelect.siblings(".waitingSpinner").css('visibility','visible');
	$.get(getBaseURI() + "resource/ajax/column-metadata-subcategories", {
		"categoryVariableId" : $categoryIdSelect.val()
	}, function(data_, textStatus) {
		var data = jQuery.parseJSON(data_);

		var result = "";
		for ( var i = 0; i < data.length; i++) {
			result += "<option value=\"" + data[i]['value'] + "\">"
					+ data[i]['label'] + "</option>\n";
		}

		$categoryIdSelect.siblings(".waitingSpinner").css('visibility','hidden');
		$subCategoryIdSelect.html(result);
	});
}
