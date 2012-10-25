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
    var formVals = $.map([ 
                     $('#minx').val(),
                     $('#miny').val(), 
                     $('#maxx').val(),
                     $('#maxy').val()
                    ], function(v){if(v!=="") return parseFloat(v)});

    return  !formVals.length || $.compareArray(jsonVals, formVals, false);
}

// return whether it's "safe" to populate the temporal information section with
// the supplied temporalInformation
// we define "safe" to mean that section is either currently blank or that the
// supplied temporalInformation is the same as what is already on the form.
function inheritingDatesIsSafe(rootElementSelector, temporalInformation) {
    // are all the fields in this section blank?
    var $coverageTextFields = $('input:text', '#coverageDateRepeatable');
    var joinedFieldValues = $coverageTextFields.map(function() {
        return $(this).val();
    }).toArray().join("");

    // okay to populate if if the form section is blank
    if (joinedFieldValues === "")
        return true;

    // not okay to populate if the incoming list is a different size as the
    // current list
    $tableRows = $('.repeat-row', '#coverageDateRepeatable');
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
    console.debug("inheritInformation(formId:%s, json:%s, sectionId:%s, tableId:%s)", formId, json, sectionId, tableId);
    clearFormSection(sectionId);
    if (tableId != undefined) {
        if (document.getElementById("uncontrolled" + tableId + "Repeatable") != undefined) {
            TDAR.inheritance.resetRepeatable("#" + 'uncontrolled' + tableId + 'Repeatable', json['uncontrolled' + tableId].length);
        }
        if (document.getElementById("approved" + tableId + "Repeatable") != undefined) {
            TDAR.inheritance.resetRepeatable("#" + 'approved' + tableId + 'Repeatable', json['approved' + tableId].length);
        }
        var simpleId = tableId;
        simpleId[0] = simpleId[0].toLowerCase();
        if (document.getElementById(simpleId + "Repeatable") != undefined) {
            TDAR.inheritance.resetRepeatable("#" + simpleId + 'Repeatable', json[simpleId].length);
        }
    }
    populateSection(formId, json);
    disableSection(sectionId);
}

//

function inheritSiteInformation(formId, json) {
    clearFormSection('#divSiteInformation');
    TDAR.inheritance.resetRepeatable('#siteNameKeywordsRepeatable', json.siteInformation['siteNameKeywords'].length);
    TDAR.inheritance.resetRepeatable('#uncontrolledSiteTypeKeywordsRepeatable', json.siteInformation['uncontrolledSiteTypeKeywords'].length);
    populateSection(formId, json.siteInformation);
    disableSection('#divSiteInformation');
}

function inheritIdentifierInformation(formId, json) {
    clearFormSection('#divIdentifiers');
    TDAR.inheritance.resetRepeatable('#resourceAnnotationsTable', json.resourceAnnotations.length);
    populateSection(formId, json);
    disableSection('#divIdentifiers');
}

function inheritCollectionInformation(formId, json) {
    disableSection('#relatedCollectionsSection');
    clearFormSection('#relatedCollectionsSection');
    //resetRepeatRowTable('sourceCollectionTable', json.collectionInformation['relatedComparativeCollections'].length);
    //resetRepeatRowTable('relatedComparativeCitationTable', json.collectionInformation['sourceCollections'].length);
    populateSection(formId, json.collectionInformation);
}

function inheritNoteInformation(formId, json) {
    clearFormSection('#resourceNoteSection');
    TDAR.inheritance.resetRepeatable('#resourceNoteSection', json.noteInformation['resourceNotes'].length);
    populateSection(formId, json.noteInformation);
    disableSection('#resourceNoteSection');
}

function inheritSpatialInformation(formId, json) {
    var mapdiv = $('#editmapv3')[0];
    clearFormSection('#divSpatialInformation');
    TDAR.inheritance.resetRepeatable('#geographicKeywordsRepeatable', json.spatialInformation['geographicKeywords'].length);
    populateSection(formId, json.spatialInformation);
    disableSection('#divSpatialInformation');

    // clear the existing redbox and draw new one;
    populateLatLongTextFields();
    
    var si = json.spatialInformation;
    TDAR.maps.updateResourceRect(mapdiv,  si.miny, si.minx, si.maxy, si.maxx);
}

function inheritTemporalInformation(formId, json) {
    var sectionId = '#divTemporalInformation';
    clearFormSection(sectionId);
    TDAR.inheritance.resetRepeatable('#temporalKeywordsRepeatable', json.temporalInformation.temporalKeywords.length);
    TDAR.inheritance.resetRepeatable('#coverageDateRepeatable', json.temporalInformation.coverageDates.length);
    populateSection(formId, json.temporalInformation);
    disableSection(sectionId);
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

function applyInheritance(project, formSelector) {
    var $form = $(formSelector);
    //collection of 'options' objects for each inheritance section. options contain info about 
    //the a section (checkbox selector, div selector,  callbacks for isSafe, inheritSection, enableSection);
    $form.data("inheritOptionsList", []);
    
    //hack:  formId is no longer global, and updateInheritableSections() needs it...
    var formId = $(formSelector).attr("id");
    
    
    
    // if we are editing, set up the initial form values
    if (project) {
        json = convertToFormJson(project);
        updateInheritableSections(json, formId);
    }

    // update the inherited form values when project selection changes.
    $('#projectId').change(function(e) {
        var sel = this;
        $('.open-project', '#t-project').remove();
        
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
            updateInheritableSections(json, formId);
        }
        enableOrDisableInheritAllSection();
        updateInheritanceCheckboxes();
    });
    
    
    updateInheritanceCheckboxes();
    enableOrDisableInheritAllSection();
    processInheritance(formSelector);
    var $cbSelectAllInheritance = $("#cbSelectAllInheritance");

    // prime the "im-busy-dont-bother-me" flag on select-all checkbox.
    $cbSelectAllInheritance.data('isUpdatingSections', false);

    // FIXME: forward-references to function statements are not advised. replace
    // with forward-reference to function expression/variable?
    $cbSelectAllInheritance.click(selectAllInheritanceClicked);
}


//update the project json variable and update the inherited sections
function projectChangedCallback(data) {
    project = data;
    
    // if user picked blank option, then clear the sections
    if (!project.id) {
        project = getBlankProject();
    } else  if (project.resourceType == 'INDEPENDENT_RESOURCES_PROJECT') {
        project = getBlankProject();
    } else {
        $('#t-project').append('<p class="open-project"><a class="btn btn-small" target="_project" href="/project/' + project.id + '">open project in new window</a></p>');
    }

    json = convertToFormJson(project);
    var formId = $('#projectId').closest('form').attr("id");
    updateInheritableSections(json, formId);
}


function processInheritance(formId) {
    // ---- bind inheritance tracking checkboxes
    var registerSection = TDAR.inheritance.registerInheritSection;

    
//    bindCheckboxToInheritSection(_options.cbSelector, _options.divSelector, _options.isSafeCallback, 
//            _options.inheritSectionCallback, _options.enableSectionCallback);     

    registerSection({
            cbSelector:'#cbInheritingSiteInformation', 
            divSelector:'#divSiteInformation',
            mappedData: "siteInformation", //curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback: function() {
                var allKeywords = json.siteInformation.siteNameKeywords.concat(json.siteInformation.uncontrolledSiteTypeKeywords);
                return inheritingCheckboxesIsSafe('#divSiteInformation', json.siteInformation.approvedSiteTypeKeywordIds)
                        && inheritingRepeatRowsIsSafe('#divSiteInformation', allKeywords);
    
            }, 
            inheritSectionCallback: function() {
                inheritSiteInformation(formId, json);
            }
    });
    
//    bindCheckboxToInheritSection('#cbInheritingSiteInformation', '#divSiteInformation', function() {
//        var allKeywords = json.siteInformation.siteNameKeywords.concat(json.siteInformation.uncontrolledSiteTypeKeywords);
//        return inheritingCheckboxesIsSafe('#divSiteInformation', json.siteInformation.approvedSiteTypeKeywordIds)
//                && inheritingRepeatRowsIsSafe('#divSiteInformation', allKeywords);
//    }, function() {
//        inheritSiteInformation(formId, json);
//    });

    bindCheckboxToInheritSection('#cbInheritingTemporalInformation', '#divTemporalInformation', function() {
        return inheritingRepeatRowsIsSafe('#temporalKeywordsRepeatable', json.temporalInformation.temporalKeywords)
                && inheritingDatesIsSafe('#divTemporalInformation', json.temporalInformation);
    }, function() {
        inheritTemporalInformation(formId, json);
    });

    bindCheckboxToInheritSection('#cbInheritingCulturalInformation', '#divCulturalInformation', function() {
        return inheritingCheckboxesIsSafe('#divCulturalInformation', json.culturalInformation.approvedCultureKeywordIds)
                && inheritingRepeatRowsIsSafe('#divCulturalInformation', json.culturalInformation.uncontrolledCultureKeywords);
    }, function() {
        inheritInformation(formId, json.culturalInformation, "#divCulturalInformation", "CultureKeywords");
    });

    bindCheckboxToInheritSection('#cbInheritingOtherInformation', '#divOtherInformation', function() {
        return inheritingRepeatRowsIsSafe('#divOtherInformation', json.otherInformation.otherKeywords);
    }, function() {
        inheritInformation(formId, json.otherInformation, "#divOtherInformation", "otherKeywords");
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
                && inheritingRepeatRowsIsSafe('#geographicKeywordsRepeatable', json.spatialInformation.geographicKeywords);
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
        $('.divInheritSection').find('label').addClass('disabled');
        $('.divInheritSection').find(':checkbox').prop('disabled', true);
        $('.divInheritSection').find(':checkbox').prop('checked', false);
        enableAll();
    } else {
        $('.divInheritSection').find('label').removeClass('disabled');
        $('.divInheritSection').find(':checkbox').prop('disabled', false);
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

//todo: this duplicates code (see all the calls to bindCheckbox); use  inheritOptionsList instead
function updateInheritableSections(json, formId) {
    //HACK: temporary fix for TDAR-2268 - our form populate js is overwriting the ID field with data.id
    var jsonid = json.id;
//    console.log(json);
    json.id = null;
    delete(json.id);

    // indicate in each section which project the section will inherit from.
    var labelText = "Inherit values from parent project";
    var selectedProjectName = "Select a project above to enable inheritance";
    if (jsonid > 0) {
        labelText = 'Inherit values from parent project "' + TDAR.ellipsify(json.title, 60) + '"';
        selectedProjectName = "Inherit metadata from " + json.title;
    }
    
    //update inheritance checkbox labels with new project name (don't clobber checkbox in the process)
    $('.divInheritSection').find('label.checkbox .labeltext').text(labelText);
    
    $('#spanCurrentlySelectedProjectText').text(selectedProjectName);

    
    
    //todo:  convert other sections to use this style
    $form = $("#" + formId);
    $.each($form.data("inheritOptionsList"), function(idx, options) {
        if($(options.cbSelector).is(':checked')) {
            options.inheritSectionCallback();
        }
    });
    
    
    // show or hide the text of each inheritable section based on checkbox
    // state.
    if ($('#cbInheritingInvestigationInformation').is(':checked')) {
        inheritInformation(formId, json.investigationInformation, '#divInvestigationInformation');
    }

//    if ($('#cbInheritingSiteInformation').is(':checked')) {
//        inheritSiteInformation(formId, json);
//    }

    if ($('#cbInheritingMaterialInformation').is(':checked')) {
        inheritInformation(formId, json.materialInformation, '#divMaterialInformation');
    }

    if ($('#cbInheritingCulturalInformation').is(':checked')) {
        inheritInformation(formId, json.culturalInformation, "#divCulturalInformation", "CultureKeywords");
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
        inheritInformation(formId, json.otherInformation, "#divOtherInformation", "otherKeywords");
    }
}

function selectAllInheritanceClicked() {
    var $elem = $("#cbSelectAllInheritance");
    try {
        $elem.data('isUpdatingSections', true);

        var checked = $elem.prop('checked');
        var $sectionCheckboxes = $('.divInheritSection input[type=checkbox]');

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
        var $uncheckedBoxes = $('.divInheritSection input[type=checkbox]').not(":checked");
        $cbSelectAllInheritance.prop('checked', $uncheckedBoxes.length === 0);
    }
}

//FIXME: kill this function.
function populateLatLongTextFields() {
 $("#d_minx").val(Geo.toLon($("#minx").val()));
 $("#d_miny").val(Geo.toLat($("#miny").val()));
 $("#d_maxx").val(Geo.toLon($("#maxx").val()));
 $("#d_maxy").val(Geo.toLat($("#maxy").val()));
}

//FIXME: kill this function.
function disableMap() {
    var $mapdiv = $('#editmapv3');
    $mapdiv.addClass('opaque');
    if($mapdiv.data("resourceRect")) {
        $mapdiv.data("resourceRect").setEditable(false);
    }
}

//FIXME: kill this function.
function enableMap() {
    var $mapdiv = $('#editmapv3');
    $mapdiv.removeClass('opaque');
    if($mapdiv.data("resourceRect")) {
        $mapdiv.data("resourceRect").setEditable(true);
    }
}

TDAR.namespace("inheritance");
TDAR.inheritance = function() {
    "use strict";
    var _resetRepeatable = function(repeatableSelector, newSize) {
        $(repeatableSelector).find(".repeat-row:not(:first)").remove();
        var $firstRow = $('.repeat-row', repeatableSelector);
        resetIndexedAttributes($firstRow); 
        for(var i = 0; i < newSize - 1; i++) {
            TDAR.repeatrow.cloneSection($('.repeat-row:last', repeatableSelector)[0]);
        }
    };
    
    var _resetKeywords = function(keywords, repeatableSelector) {
        var $repeatable = $(repeatableSelector);
        _resetRepeatable(repeatableSelector, keywords.length);
    };
    
    //default function for enabling an inheritance section
    var _enableSection = function (idSelector) {
        $(':input', idSelector).prop('disabled', false);
        $('label', idSelector).removeClass('disabled');
        $('.addAnother, .minus', idSelector).show();
    }

    var _registerInheritSection = function(options) {
        var $checkbox = $(options.cbSelector);
        var $form = $checkbox.closest("form");
        var formId = $form.attr("id");
        var _options = {
                isSafeCallback: function(){return true;},
                //fixme: getObjValue(json, options.mappedData) instead?
                inheritSectionCallback: function() {inheritInformation(formId, json[_options.mappedData])}, 
                enableSectionCallback: enableSection, //fixme: move to namespace
        };
        $.extend(_options, options);
        $form.data("inheritOptionsList").push(_options);
        
        //fixme: move this to body of this function
        bindCheckboxToInheritSection(_options.cbSelector, _options.divSelector, _options.isSafeCallback, 
                _options.inheritSectionCallback, _options.enableSectionCallback);     
        
    };
    
    
    return{
        resetRepeatable: _resetRepeatable,
        resetKeywords: _resetKeywords,
        registerInheritSection: _registerInheritSection
    };
}();
