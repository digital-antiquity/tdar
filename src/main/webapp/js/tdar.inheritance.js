TDAR.inheritance = (function() {
    "use strict";

/*
 * DOWNWARD INHERITANCE SUPPORT
 */
var indexExclusions = [ 'investigationTypeIds', 'approvedSiteTypeKeywordIds', 'materialKeywordIds', 'approvedCultureKeywordIds' ];
var TYPE_PERSON = "PERSON";
var TYPE_INSTITUTION = "INSTITUTION";

    /**
     * convenience function for $.populate()
     *
     * @param elemSelector form element sent to $.populate() (NOTE: must be form element due to bug in $.populate plugin, no matter what their documentation may say)
     * @param formdata  pojo to send to $.populate()
     * @private
     */
    function _populateSection(elemSelector, formdata) {

        $(elemSelector).populate(formdata, {
            resetForm : false,
            phpNaming : false,
            phpIndices : true,
            strutsNaming : true,
            noIndicesFor : indexExclusions
        });
    }

// convert a serialized project into the json format needed by the form.
function _convertToFormJson(rawJson) {
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
            relatedComparativeCollections : rawJson.relatedComparativeCollections

        },
        otherInformation : {
            otherKeywords : $.map(rawJson.otherKeywords, function(v) {
                return v.label;
            })
        },
        creditProxies: $.map(rawJson.individualAndInstitutionalCredit, _convertCreator)
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

    //now build out individual/institutional credit
    return obj;
}
//convert creator from untranslated json to object that can then be passed to from populate plugin
function _convertCreator(raw) {
    var bPerson =  raw.creator.hasOwnProperty("lastName");
    var obj = {
        id: raw.creator.id,
        role: raw.role,
        type: bPerson ? TYPE_PERSON : TYPE_INSTITUTION,
        person: {},
        institution: {}
    };

    if(bPerson) {
        obj.person = {
            id: raw.creator.id,
            lastName: raw.creator.lastName,
            firstName: raw.creator.firstName,
            email: raw.creator.email,
            institution: {name:"", id:""}
        }
        if(raw.creator.institution) {
            obj.person.institution.name = raw.creator.institution.name;
            obj.person.institution.id = raw.creator.institution.id;
        }
    } else {
        obj.institution = {
            id: raw.creator.id,
            name: raw.creator.name
        }
    };
    console.log(obj);
    return obj;
}

// disable a section: disable inputs, but also make section look disabled by
// graying labels and removing edit controls
function _disableSection(idSelector) {
    
    $(':input', idSelector).not(".alwaysEnabled").prop('disabled', true);
    $('label', idSelector).not(".alwaysEnabled").addClass('disabled');
    $('.addAnother, .minus', idSelector).hide();
}

function _enableSection(idSelector) {
    $(':input', idSelector).prop('disabled', false);
    $('label', idSelector).removeClass('disabled');
    $('.addAnother, .minus', idSelector).show();
}

// modify id/name attribute in element and children if they follow 'indexed'
// pattern
// e.g. <input name='my_input_field[12]'> becomes <input
// name='my_input_field[0]'>
function _resetIndexedAttributes(elem) {
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
function _clearFormSection(selector) {
    // Use a whitelist of fields to minimize unintended side effects.
    $(selector).find('input:text, input:password, input:file, textarea').val('');
    // De-select any checkboxes, radios and drop-down menus
    $(':input', selector).prop('checked', false).prop('selected', false);
}

// return true if the repeatrows contained in the selector match the list of
// strings
// FIXME: these are terrible function names
function _inheritingRepeatRowsIsSafe(rootElementSelector, values) {
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
function _inheritingCheckboxesIsSafe(rootElementSelector, values) {
    var checkedValues = $.map($(':checkbox:checked', rootElementSelector), function(v, i) {
        return $(v).val();
    });
    var isSafe = checkedValues.length === 0 || $.compareArray(checkedValues, values);
    return isSafe;
}

function _inheritingMapIsSafe(rootElementSelector, spatialInformation) {
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
                    ], function(v){if(v!=="") return parseFloat(v);});

    return  !formVals.length || $.compareArray(jsonVals, formVals, false);
}

// return whether it's "safe" to populate the temporal information section with
// the supplied temporalInformation
// we define "safe" to mean that section is either currently blank or that the
// supplied temporalInformation is the same as what is already on the form.
function _inheritingDatesIsSafe(rootElementSelector, temporalInformation) {
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
    var $tableRows = $('.repeat-row', '#coverageDateRepeatable');
    if (temporalInformation.coverageDates.length !== $tableRows.length)
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


function _inheritInformation(formId, json, sectionId, tableId) {
    //console.debug("_inheritInformation(formId:%s, json:%s, sectionId:%s, tableId:%s)", formId, json, sectionId, tableId);
    _clearFormSection(sectionId);
    if (tableId !== undefined) {
        if (document.getElementById("uncontrolled" + tableId + "Repeatable") !== null) {
            TDAR.inheritance.resetRepeatable("#" + 'uncontrolled' + tableId + 'Repeatable', json['uncontrolled' + tableId].length);
        }
        if (document.getElementById("approved" + tableId + "Repeatable") !== null) {
            TDAR.inheritance.resetRepeatable("#" + 'approved' + tableId + 'Repeatable', json['approved' + tableId].length);
        }
        var simpleId = tableId;
        simpleId = simpleId[0].toLowerCase() + simpleId.substr(1);
        if (document.getElementById(simpleId + "Repeatable") !== null) {
            TDAR.inheritance.resetRepeatable("#" + simpleId + 'Repeatable', json[simpleId].length);
        }
    }
    _populateSection(formId, json);
    _disableSection(sectionId);
}

//

function _inheritSiteInformation(formId, json) {
    _clearFormSection('#divSiteInformation');
    TDAR.inheritance.resetRepeatable('#siteNameKeywordsRepeatable', json.siteInformation['siteNameKeywords'].length);
    TDAR.inheritance.resetRepeatable('#uncontrolledSiteTypeKeywordsRepeatable', json.siteInformation['uncontrolledSiteTypeKeywords'].length);
    _populateSection(formId, json.siteInformation);
    _disableSection('#divSiteInformation');
}

function _inheritCulturalInformation(formId, json) {
    //                _inheritInformation(formId, json.culturalInformation, "#divCulturalInformation", "CultureKeywords");

    _clearFormSection('#divCulturalInformation');
    TDAR.inheritance.resetRepeatable('#uncontrolledCultureKeywordsRepeatable', json.culturalInformation['uncontrolledCultureKeywords'].length);
    _populateSection(formId, json.culturalInformation);
    _disableSection('#divCulturalInformation');
}


function _inheritIdentifierInformation(formId, json) {
    _clearFormSection('#divIdentifiers');
    TDAR.inheritance.resetRepeatable('#resourceAnnotationsTable', json.resourceAnnotations.length);
    _populateSection(formId, json);
    _disableSection('#divIdentifiers');
}

function _inheritCollectionInformation(formId, json) {
    _clearFormSection('#relatedCollectionsSection');
    TDAR.inheritance.resetRepeatable('#divSourceCollectionControl', json.collectionInformation.sourceCollections.length);
    TDAR.inheritance.resetRepeatable('#divRelatedComparativeCitationControl', json.collectionInformation.sourceCollections.length);
    _populateSection(formId, json.collectionInformation);
    _disableSection('#relatedCollectionsSection');
}

function _inheritNoteInformation(formId, json) {
    _clearFormSection('#resourceNoteSection');
    TDAR.inheritance.resetRepeatable('#resourceNoteSection', json.noteInformation['resourceNotes'].length);
    _populateSection(formId, json.noteInformation);
    _disableSection('#resourceNoteSection');
}

function _inheritSpatialInformation(formId, json) {
    console.log("inherit spatial information(%s, %s)", formId, json);
    var mapdiv = $('#editmapv3')[0];
    var mapReadyCallback = function(){

        console.log("map ready callback");
        _clearFormSection('#divSpatialInformation');
        TDAR.inheritance.resetRepeatable('#geographicKeywordsRepeatable', json.spatialInformation['geographicKeywords'].length);
        _populateSection(formId, json.spatialInformation);
        _disableSection('#divSpatialInformation');

        // clear the existing redbox and draw new one;
        TDAR.maps.clearResourceRect(mapdiv);
        _populateLatLongTextFields();

        var si = json.spatialInformation;
        if(si.miny != null && si.minx != null && si.maxy != null && si.maxx != null) {
            TDAR.maps.updateResourceRect(mapdiv,  si.miny, si.minx, si.maxy, si.maxx);
        }

        _disableMap();
    };

    //need to wait until map api is ready *and* this page's map is ready.
    if(!$(mapdiv).data("gmap")) {
        $(mapdiv).one("mapready", mapReadyCallback)
    } else {
        mapReadyCallback();
    }


}

function _inheritTemporalInformation(formId, json) {
    var sectionId = '#divTemporalInformation';
    _clearFormSection(sectionId);
    TDAR.inheritance.resetRepeatable('#temporalKeywordsRepeatable', json.temporalInformation.temporalKeywords.length);
    TDAR.inheritance.resetRepeatable('#coverageDateRepeatable', json.temporalInformation.coverageDates.length);
    _populateSection(formId, json.temporalInformation);
    _disableSection(sectionId);
}


function _inheritCreditInformation(divSelector, creators) {
    _clearFormSection(divSelector);
    TDAR.inheritance.resetRepeatable(divSelector, creators.length);
    if(creators.length > 0) {
        _populateSection(divSelector, {creditProxies: creators});
        //now set the correct toggle state for eachrow
        var $proxyRows = $(divSelector).find(".repeat-row");
        $proxyRows.each(function(i, rowElem){
            if(creators[i].type === TYPE_PERSON) {
                $(rowElem).find(".creatorPerson").removeClass("hidden");
                $(rowElem).find(".creatorInstitution").addClass("hidden");

                $(rowElem).find(".personButton").addClass("active");
                $(rowElem).find(".institutionButton").removeClass("active");
            } else {

                //fixme: cmon jim, really??  there's a better way to activate one over the other
                $(rowElem).find(".creatorPerson").addClass("hidden");
                $(rowElem).find(".creatorInstitution").removeClass("hidden");

                $(rowElem).find(".personButton").removeClass("active");
                $(rowElem).find(".institutionButton").addClass("active");

            }
        });

    }

    _disableSection(divSelector);
}

function applyInheritance(formSelector) {
    var $form = $(formSelector);
    //collection of 'options' objects for each inheritance section. options contain info about
    //the a section (checkbox selector, div selector,  callbacks for isSafe, inheritSection, enableSection);
    $form.data("inheritOptionsList", []);

    //hack:  formId is no longer global, and updateInheritableSections() needs it...
    var formId = $(formSelector).attr("id");



    // if we are editing, set up the initial form values
//    if (project) {
//        json = _convertToFormJson(project);
//        _updateInheritableSections(json, formId);
//    }

    // update the inherited form values when project selection changes.
    $('#projectId').change(function(e) {
        var sel = this;
        if ($(sel).val() !== '' && $(sel).val() > 0) {
            $.ajax({
                url : getBaseURI() + "project/json",
                dataType : "jsonp",
                data : {
                    id : $(sel).val()
                },
                success : _projectChangedCallback,
                error : function(msg) {
                    console.error("error");
                },
                waitMessage: "Loading project information"
            });
        } else {
            TDAR.inheritance.project = _getBlankProject();
            TDAR.inheritance.json = _convertToFormJson(TDAR.inheritance.project);
            _updateInheritableSections(formId, TDAR.inheritance.json);
        }
        _enableOrDisableInheritAllSection();
        _updateInheritanceCheckboxes();
    });


    _updateInheritanceCheckboxes();
    _enableOrDisableInheritAllSection();
    _processInheritance(formSelector);
    var $cbSelectAllInheritance = $("#cbSelectAllInheritance");

    // prime the "im-busy-dont-bother-me" flag on select-all checkbox.
    $cbSelectAllInheritance.data('isUpdatingSections', false);

    // FIXME: forward-references to function statements are not advised. replace
    // with forward-reference to function expression/variable?
    $cbSelectAllInheritance.click(_selectAllInheritanceClicked);

    _projectChangedCallback(TDAR.inheritance.project);
}

//return skeleton project
function _getBlankProject() {
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
        "uncontrolledSiteTypeKeywords" : [],
        "individualAndInstitutionalCredit": []
    };
    return skeleton;
}


//update the project json variable and update the inherited sections
function _projectChangedCallback(data) {
    TDAR.inheritance.project = data;

    // if user picked blank option, then clear the sections
    if (!TDAR.inheritance.project.id) {
        TDAR.inheritance.project = _getBlankProject();
    } else  if (TDAR.inheritance.project.resourceType === 'INDEPENDENT_RESOURCES_PROJECT') {
        TDAR.inheritance.project = _getBlankProject();
    }

    TDAR.inheritance.json = _convertToFormJson(TDAR.inheritance.project);
    var formId = $('#projectId').closest('form').attr("id");
    _updateInheritableSections(formId, TDAR.inheritance.json);
}


function _processInheritance(formId) {
    //declare options for each inheritSection; including the top-level div, criteria for overwrite "safety", how populate controls, etc.
    var optionsList = [
        {
            //todo:  derive section name from section header
            sectionNameSelector: "#siteInfoSectionLabel",
            cbSelector : '#cbInheritingSiteInformation',
            divSelector : '#siteSection',
            mappedData : "siteInformation", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                var allKeywords = TDAR.inheritance.json.siteInformation.siteNameKeywords.concat(TDAR.inheritance.json.siteInformation.uncontrolledSiteTypeKeywords);
                return _inheritingCheckboxesIsSafe('#divSiteInformation', TDAR.inheritance.json.siteInformation.approvedSiteTypeKeywordIds) &&
                        _inheritingRepeatRowsIsSafe('#divSiteInformation', allKeywords);

            },
            inheritSectionCallback : function() {
                _inheritSiteInformation("#siteSection", TDAR.inheritance.json);
            }
        },
        {
            sectionNameSelector: "#temporalInfoSectionLabel",
            cbSelector : '#cbInheritingTemporalInformation',
            divSelector : '#temporalSection',
            mappedData : "temporalInformation", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                return _inheritingRepeatRowsIsSafe('#temporalKeywordsRepeatable', TDAR.inheritance.json.temporalInformation.temporalKeywords)
                        && _inheritingDatesIsSafe('#divTemporalInformation', TDAR.inheritance.json.temporalInformation);
            },
            inheritSectionCallback : function() {
                _inheritTemporalInformation("#temporalSection", TDAR.inheritance.json);
            }
        },
        {
            sectionNameSelector: "#culturalInfoSectionLabel",
            cbSelector : '#cbInheritingCulturalInformation',
            divSelector : '#divCulturalInformation',
            mappedData : "culturalInformation", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                return _inheritingCheckboxesIsSafe('#divCulturalInformation', TDAR.inheritance.json.culturalInformation.approvedCultureKeywordIds)
                        && _inheritingRepeatRowsIsSafe('#divCulturalInformation', TDAR.inheritance.json.culturalInformation.uncontrolledCultureKeywords);
            },
            inheritSectionCallback : function() {
                _inheritCulturalInformation('#divCulturalInformation', TDAR.inheritance.json);
            }
        },
        {
            sectionNameSelector: "#generalInfoSectionLabel",
            cbSelector : '#cbInheritingOtherInformation',
            divSelector : '#divOtherInformation',
            mappedData : "otherInformation", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                return _inheritingRepeatRowsIsSafe('#divOtherInformation', TDAR.inheritance.json.otherInformation.otherKeywords);
            },
            inheritSectionCallback : function() {
                _inheritInformation('#divOtherInformation', TDAR.inheritance.json.otherInformation, "#divOtherInformation", "otherKeywords");
            }
        },
        {
            sectionNameSelector: "#investigationInfoSectionLabel",
            cbSelector : '#cbInheritingInvestigationInformation',
            divSelector : '#divInvestigationInformation',
            mappedData : "investigationInformation", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                return _inheritingCheckboxesIsSafe('#divInvestigationInformation', TDAR.inheritance.json.investigationInformation.investigationTypeIds);
            },
            inheritSectionCallback : function() {
                _inheritInformation('#divInvestigationInformation', TDAR.inheritance.json.investigationInformation, '#divInvestigationInformation');
            }
        },
        {
            sectionNameSelector: "#materialInfoSectionLabel",
            cbSelector : '#cbInheritingMaterialInformation',
            divSelector : '#divMaterialInformation',
            mappedData : "materialInformation", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                return _inheritingCheckboxesIsSafe('#divMaterialInformation', TDAR.inheritance.json.materialInformation.materialKeywordIds);
            },
            inheritSectionCallback : function() {
                _inheritInformation('#divMaterialInformation', TDAR.inheritance.json.materialInformation, '#divMaterialInformation');
            }
        },
        {
            sectionNameSelector: "#notesInfoSectionLabel",
            cbSelector : '#cbInheritingNoteInformation',
            divSelector : '#resourceNoteSection',
            mappedData : "noteInformation", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                var $resourceNoteSection = $('#resourceNoteSection');
                var projectNotes = TDAR.inheritance.json.noteInformation.resourceNotes;

                //it's always safe to overwrite an empty section
                var $textareas = $resourceNoteSection.find('textarea');
                if($textareas.length === 1 && $.trim($textareas.first().val()).length === 0) {
                    return true;
                }

                //distill the resourcenote objects to one array and the form section to another array, then compare the two arrays.
                var formVals = [], projectVals = [];
                projectVals = projectVals.concat($.map(projectNotes, function(note){
                    return note.type;
                }));
                projectVals = projectVals.concat($.map(projectNotes, function(note){
                    return note.note;
                }));


                $resourceNoteSection.find('select').each(function(){
                    formVals.push($(this).val());
                });

                $resourceNoteSection.find('textarea').each(function(){
                    formVals.push($.trim($(this).val()));
                });

                //FIXME: ignoreOrder should be false, but I'm pretty sure server doesn't preserve order. turn this on once fixed.
                return $.compareArray(projectVals, formVals, true);


            },
            inheritSectionCallback : function() {
                _inheritNoteInformation('#resourceNoteSection', TDAR.inheritance.json);
            }
        },
        {
            sectionNameSelector: "#relatedCollectionInfoSectionLabel",
            cbSelector : '#cbInheritingCollectionInformation',
            divSelector : '#relatedCollectionsSection',
            mappedData : "collectionInformation", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                var $textareas = $('#relatedCollectionsSection').find("textarea");
                //overwriting empty section is always safe
                if($textareas.length === 2 && $.trim($textareas[0].value).length === 0  && $.trim($textareas[1].value).length === 0)  {
                    return true;
                }

                var formVals = [], projectVals = [];
                $textareas.each(function() {
                    formVals.push($.trim(this.value));
                });

                projectVals = projectVals.concat($.map(TDAR.inheritance.json.collectionInformation.sourceCollections, function(obj) {
                    return obj.text;
                }));
                projectVals = projectVals.concat($.map(TDAR.inheritance.json.collectionInformation.relatedComparativeCollections, function(obj) {
                    return obj.text;
                }));

                //FIXME: array comparison shouldn't ignore order if server side maintains sequence order... does it?
                return $.compareArray(formVals, projectVals, true);
            },
            inheritSectionCallback : function() {
                _inheritCollectionInformation('#relatedCollectionsSection', TDAR.inheritance.json);
            }
        },
        {
            sectionNameSelector: "#identifierInfoSectionLabel",
            cbSelector : '#cbInheritingIdentifierInformation',
            divSelector : '#divIdentifiers',
            mappedData : "resourceAnnotations", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                // flatten json to array of values [key, val, key, val, ...], and
                // compare to field values.
                var vals = [];
                $.each(TDAR.inheritance.json.resourceAnnotations, function(i, annotation) {
                    vals.push(annotation.resourceAnnotationKey.key); // identifier
                                                                        // key
                    vals.push(annotation.value); // identifier value;
                });
                return _inheritingRepeatRowsIsSafe('#divIdentifiers', vals);
            },
            inheritSectionCallback : function() {
                _inheritIdentifierInformation('#divIdentifiers', TDAR.inheritance.json);
            }
        },
        {
            sectionNameSelector: "#spatialInfoSectionLabel",
            cbSelector : '#cbInheritingSpatialInformation',
            divSelector : '#divSpatialInformation',
            mappedData : "collectionInformation", // curently not used (fixme: implement tdar.common.getObjValue)
            isSafeCallback : function() {
                return _inheritingMapIsSafe('#divSpatialInformation', TDAR.inheritance.json.spatialInformation)
                && _inheritingRepeatRowsIsSafe('#geographicKeywordsRepeatable', TDAR.inheritance.json.spatialInformation.geographicKeywords);
            },
            inheritSectionCallback : function() {
                _inheritSpatialInformation("#divSpatialInformation", TDAR.inheritance.json);
            },
            enableSectionCallback: function() {
                _enableSection('#divSpatialInformation');
                _enableMap();
            }
        },

        {
            sectionNameSelector: "#creditInfoSectionLabel",
            cbSelector: "#cbInheritingCreditRoles",
            divSelector: "#creditSection",
            mappedData: "creditProxies",
            isSafeCallback: function() {return true;},
            inheritSectionCallback: function() {
                _inheritCreditInformation('#creditTable', TDAR.inheritance.json.creditProxies);

            }

        }

    ];

    $.each(optionsList, function(idx, options){
        TDAR.inheritance.registerInheritSection(options);
    });

    //We don't want to have an editable map when resource inherits spatialInformation, however, the map won't be available immediately after pageload. So
    //we wait till the map is loaded and ready
    $('#editmapv3').one('mapready', function(e) {
        if ($('#cbInheritingSpatialInformation').prop('checked')) {
            _disableMap();
        }
    });
}

function _updateInheritanceCheckboxes() {
    var projectId = $('#projectId').val();
    if (projectId <= 0) {
        $('.divInheritSection').find('label').addClass('disabled');
        $('.divInheritSection').find(':checkbox').prop('disabled', true);
        $('.divInheritSection').find(':checkbox').prop('checked', false);
        _enableAll();
    } else {
        $('.divInheritSection').find('label').removeClass('disabled');
        $('.divInheritSection').find(':checkbox').prop('disabled', false);
    }
}

function _enableAll() {
    _enableSection('#divInvestigationInformation');
    _enableSection('#divSiteInformation');
    _enableSection('#divMaterialInformation');
    _enableSection('#divCulturalInformation');
    _enableSection('#divSpatialInformation');
    _enableSection('#divTemporalInformation');
    _enableSection('#divOtherInformation');
    _enableSection('#divIdentifiers');
    _enableSection('#relatedCollectionsSection');
    _enableSection('#resourceNoteSection');
    _enableSection("#creditTable");
}

//todo: this duplicates code (see all the calls to bindCheckbox); use  inheritOptionsList instead
function _updateInheritableSections(formId, projectJson) {
    //HACK: temporary fix for TDAR-2268 - our form populate js is overwriting the ID field with data.id
    var jsonid = projectJson.id;
//    console.log(json);
    TDAR.inheritance.id = null;
    delete(TDAR.inheritance.id);

    // indicate in each section which project the section will inherit from.
    var labelText = "Inherit values from parent project";
    var selectedProjectName = "Select a project above to enable inheritance";
    if (jsonid > 0) {
        labelText = 'Inherit values from parent project "' + TDAR.ellipsify(projectJson.title, 60) + '"';
        selectedProjectName = "Inherit metadata from " + projectJson.title;
    }
    
    //update inheritance checkbox labels with new project name (don't clobber checkbox in the process)
    $('.divInheritSection').find('label.checkbox .labeltext').text(labelText);
    
    $('#spanCurrentlySelectedProjectText').text(selectedProjectName);

    //update the checked inheritable sections with the updated project data
    var $form = $("#" + formId);
    $.each($form.data("inheritOptionsList"), function(idx, options) {
        if($(options.cbSelector).prop('checked')) {
            options.inheritSectionCallback();
        }
    });
    
    
}

function _selectAllInheritanceClicked() {
    var $elem = $("#cbSelectAllInheritance");
    try {
        $elem.data('isUpdatingSections', true);

        var checked = $elem.prop('checked');
        var $sectionCheckboxes = $('.divInheritSection input[type=checkbox]');

        // make all of the section checkboxes just like this checkbox.
        if (checked) {
            // check all of the unchecked sections
            
//            $sectionCheckboxes.not(':checked').each(function() {
//                $(this).click();
//            });
            _attemptMultipleInheritance($sectionCheckboxes.not(':checked'));
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

//display modal dialog w/ list of affected sections.
//TODO: Alert box might be better because user could inspect sections prior to decision, but managing state would be way harder 
//(e.g. user changes additional values before they finally click 'okay' or 'cancel').
function _displayOverwritePrompt(optionsList, okaySelected, cancelSelected) {
    var $modalDiv = $('#inheritOverwriteAlert');
    
    //populate list of conflicting sections
    var $ul = $("<ul></ul>");
    $.each(optionsList, function(idx, options){
        $ul.append("<li>" + $(options.sectionNameSelector).text() + "</li>");
    });
    $modalDiv.find('.list-container').empty().append($ul);

    //modal is animated,  so we shouldn't do this dom-intensive stuff until animation is complete

    //by default, treat 'hidden' event as a 'cancel'
    $modalDiv.one("hidden", cancelSelected);
    
    //if 'okay' clicked,  swap out the 'cancel'  and perform multi-inheritance once the modal is completely gone
    $('#btnInheritOverwriteOkay').one("click", function(){
        $modalDiv.unbind("hidden", cancelSelected);
        $modalDiv.one("hidden", okaySelected);
        $modalDiv.modal('hide');
    });
    
    $modalDiv.modal();
}


//get all unchecked boxes
//get options for each
//if any unsafe,   fire prompt
//for prompt,  wire yes and no buttons
function _attemptMultipleInheritance($checkboxes) {
    var unsafeSections = [];
    var optionsList = $.map($checkboxes, function(checkbox, idx){
        var options = $(checkbox).data("inheritOptions");
        if(!options.isSafeCallback()) {
            unsafeSections.push(options);
        }
        return options;
    });
    
    //action we'll take if all sections safe OR user expressly permits overwrite
    var _inheritMultiple = function() {
        $checkboxes.prop("checked", true);
        $.each(optionsList, function(idx, options){
            options.inheritSectionCallback();
        });
    };
    
    //if any sections unsafe (i.e. inheritance would overwrite previous values),  throw a prompt to the user
    if(unsafeSections.length) {
        _displayOverwritePrompt(unsafeSections, _inheritMultiple, function(){
                $('#cbSelectAllInheritance').prop("checked", false);
        });
    //no conflicts; it's okay to go down the line and inherit the given sections
    } else {
        _inheritMultiple();
    }
}
 

// determine whether to enable or disable the 'inherit all' checkbox based upon
// the currently selected parent project.
function _enableOrDisableInheritAllSection() {
    var $cbSelectAllInheritance = $('#cbSelectAllInheritance');
    var projectId = $('#projectId').val();
    if (projectId > 0) {
        _enableSection('#divInheritFromProject');

    } else {
        $cbSelectAllInheritance.removeAttr('checked');
        _disableSection('#divInheritFromProject');
    }
}

//Enforce the correct state of 'inherit all' checkbox. It should only be checked iff. every inherit-section is checked.
function _updateSelectAllCheckboxState() {
    var $cbSelectAllInheritance = $('#cbSelectAllInheritance');
    if (!$cbSelectAllInheritance.data('isUpdatingSections')) {
        var $uncheckedBoxes = $('.divInheritSection input[type=checkbox]').not(":checked");
        $cbSelectAllInheritance.prop('checked', $uncheckedBoxes.length === 0);
    }
}

//FIXME: kill this function.
function _populateLatLongTextFields() {
 $("#d_minx").val(Geo.toLon($("#minx").val()));
 $("#d_miny").val(Geo.toLat($("#miny").val()));
 $("#d_maxx").val(Geo.toLon($("#maxx").val()));
 $("#d_maxy").val(Geo.toLat($("#maxy").val()));
}

//FIXME: kill this function.
function _disableMap() {
    var $mapdiv = $('#editmapv3');
    $mapdiv.addClass('opaque');
    if($mapdiv.data("resourceRect")) {
        $mapdiv.data("resourceRect").setEditable(false);
    }
}

//FIXME: kill this function.
function _enableMap() {
    var $mapdiv = $('#editmapv3');
    $mapdiv.removeClass('opaque');
    if($mapdiv.data("resourceRect")) {
        $mapdiv.data("resourceRect").setEditable(true);
    }
}

    /**
     * "Reset" a repeat-row table so that it contains N blank rows.  Any non-default input field values are destroyed.
     * If the specified repeat-row table contains more that N rows, this function destroys the extraneous rows.
     *
     * @param repeatableSelector selector for the repeatrow table.
     * @param newSize the number of rows the table will contain.
     */
    var resetRepeatable = function(repeatableSelector, newSize) {
        $(repeatableSelector).find(".repeat-row:not(:first)").remove();
        var $firstRow = $('.repeat-row', repeatableSelector);
        _resetIndexedAttributes($firstRow); 
        for(var i = 0; i < newSize - 1; i++) {
            TDAR.repeatrow.cloneSection($('.repeat-row:last', repeatableSelector)[0]);
        }
    };

    /**
     * Convenience function, equivalent to resetRepeatable(repeatableSelector, keywords.length)
     *
     * @param keywords array of strings. length of the array dictates the rowcount after the reset.
     * @param repeatableSelector selector for the repeatrow table
     */
    var resetKeywords = function(keywords, repeatableSelector) {
        var $repeatable = $(repeatableSelector);
        resetRepeatable(repeatableSelector, keywords.length);
    };

    /**
     * Enable inputs and remove disabled styling for labels  inside the specified container
     * @param idSelector element that contains the inputs/labels to enable.
     * @private
     */
    var _enableSection = function (idSelector) {
        $(':input', idSelector).prop('disabled', false);
        $('label', idSelector).removeClass('disabled');
        $('.addAnother, .minus', idSelector).show();
    };

    /**
     * Display a confirm prompt and call a callback corresponding to the user's choice
     * @param msg message to display in the prompt.
     * @param okaySelected  handler to call if user clicks "okay"
     * @param cancelSelected handler to call if user clicks "cancel" or dismisses prompt
     * @private
     */
    var _confirm = function(msg, okaySelected, cancelSelected) {
        var confirmed = confirm(msg);
        if(confirmed) {
            okaySelected();
        } else {
            cancelSelected();
        }
    };

    /**
     * Register a section of a form that support inheritance.  When registered,  the input fields in the section
     * appear disabled and cannot be modified directly by the user.  Instead, the values of the input fields
     * are dictated by the values of the corresponding fields in the currently-selected "parent project".  If the user
     * specifies a different parent project,  the registered section updates its field values accordingly.
     *
     * @param options  settings object (all properties required)
     *          cbSelector: selector for the checkbox element that enables/disables inheritance for the section,
     *          sectionNameSelector: string used for the label beside the section checkbox
     *          divSelector: selector for the DIV that contains all of the elements in the inheritence section
     *
     */
    var registerInheritSection = function(options) {
        var $checkbox = $(options.cbSelector);
        if($checkbox.length === 0 ) return;
        
        var $form = $checkbox.closest("form");
        var formId = $form.attr("id");
        var _options = {
                sectionNameSelector: "",
                isSafeCallback: function(){return true;},
                //fixme: getObjValue(json, options.mappedData) instead?
                inheritSectionCallback: function() {_inheritInformation(formId, TDAR.inheritance.json[_options.mappedData]);}, 
                enableSectionCallback: _enableSection //fixme: move to namespace
        };
        $.extend(_options, options);
        $form.data("inheritOptionsList").push(_options);
        $checkbox.data("inheritOptions", _options);
        
        //update contents/state of section when checkbox for that section is toggled
        $(_options.cbSelector).change(function(e) {
            var cb = this;
            var $cb = $(cb);
            if ($cb.prop("checked")) {
                // determine if inheriting would overrwrite existing values
                var isSafe = _options.isSafeCallback();
                if(isSafe) {
                    _options.inheritSectionCallback();
                } else {
                    //not safe!  ask the user for confirmation
                    _confirm("Inheriting from '" + TDAR.common.htmlEncode(TDAR.inheritance.project.title) + "' will overwrite existing values. Continue?",
                        function(){
                            _options.inheritSectionCallback();
                        },
                        function(){
                            $cb.prop("checked", false);
                            _options.enableSectionCallback(_options.divSelector);
                            _updateSelectAllCheckboxState();
                        }
                    );
                };
            } else {
                //user unchecked inheritance - enable the controls
                    _options.enableSectionCallback(_options.divSelector);
                    _updateSelectAllCheckboxState();
                    $(_options.divSelector).find('input[type=hidden].dont-inherit').val("");
            }
        });
    };

    return{
        resetRepeatable: resetRepeatable,
        resetKeywords: resetKeywords,
        registerInheritSection: registerInheritSection,
        applyInheritance: applyInheritance
    };
})();
