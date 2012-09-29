function initAdvancedSearch() {
    setupFormValidate('#searchGroups');

    // when user changes searchType: swap out the term ui snippet
    $('#searchGroups').on('change', '.searchType', function(evt) {
        // console.log("change event on %s", this.id);
        var searchType = $(this).val();

        // get a copy of the template term
        var row = $('div.' + searchType, '#template').clone();
        // figure out which group, rownum we are in, and then update attribute
        // values of the cloned row
        var groupnum = $(this).closest(".grouptable").data("groupnum");
        var rownum = $(this).closest("tbody").children(":visible").index($(this).closest("tr"));
        updateAttributesForRow($(row), groupnum, rownum);

        // get parent row, remove term div
        var $tr = $(this).closest('tr');

        var $term = $('.term', $tr);
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

        $('.searchfor', $tr).html("");
        $('.searchfor', $tr).append(row);
        if ($(row).hasClass('retain')) {
            $(':text:first', row).val(oldval);
        }

        sectionLoaded(row);
    });

    // after rows added, replace attribute values
    $('.grouptable').on('row-added', function(evt, $row, idx) {
        // console.log('added row:' + $row + ' p2:' + idx);
        var $table = $(this);
        updateAttributesForRow($row, $table.data('groupnum'), idx);
    });

    // more handling of added/removed rows
    // TODO: this needs to be refactored if we implement multiple groups (i
    // think)
    $('#searchGroups').on('row-added', '.grouptable', function(evt) {
        var $groupDiv = $(this).closest('.searchgroup');
        showGroupingSelectorIfNecessary($groupDiv);
    });
    $('#searchGroups').on('row-removed', '.grouptable', function(evt) {
        var $groupDiv = $(this).closest('.searchgroup');
        showGroupingSelectorIfNecessary($groupDiv);
    });

    // register autocompletes
    // FIXME: make our own combobox jquery plugin (or jquery ui widget)
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
    applyWatermarks(context);

    // HACK: registering datetype fields if any were created. We should just
    // make a smarter validator rule
    if ($('.coverageDateType', context).length) {
        prepareDateFields($('.coverageDateType', context));
    }

    // register any treeviews
    $('.treeview', context).treeview({
        collapsed : true
    });

    // register any person autocomplete fields (don't cull nonusers, don't show
    // the 'create person' option)
    var $personAutoFields = $('.nameAutoComplete', context);
    if ($personAutoFields.length) {
        applyPersonAutoComplete($personAutoFields, false, false);
    }

    // similar setup for institution autocompletes
    var $institutionAutoFields = $('.institutionAutoComplete', context);
    if ($institutionAutoFields.length) {
        applyInstitutionAutocomplete($institutionAutoFields, false);
    }

    $('.datepicker', context).datepicker({
        dateFormat : 'm/d/y'
    });

    // collection, project combo boxes
    applyResourceAutocomplete($('.projectcombo', context), "PROJECT");
    applyCollectionAutocomplete($('.collectioncombo', context), {
        minLength : 0
    }, {});

}

// fixme: refactor deleterow
// remove the closest tr from $elem, and then fire a row-removed event
function removeRow($elem) {
    var $tr = $elem.closest('tr');
    var $tbody = $tr.parent(); // $tbody/$table will be same if table has not
                                // tbody tag (only a scenario for IE i think)
    var $table = $tbody.closest('table');
    var visibleRows = $('tr:visible', $tbody).length;
    if (visibleRows > 1) {
        $tr.remove();
    } else {
        cleanRow($tr);
    }
    // TODO: i'm assuming we don't need/want reference to row we just
    // removed...can we think of a reason otherwise?
    $table.trigger('row-removed', [ $tbody.closest('table'), visibleRows ]);
}

// fixme: refactor clearRow
function cleanRow($tr) {
    // console.warn("incomplete!!");
    $('input[type=text],textarea,hidden').val("");
}

function setDefaultTerm(obj) {
    // console.log("adding new term");
}

// copy a template row from a table, add it to the end of the table
function addRowFromTemplate(tableSelector) {
    var $table = $(tableSelector);
    var $sourceRow = $('#template .basicTemplate');
    var $clonedRow = $sourceRow.clone();
    $clonedRow.removeClass('template');
    // console.log($clonedRow);
    var $tbody = $('> tbody:last', $table);
    $tbody.append($clonedRow);
    $("select", $clonedRow).trigger("change");
    
    var rownum = $("tr.termrow", $table).length - 1;
    $table.trigger('row-added', [ $clonedRow, rownum ]);
    return $clonedRow;
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
    $.each($root.find('*'), function(ignored, elem) {
        var map = {
            "{groupid}" : groupnum,
            "{termid}" : rownum
        };
        // don't update all attributes, just id, name, for, etc.
        $.each([ "id", "name", "for", "autocompleteIdElement", "autocompleteParentElement" ], function(ignored, attrName) {
            replaceAttributeTemplate($(elem), attrName, map);
        });
    });
}

function showGroupingSelectorIfNecessary($groupDiv) {
    // if more than one term, show the grouping selector
    $visibleRows = $('.grouptable tr:visible', $groupDiv);
    $('.groupingSelectDiv', $groupDiv).toggle($visibleRows.length > 1);
}

function resetAdvancedSearchForm() {
    $('#groupTable0 > tbody > tr').each(function() {
        removeRow($(this));
    });
    $("input[type=checkbox]").prop("checked", false);
    $("select,input[type=hidden],input[type=text]").val("");
    try {
        map.removeOverlay(GZoomControl.G.oZoomArea);
    } catch (e) {
    }
    $('.searchType', '#searchGroups').val("ALL_FIELDS");
    $('.searchType', '#searchGroups').change();
}

function serializeFormState() {
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