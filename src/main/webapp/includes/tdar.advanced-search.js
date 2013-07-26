
TDAR.namespace("advancedSearch");
TDAR.advancedSearch = (function() {
    "use strict";

    var self = {};

function _initAdvancedSearch() {
    TDAR.common.initFormValidation($('#searchGroups')[0]);
    TDAR.repeatrow.registerRepeatable(".repeatLastRow");
    
    //HACK: clicking delete button when only one row present resets fieldType select  and may become out of sync w/ term control
    $('#searchGroups').on('click', '.repeat-row-delete', function(){
        if($('#searchGroups').find('.searchType').length === 1) {
            $('#searchGroups').find('.searchType').trigger("change");
        }
    });
    
    
    // when user changes searchType: swap out the term ui snippet
    $('#searchGroups').on('change', '.searchType', function(evt) {
        "use strict";
        
        console.log("change event on %s", this.id);
        var $select = $(this);
        var searchType = $select.val();
        var $controlGroup = $select.parent();
        
        

        // get a copy of the template term controls
        var row = $('div.' + searchType, '#template').clone();
        // figure out which group, rownum we are in, and then update attribute
        // values of the cloned row
        var groupnum = $(this).closest(".grouptable").data("groupnum");
        var rownum = $(this).closest(".grouptable").children(".termrow:visible").index($(this).closest(".termrow"));
        updateAttributesForRow($(row), groupnum, rownum);

        // remove whatever is currently inside of the term-container and replace with the new term
        var $termContainer= $controlGroup.find('div.term-container');

        var $term = $('.term', $termContainer);
        var oldval = "";

        // for changing between two term types that are simple text fields, we
        // attempt to 'retain' the value by copying it from field we are about
        // to destroy to the new field that will take it's place. both the old 
        // term div and the incoming term div need to have to the 'retain' 
        // class.
        if ($term.hasClass('retain')) {
            oldval = $(':text:first', $term).val();
        }
        $term.remove();

        $termContainer.empty().append(row);
        
        if ($(row).hasClass('retain')) {
            $(':text:first', row).val(oldval);
        }

        sectionLoaded(row);
    });

    // after rows added, replace attribute values
    $('.grouptable').on('repeatrowadded', function(evt, parentElement,  row, idx) {
        var $row = $(row);
        // console.log('added row:' + $row + ' p2:' + idx);
        var $repeatable = $(this);
        var $select = $row.find('.searchType');
        $select.val($select.find("option:first").val()).trigger("change");
        $select.removeAttr("readonly");
        updateAttributesForRow($row, $repeatable.data('groupnum'), idx);
    });

    // more handling of added/removed rows
    // TODO: this needs to be refactored if we implement multiple groups (i
    // think)
    $('#searchGroups').on('repeatrowadded', '.grouptable', function(evt) {
        var $groupDiv = $(this).closest('.searchgroup');
        showGroupingSelectorIfNecessary($groupDiv);
    });
    $('#searchGroups').on('repeatrowdeleted', '.grouptable', function(evt) {
        var $groupDiv = $(this).closest('.searchgroup');
        showGroupingSelectorIfNecessary($groupDiv);
    });

    // mimic combobox - show complete list when user clicks down-arrow
    $('#searchGroups').on("click", ".down-arrow", function() {
        $(this).siblings('.projectcombo, .collectioncombo').focus().autocomplete("search", "");
    });

    // if coming here via the backbutton the submit button may be disabled, so
    // we force it to be enabled
    $('#searchButton').prop('disabled', false);

    // perform load handlers on anything currently in the dom (e.g. stuff added
    // by our backbutton hack, or from the server via 'refine your search' or
    // INPUT action)
    sectionLoaded($('#searchGroups')[0]);
}

// register any jquery widgets and/or perform any operations withen the
// specified context. This is somewhat akin to body.onload(). The 'context'
// element could be either: row-added back-button hack, or via server-side 
// INPUT action.
//
// NOTE: if all you want to do is register an event handler, your best bet is to
// do so via jQuery.on().
function sectionLoaded(context) {
    TDAR.common.applyWatermarks(context);

    // HACK: registering datetype fields if any were created. We should just
    // make a smarter validator rule
    if ($('.coverageDateType', context).length) {
        TDAR.common.prepareDateFields($('.coverageDateType', context));
    }

    // register any treeviews
    $('.tdar-treeview', context).treeview({
        collapsed : true
    });

    // register any person autocomplete fields (don't cull nonusers, don't show
    // the 'create person' option)
    var $personAutoFields = $('.nameAutoComplete', context);
    if ($personAutoFields.length) {
        TDAR.autocomplete.applyPersonAutoComplete($personAutoFields, false, false);
    }

    // similar setup for institution autocompletes
    var $institutionAutoFields = $('.institutionAutoComplete', context);
    if ($institutionAutoFields.length) {
        TDAR.autocomplete.applyInstitutionAutocomplete($institutionAutoFields, false);
    }

    $('.datepicker', context).datepicker({
        dateFormat : 'm/d/y'
    });

    // collection, project combo boxes
    TDAR.autocomplete.applyResourceAutocomplete($('.projectcombo', context), "PROJECT");
    TDAR.autocomplete.applyCollectionAutocomplete($('.collectioncombo', context), {
        minLength : 0
    }, {});

    $('#searchGroups').find('.searchgroup').each(function(){
        showGroupingSelectorIfNecessary($(this));
    });
}

function _setDefaultTerm(obj) {
    // console.log("adding new term");
}


// replace *all* occurances of map key with map value in element's attribute
// value
function replaceAttributeTemplate($elem, attr, map) {
    var oldstr = $elem.attr(attr);
    if (typeof oldstr == 'undefined' || newstr == false)
        return;
    var newstr = $elem.attr(attr);

    for ( var key in map) {
        newstr = newstr.split(key).join(map[key]);
        // console.trace("processing key:" + key + " was:" + oldstr + " now:" +
        // newstr);
    }
    $elem.attr(attr, newstr);
}

function updateAttributesForRow($root, groupnum, rownum) {
    // todo: *[id], *[name] instead
    var map = {
            "{groupid}" : groupnum,
            "{termid}" : rownum};
    $.each($root.find('*'), function(ignored, elem) {
        // don't update all attributes, just id, name, for, etc.
        $.each([ "id", "name", "for", "autocompleteIdElement", "autocompleteParentElement" ], function(ignored, attrName) {
            replaceAttributeTemplate($(elem), attrName, map);
        });
    });
}

function showGroupingSelectorIfNecessary($groupDiv) {
    // if more than one term, show the grouping selector
    var $visibleRows = $('.grouptable .repeat-row:visible', $groupDiv);
    var $groupingControl = $groupDiv.find(".groupingSelectDiv");
    if($visibleRows.length > 1) {
        $groupingControl.find('select').show();
        $groupingControl.addClass("in");
    } else {
        $groupingControl.removeClass("in");
        $groupingControl.find('select').hide();
        
    }
//    $('.groupingSelectDiv', $groupDiv).toggle($visibleRows.length > 1);
}


function _serializeFormState() {
    $("#searchGroups").submit(function() {
        var frm = this;
        var $frm = $(this);

        // modify attribute to reflect *current* value of property
        setAttributesToPropValue($('option', frm), "selected");
        setAttributesToPropValue($('input[type=checkbox]', frm), "checked");
        // Note: in theory radio buttons and disabled items need similar 
        // treatment, but they aren't used in this form

        $("input[type=text],input[type=hidden]", frm).each(function(idx, inputElem) {
            $(inputElem).attr("value", inputElem.value);
        });
        
        //don't persist dom elements created by google map api
        console.log("clearing map information")
        $('#large-google-map').removeData();
        $('#large-google-map').empty("div");
        
        $("#autosave").val($frm.html());
        
        //if doing resource search, clear collection search field so we aren't confused about which tab to display
        $("#queryField").val("");
        
        
    });
}

// set the default attribute value of each selected form element based on the
// current state of the boolean property of the same name.
// this really only makes sense for elements that have boolean properties,
// e.g.'checked', 'selected', or 'disabled'
function setAttributesToPropValue($formElements, booleanPropertyName) {
    // assumption: the property named ${booleanPropertyName} is a boolean
    // assumption: the attribute named ${booleanPropertyName} is a string, and
    // the only acceptable value (if exists) is
    // ${booleanPropertyName}="${booleanPropertyName}"
    $formElements.each(function(idx, elem) {
        var $elem = $(elem);
        if ($elem.prop(booleanPropertyName)) {
            $elem.attr(booleanPropertyName, booleanPropertyName);
            console.log("setting %s attribute for elem:%s", booleanPropertyName, elem);
        } else {
            $elem.removeAttr(booleanPropertyName);
            console.log("removing %s attribute for elem:%s", booleanPropertyName, elem);
        }
    });
}

return {
    serializeFormState: _serializeFormState,
    initAdvancedSearch: _initAdvancedSearch,
    setDefaultTerm: _setDefaultTerm
};
})();