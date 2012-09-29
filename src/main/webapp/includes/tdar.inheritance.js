// INHERITANCE

/*
 * DOWNWARD INHERITANCE SUPPORT
 */
var indexExclusions = [ 'investigationTypeIds', 'approvedSiteTypeKeywordIds', 'materialKeywordIds', 'approvedCultureKeywordIds' ];

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
            investigationTypeIds : $.map(rawJson.investigationTypes, function(v) {
                return v.id;
            }) || []
        },
        siteInformation : {
            siteNameKeywords : $.map(rawJson.siteNameKeywords, function(v) {
                return v.label;
            }),
            approvedSiteTypeKeywordIds : $.map(rawJson.approvedSiteTypeKeywords, function(v) {
                return v.id;
            }) || [],
            uncontrolledSiteTypeKeywords : $.map(rawJson.uncontrolledSiteTypeKeywords, function(v) {
                return v.label;
            })
        },
        materialInformation : {
            materialKeywordIds : $.map(rawJson.materialKeywords, function(v) {
                return v.id;
            }) || []
        },
        culturalInformation : {
            approvedCultureKeywordIds : $.map(rawJson.approvedCultureKeywords, function(v) {
                return v.id;
            }) || [],
            uncontrolledCultureKeywords : $.map(rawJson.uncontrolledCultureKeywords, function(v) {
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
        resourceAnnotations : rawJson.resourceAnnotations,
        noteInformation : {
            resourceNotes : rawJson.resourceNotes
        },
        collectionInformation : {
            sourceCollections : rawJson.sourceCollections,
            relatedComparativeCollections : rawJson.relatedComparativeCollections,

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

// disable a section: disable inputs, but also make section look disabled by
// graying labels and removing edit controls
function disableSection(idSelector) {
    
    $(':input', idSelector).not(".alwaysEnabled").prop('disabled', true);
    $('label', idSelector).not(".alwaysEnabled").addClass('disabled');
    $('.addAnother, .minus', idSelector).hide();
}

function enableSection(idSelector) {
    $(':input', idSelector).prop('disabled', false);
    $('label', idSelector).removeClass('disabled');
    $('.addAnother, .minus', idSelector).show();
}

// remove all but the first item of a repeatrow table.
function resetRepeatRowTable(id, newSize) {
    var table = $('#' + id);
    table.hide();
    if (!newSize)
        newSize = 1;
    table.find("tbody tr:not(:first)").remove();
    // change the id/name for each element in first row that matches _num_
    // format to _0_
    var firstRow = table.find("tbody tr:first");
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
//            console.log(id + " is now " + newid);
            $(v).attr("id", newid);
        }
        if (name) {
            var newname = name.replace(rex, replacement);
//            console.log(name + " is now " + newname);
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

// return true if the repeatrows contained in the selector match the list of
// strings
// FIXME: these are terrible function names
function inheritingRepeatRowsIsSafe(rootElementSelector, values) {
    var repeatRowValues = $.map($('input[type=text]', rootElementSelector), function(v, i) {
        if ($(v).val())
            return $(v).val();
    });
    return repeatRowValues.length === 0 || $.compareArray(repeatRowValues, values);
}

// FIXME: these are terrible function names
// return true if this section can 'safely' inherit specified values. 'safe'
// means that the target values are empty or the same
// as the incoming values.
function inheritingCheckboxesIsSafe(rootElementSelector, values) {
    var checkedValues = $.map($(':checkbox:checked', rootElementSelector), function(v, i) {
        return $(v).val();
    });
    var isSafe = checkedValues.length === 0 || $.compareArray(checkedValues, values);
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
    return formVals.length === 0 || $.compareArray(jsonVals, formVals, false); // don't
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
    if (joinedFieldValues === "")
        return true;

    // not okay to populate if the incoming list is a different size as the
    // current list
    $tableRows = $('tr', '#coverageTable');
    if (temporalInformation.coverageDates.length != $tableRows.length)
        return false;

    // at this point it's we need to compare the contents of the form vs.
    // incoming coverage dates
    var concatTemporalInformation = $.map(temporalInformation.coverageDates, function(val, i) {
        return "" + val.startDate + val.endDate + val.description;
    }).join("");
    var concatRowFields = $.map($tableRows, function(rowElem, i) {
        var concatRow = $('.coverageStartYear', rowElem).val();
        concatRow += $('.coverageEndYear', rowElem).val();
        concatRow += $('.coverageDescription', rowElem).val();
        return concatRow;
    }).join("");

    return concatTemporalInformation === concatRowFields;

}

function inheritInformation(formId, json, sectionId, tableId) {
    disableSection(sectionId);
    clearFormSection(sectionId);
    if (tableId != undefined) {
        if (document.getElementById("uncontrolled" + tableId + "Table") != undefined) {
            resetRepeatRowTable('uncontrolled' + tableId + 'Table', json['uncontrolled' + tableId + 's'].length);
        }
        if (document.getElementById("approved" + tableId + "Table") != undefined) {
            resetRepeatRowTable('approved' + tableId + 'Table', json['approved' + tableId + 's'].length);
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
    resetRepeatRowTable('siteNameKeywordTable', json.siteInformation['siteNameKeywords'].length);
    resetRepeatRowTable('uncontrolledSiteTypeKeywordTable', json.siteInformation['uncontrolledSiteTypeKeywords'].length);
    populateSection(formId, json.siteInformation);
}

function inheritIdentifierInformation(formId, json) {
    disableSection('#divIdentifiers');
    clearFormSection('#divIdentifiers');
    resetRepeatRowTable('resourceAnnotationsTable', json.resourceAnnotations.length);
    populateSection(formId, json);
}

function inheritCollectionInformation(formId, json) {
    disableSection('#relatedCollectionsSection');
    clearFormSection('#relatedCollectionsSection');
    resetRepeatRowTable('sourceCollectionTable', json.collectionInformation['relatedComparativeCollections'].length);
    resetRepeatRowTable('relatedComparativeCitationTable', json.collectionInformation['sourceCollections'].length);
    populateSection(formId, json.collectionInformation);
}

function inheritNoteInformation(formId, json) {
    disableSection('#resourceNoteSection');
    clearFormSection('#resourceNoteSection');
    resetRepeatRowTable('resourceNoteTable', json.noteInformation['resourceNotes'].length);
    populateSection(formId, json.noteInformation);
}

function inheritSpatialInformation(formId, json) {
    disableSection('#divSpatialInformation');
    disableMap();

    clearFormSection('#divSpatialInformation');
    resetRepeatRowTable('geographicKeywordTable', json.spatialInformation['geographicKeywords'].length);
    populateSection(formId, json.spatialInformation);

    // clear the existing redbox and draw new one;
    if (GZoomControl.G.oZoomArea) {
        // TODO: reset the map to default zoom and default location
        GZoomControl.G.oMap.removeOverlay(GZoomControl.G.oZoomArea);
    }
    drawMBR();
    populateLatLongTextFields();
}

function inheritTemporalInformation(formId, json) {
    var sectionId = '#divTemporalInformation';
    disableSection(sectionId);
    clearFormSection(sectionId);
    resetRepeatRowTable('temporalKeywordTable', json.temporalInformation.temporalKeywords.length);
    resetRepeatRowTable('coverageTable', json.temporalInformation.coverageDates.length);
    populateSection(formId, json.temporalInformation);
}

function bindCheckboxToInheritSection(cbSelector, divSelector, isSafeCallback, inheritSectionCallback, enableSectionCallback) {
    $(cbSelector).change(function(e) {
        var cb = this;
        var divid = divSelector;
        var proceed = true;
        if ($(cb).is(":checked")) {
            // check if inheriting would overrwrite existing
            // values
            var isSafe = isSafeCallback();
            if (!isSafe) {
                proceed = confirm("Inheriting from '" + htmlEncode(project.title) + "' will overwrite existing values. Continue?");
                if (!proceed) {
                    $(cb).removeAttr("checked");
                }
            }
            if (proceed) {
                inheritSectionCallback();
            }
        } else {
//            console.log(divid + " cleared");
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
//        console.log('project changed. new value:' + $(sel).val());
        if ($(sel).val() != '' && $(sel).val() > 0) {
//            console.log('about to make ajax call for project info');
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
    var $cbSelectAllInheritance = $("#cbSelectAllInheritance");

    // prime the "im-busy-dont-bother-me" flag on select-all checkbox.
    $cbSelectAllInheritance.data('isUpdatingSections', false);

    // FIXME: forward-references to function statements are not advised. replace
    // with forward-reference to function expression/variable?
    $cbSelectAllInheritance.click(selectAllInheritanceClicked);
}

function processInheritance(formId) {
    // ---- bind inheritance tracking checkboxes

    bindCheckboxToInheritSection('#cbInheritingSiteInformation', '#divSiteInformation', function() {
        var allKeywords = json.siteInformation.siteNameKeywords.concat(json.siteInformation.uncontrolledSiteTypeKeywords);
        return inheritingCheckboxesIsSafe('#divSiteInformation', json.siteInformation.approvedSiteTypeKeywordIds)
                && inheritingRepeatRowsIsSafe('#divSiteInformation', allKeywords);
    }, function() {
        inheritSiteInformation(formId, json);
    });

    bindCheckboxToInheritSection('#cbInheritingTemporalInformation', '#divTemporalInformation', function() {
        return inheritingRepeatRowsIsSafe('#temporalKeywordTable', json.temporalInformation.temporalKeywords)
                && inheritingDatesIsSafe('#divTemporalInformation', json.temporalInformation);
    }, function() {
        inheritTemporalInformation(formId, json);
    });

    bindCheckboxToInheritSection('#cbInheritingCulturalInformation', '#divCulturalInformation', function() {
        return inheritingCheckboxesIsSafe('#divCulturalInformation', json.culturalInformation.approvedCultureKeywordIds)
                && inheritingRepeatRowsIsSafe('#divCulturalInformation', json.culturalInformation.uncontrolledCultureKeywords);
    }, function() {
        inheritInformation(formId, json.culturalInformation, "#divCulturalInformation", "CultureKeyword");
    });

    bindCheckboxToInheritSection('#cbInheritingOtherInformation', '#divOtherInformation', function() {
        return inheritingRepeatRowsIsSafe('#divOtherInformation', json.otherInformation.otherKeywords);
    }, function() {
        inheritInformation(formId, json.otherInformation, "#divOtherInformation", "otherKeyword");
    });

    bindCheckboxToInheritSection('#cbInheritingInvestigationInformation', '#divInvestigationInformation', function() {
        return inheritingCheckboxesIsSafe('#divInvestigationInformation', json.investigationInformation.investigationTypeIds);
    }, function() {
        inheritInformation(formId, json.investigationInformation, '#divInvestigationInformation');
    });

    bindCheckboxToInheritSection('#cbInheritingMaterialInformation', '#divMaterialInformation', function() {
        return inheritingCheckboxesIsSafe('#divMaterialInformation', json.materialInformation.materialKeywordIds);
    }, function() {
        inheritInformation(formId, json.materialInformation, '#divMaterialInformation');
    });

    bindCheckboxToInheritSection('#cbInheritingNoteInformation', '#resourceNoteSection', function() {
        return inheritingRepeatRowsIsSafe('#resourceNoteSection', json.noteInformation);
    }, function() {
        inheritNoteInformation(formId, json);
    });

    bindCheckboxToInheritSection('#cbInheritingCollectionInformation', '#relatedCollectionsSection', function() {
        return inheritingRepeatRowsIsSafe('#relatedCollectionsSection', json.collectionInformation.sourceCollections)
                && inheritingRepeatRowsIsSafe('#relatedCollectionsSection', json.collectionInformation.relatedComparativeCollections);
    }, function() {
        inheritCollectionInformation(formId, json);
    });

    bindCheckboxToInheritSection('#cbInheritingIdentifierInformation', '#divIdentifiers', function() {
        // flatten json to array of values [key, val, key, val, ...], and
        // compare to field values.
        var vals = [];
        $.each(json.resourceAnnotations, function(i, annotation) {
            vals.push(annotation.resourceAnnotationKey.key); // identifier
                                                                // key
            vals.push(annotation.value); // identifier value;
        });
        return inheritingRepeatRowsIsSafe('#divIdentifiers', vals);
    }, function() {
        inheritIdentifierInformation(formId, json);
    });

    bindCheckboxToInheritSection('#cbInheritingSpatialInformation', '#divSpatialInformation', function() {
        return inheritingMapIsSafe('#divSpatialInformation', json.spatialInformation)
                && inheritingRepeatRowsIsSafe('#geographicKeywordTable', json.spatialInformation.geographicKeywords);
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
    enableSection('#divIdentifiers');
    enableSection('#relatedCollectionsSection');
    enableSection('#resourceNoteSection');
}

function updateInheritableSections(json) {
    //HACK: temporary fix for TDAR-2268 - our form populate js is overwriting the ID field with data.id
    var jsonid = json.id;
    json.id = null;
    delete(json.id);

    // indicate in each section which project the section will inherit from.
    var labelText = "Inherit values from parent project";
    var selectedProjectName = "Select a project above to enable inheritance";
    if (jsonid > 0) {
        labelText = 'Inherit values from parent project "' + TDAR.ellipsify(json.title, 60) + '"';
        selectedProjectName = "Inherit metadata from " + json.title;
    }
    $('.inheritlabel label').text(labelText);
    $('#lblCurrentlySelectedProject').text(selectedProjectName);

    // show or hide the text of each inheritable section based on checkbox
    // state.
    if ($('#cbInheritingInvestigationInformation').is(':checked')) {
        inheritInformation(formId, json.investigationInformation, '#divInvestigationInformation');
    }

    if ($('#cbInheritingSiteInformation').is(':checked')) {
        inheritSiteInformation(formId, json);
    }

    if ($('#cbInheritingMaterialInformation').is(':checked')) {
        inheritInformation(formId, json.materialInformation, '#divMaterialInformation');
    }

    if ($('#cbInheritingCulturalInformation').is(':checked')) {
        inheritInformation(formId, json.culturalInformation, "#divCulturalInformation", "CultureKeyword");
    }

    if ($('#cbInheritingSpatialInformation').is(':checked')) {
        inheritSpatialInformation(formId, json);
    }

    if ($('#cbInheritingTemporalInformation').is(':checked')) {
        inheritTemporalInformation(formId, json);
    }

    if ($('#cbInheritingIdentifierInformation').is(':checked')) {
        inheritIdentifierInformation(formId, json);
    }
    if ($('#cbInheritingCollectionInformation').is(':checked')) {
        inheritCollectionInformation(formId, json);
    }
    if ($('#cbInheritingNoteInformation').is(':checked')) {
        inheritNoteInformation(formId, json);
    }

    if ($('#cbInheritingOtherInformation').is(':checked')) {
        inheritInformation(formId, json.otherInformation, "#divOtherInformation", "otherKeyword");
    }
}

function selectAllInheritanceClicked() {
    var $elem = $("#cbSelectAllInheritance");
    try {
        $elem.data('isUpdatingSections', true);

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

    } finally {
        //never leave this checkbox in indeterminate state
        $elem.data('isUpdatingSections', false);
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
        $cbSelectAllInheritance.removeAttr('checked');
        disableSection('#divInheritFromProject');
    }
}

//Enforce the correct state of 'inherit all' checkbox. It should only be checked iff. every inherit-section is checked.
function updateSelectAllCheckboxState() {
    var $cbSelectAllInheritance = $('#cbSelectAllInheritance');
    if (!$cbSelectAllInheritance.data('isUpdatingSections')) {
        var $uncheckedBoxes = $('.inheritlabel input[type=checkbox]').not(":checked");
        $cbSelectAllInheritance.prop('checked', $uncheckedBoxes.length === 0);
    }
}
