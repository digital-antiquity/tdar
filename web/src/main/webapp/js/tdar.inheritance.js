TDAR.inheritance = (function () {
    "use strict";

    /*
     * DOWNWARD INHERITANCE SUPPORT
     */
    var indexExclusions = [ 'investigationTypeIds', 'approvedSiteTypeKeywordIds', 'approvedMaterialKeywordIds', 'approvedCultureKeywordIds' ];
    var TYPE_PERSON = "PERSON";
    var TYPE_INSTITUTION = "INSTITUTION";
    var select2 = false;
    /**
     * convenience function for $.populate()
     *
     * @param elemSelector form element sent to $.populate() (NOTE: must be form element due to bug in $.populate plugin, no matter what their documentation may say)
     * @param formdata  pojo to send to $.populate()
     * @private
     */
    function _populateSection(elemSelector, formdata) {

        $(elemSelector).populate(formdata, {
            resetForm: false,
            phpNaming: false,
            phpIndices: true,
            strutsNaming: true,
            noIndicesFor: indexExclusions
        });
        $(elemSelector).trigger("heightchange");
    }

    /**
     * convert a serialized project into the json format needed by the form.
     * @param rawJson
     * @returns *
     * @private
     */
    function _convertToFormJson(rawJson) {
        // create a skeleton of what we need
        var obj = {

            title: rawJson.title,
            id: rawJson.id,
            resourceType: rawJson.resourceType,
            investigationInformation: {
                investigationTypeIds: $.map(rawJson.activeInvestigationTypes, function (v) {
                    return v.id;
                }) || []
            },
            siteInformation: {
                siteNameKeywords: $.map(rawJson.activeSiteNameKeywords, function (v) {
                    return v.label;
                }),
                approvedSiteTypeKeywordIds: $.map(rawJson.approvedSiteTypeKeywords, function (v) {
                    return v.id;
                }) || [],
                uncontrolledSiteTypeKeywords: $.map(rawJson.uncontrolledSiteTypeKeywords, function (v) {
                    return v.label;
                })
            },
            materialInformation: {
                approvedMaterialKeywordIds: $.map(rawJson.approvedMaterialKeywords, function (v) {
                    return v.id;
                }) || [],
                uncontrolledMaterialKeywords: $.map(rawJson.uncontrolledMaterialKeywords, function (v) {
                    return v.label;
                })
            },
            culturalInformation: {
                approvedCultureKeywordIds: $.map(rawJson.approvedCultureKeywords, function (v) {
                    return v.id;
                }) || [],
                uncontrolledCultureKeywords: $.map(rawJson.uncontrolledCultureKeywords, function (v) {
                    return v.label;
                })
            },
            spatialInformation: {
                geographicKeywords: $.map(rawJson.activeGeographicKeywords, function (v) {
                    return v.label;
                })
            },
            temporalInformation: {
                temporalKeywords: $.map(rawJson.activeTemporalKeywords, function (v) {
                    return v.label;
                }),
                coverageDates: rawJson.activeCoverageDates
            },
            resourceAnnotations: rawJson.activeResourceAnnotations,
            noteInformation: {
                resourceNotes: rawJson.activeResourceNotes
            },
            collectionInformation: {
                sourceCollections: rawJson.activeSourceCollections,
                relatedComparativeCollections: rawJson.activeRelatedComparativeCollections

            },
            otherInformation: {
                otherKeywords: $.map(rawJson.activeOtherKeywords, function (v) {
                    return v.label;
                })
            },
            creditProxies: $.map(rawJson.activeIndividualAndInstitutionalCredit, _convertCreator)
        };

        if (rawJson.activeLatitudeLongitudeBoxes != undefined && rawJson.activeLatitudeLongitudeBoxes.length > 0) {
            var llb = rawJson.activeLatitudeLongitudeBoxes[0];
            obj.spatialInformation['minx'] = llb.obfuscatedWest;
            obj.spatialInformation['maxx'] = llb.obfuscatedEast;
            obj.spatialInformation['miny'] = llb.obfuscatedSouth;
            obj.spatialInformation['maxy'] = llb.obfuscatedNorth;
        } else{
            obj.spatialInformation['minx'] = '';
            obj.spatialInformation['maxx'] = '';
            obj.spatialInformation['miny'] = '';
            obj.spatialInformation['maxy'] = '';
        }

        return obj;
    }

    /**
     * convert creator from untranslated json to object that can then be passed to from populate plugin
     * @param raw
     * @returns {{id: *, role: *, type: string, person: {}, institution: {}}}
     * @private
     */
    function _convertCreator(raw) {
        var bPerson = raw.creator.hasOwnProperty("lastName");
        var obj = {
            id: raw.creator.id,
            role: raw.role,
            type: bPerson ? TYPE_PERSON : TYPE_INSTITUTION,
            person: {},
            institution: {}
        };

        if (bPerson) {
            obj.person = {
                id: raw.creator.id,
                lastName: raw.creator.lastName,
                firstName: raw.creator.firstName,
                email: raw.creator.email,
                institution: {name: "", id: ""}
            }
            if (raw.creator.institution) {
                obj.person.institution.name = raw.creator.institution.name;
                obj.person.institution.id = raw.creator.institution.id;
            }
        } else {
            obj.institution = {
                id: raw.creator.id,
                name: raw.creator.name
            }
        }
        ;
        return obj;
    }

    /**
     * disable a section: disable inputs, but also make section look disabled by graying labels and removing edit controls
     * @param idSelector
     * @private
     */
    function _disableSection($section) {

        $(':input', $section).not(".alwaysEnabled").prop('disabled', true);
        $('label', $section).not(".alwaysEnabled").addClass('disabled');
        $('.addAnother, .minus', $section).hide();

        //if sibling is an add-another button,  disable that too.
        $section.next(".add-another-control").find("button").prop('disabled', true);
    }

    function _enableSection($section) {
        $(':input', $section).prop('disabled', false); // here's call that spawns ghost elements (TDAR-4358)
        $('label', $section).removeClass('disabled');
        $('.addAnother, .minus', $section).show();

        //if sibling is an add-another button,  enable that too.
        $section.next(".add-another-control").find("button").prop('disabled', false);
    }

    /**
     * Rename id/name attribute in element and children if they follow struts 'indexed' such that the 'index' portion  of the name is set to zero (e.g.
     * if an ID attribute has a value of 'my_input_field[12]' the function will rename it to 'my_input_field[0]'
     * @param elem
     * @private
     */
    function _resetIndexedAttributes(elem) {
        var rex = /^(.+[_|\[])([0-9]+)([_|\]].*$)/; // string containing _num_ or [num]
        var replacement = "$10$3"; // replace foo_bar[5] with foo_bar[0]
        $(elem).add("tr, :input", elem).each(function (i, v) {
            var id = $(v).attr("id");
            var name = $(v).attr("name");
            if (id) {
                var newid = id.replace(rex, replacement);
                $(v).attr("id", newid);
            }
            if (name) {
                var newname = name.replace(rex, replacement);
                $(v).attr("name", newname);
            }
        });
    }

    /**
     * Clears (but does not reset) the selected elements.
     * @param selector
     * @private
     */
    function _clearFormSection($selector) {
        // Use a whitelist of fields to minimize unintended side effects.
        $selector.find('input:text, input:password, input:file, textarea').val('');
        // De-select any checkboxes, radios and drop-down menus
        $(':input', $selector).prop('checked', false).prop('selected', false);
    }

    /**
     * return true if the repeatrows contained in the selector match the list of strings
     * @param rootElementSelector
     * @param values
     * @returns {boolean}
     * @private
     */
    function _inheritingRepeatRowsIsSafe(rootElementSelector, values) {
        var repeatRowValues = $.map($('input[type=text]', rootElementSelector), function (v, i) {
            if ($(v).val()) {
                return $(v).val();
            }
        });
        
        // if select2 is enabled...
        if (select2) {
            var $select2 = $(rootElementSelector);
            if ($select2.length > 0) {
            	repeatRowValues = $select2.val();
            }
        }
        if (repeatRowValues == undefined) {
            repeatRowValues = new Array();
        }
        return repeatRowValues.length === 0 || $.compareArray(repeatRowValues, values);
    }

    /**
     * return true if this section can 'safely' inherit specified values. 'safe' means that the target values are empty or the
     * same as the incoming values.
     * @param rootElementSelector
     * @param values
     * @returns {boolean}
     * @private
     */
    function _inheritingCheckboxesIsSafe(rootElementSelector, values) {
        var checkedValues = $.map($(':checkbox:checked', rootElementSelector), function (v, i) {
            return $(v).val();
        });
        var isSafe = checkedValues.length === 0 || $.compareArray(checkedValues, values);
        return isSafe;
    }

    function _inheritingMapIsSafe(spatialInformation) {
        // or make the fields retrievable by name instead of id
        var si = spatialInformation;
        // compare parent coords to this form's current coords. seems like overkill
        // but isn't.
        var jsonVals = [ si.minx, si.miny, si.maxx, si.maxy ]; // strip out nulls
        var formVals = $.map([
            $('#minx').val(), $('#miny').val(), $('#maxx').val(), $('#maxy').val()
        ], function (v) {
            if (v !== "") {
                return parseFloat(v);
            }
        });

        return  !formVals.length || $.compareArray(jsonVals, formVals, false);
    }

    /**
     * return whether it's "safe" to populate the temporal information section with the supplied temporalInformation we define "safe" to mean that section is
     * either currently blank or that the supplied temporalInformation is the same as what is already on the form.

     * @param rootElementSelector
     * @param temporalInformation
     * @returns {boolean}
     * @private
     */
    function _inheritingDatesIsSafe(rootElementSelector, temporalInformation) {
        // are all the fields in this section blank?
        var $coverageTextFields = $('input:text', '#coverageDateRepeatable');
        var joinedFieldValues = $coverageTextFields.map(function () {
            return $(this).val();
        }).toArray().join("");

        // okay to populate if if the form section is blank
        if (joinedFieldValues === "") {
            return true;
        }

        // not okay to populate if the incoming list is a different size as the
        // current list
        var $tableRows = $('.repeat-row', '#coverageDateRepeatable');
        if (temporalInformation.coverageDates.length !== $tableRows.length) {
            return false;
        }

        // at this point it's we need to compare the contents of the form vs.
        // incoming coverage dates
        var concatTemporalInformation = $.map(temporalInformation.coverageDates, function (val, i) {
            return "" + val.startDate + val.endDate + val.description;
        }).join("");
        var concatRowFields = $.map($tableRows, function (rowElem, i) {
            var concatRow = $('.coverageStartYear', rowElem).val();
            concatRow += $('.coverageEndYear', rowElem).val();
            concatRow += $('.coverageDescription', rowElem).val();
            return concatRow;
        }).join("");

        return concatTemporalInformation === concatRowFields;

    }

    function _inheritInformation(formId, json, sectionId, tableId) {
        //console.debug("_inheritInformation(formId:%s, json:%s, sectionId:%s, tableId:%s)", formId, json, sectionId, tableId);
        _clearFormSection($(sectionId));
        if (tableId !== undefined) {
            TDAR.inheritance.resetRepeatable("#" + 'uncontrolled' + tableId + 'Repeatable', json['uncontrolled' + tableId].length);
            TDAR.inheritance.resetRepeatable("#" + 'approved' + tableId + 'Repeatable', json['approved' + tableId].length);
            var simpleId = tableId;
            simpleId = simpleId[0].toLowerCase() + simpleId.substr(1);
            TDAR.inheritance.resetRepeatable("#" + simpleId + 'Repeatable', json[simpleId].length);
        }
        _populateSection(formId, json);
        _disableSection($(sectionId));
    }

    function _inheritOtherInformation(formId, json, sectionId, tableId) {
        //console.debug("_inheritInformation(formId:%s, json:%s, sectionId:%s, tableId:%s)", formId, json, sectionId, tableId);
        var $section = $(sectionId);

        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable("#otherKeywordsRepeatable", json.otherInformation.otherKeywords.length);
        _populateSection(formId, json.otherInformation);
        _populateSelect2Keywords($section, json.otherInformation.otherKeywords);
        _disableSection($(sectionId));
    }

//

    function _inheritSiteInformation(formId, json) {
    	var $section = $('#divSiteInformation');
        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable('#siteNameKeywordsRepeatable', json.siteInformation['siteNameKeywords'].length);
        TDAR.inheritance.resetRepeatable('#uncontrolledSiteTypeKeywordsRepeatable', json.siteInformation['uncontrolledSiteTypeKeywords'].length);
        _populateSection(formId, json.siteInformation);
        _populateSelect2Keywords($('#siteNameKeywordsRepeatable').parent(), json.siteInformation.siteNameKeywords);
        _populateSelect2Keywords($('#uncontrolledSiteTypeKeywordsRepeatable').parent(), json.siteInformation.uncontrolledSiteTypeKeywords);
        _disableSection($section);
    }


    function _inheritCulturalInformation(formId, json) {
    	var $section = $('#divCulturalInformation');
        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable('#uncontrolledCultureKeywordsRepeatable', json.culturalInformation['uncontrolledCultureKeywords'].length);
        _populateSection(formId, json.culturalInformation);
        _populateSelect2Keywords($section, json.culturalInformation.uncontrolledCultureKeywords);
        _disableSection($section);
    }

    function _inheritMaterialInformation(formId, json) {
    	var $section = $('#allMaterialInformation');
        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable('#uncontrolledMaterialKeywordsRepeatable', json.materialInformation['uncontrolledMaterialKeywords'].length);
        _populateSection(formId, json.materialInformation);
        _populateSelect2Keywords($section, json.materialInformation.uncontrolledMaterialKeywords);
        _disableSection($section);
    }

    function _inheritIdentifierInformation(formId, json) {
    	var $section = $('#divIdentifiers');
        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable('#resourceAnnotationsTable', json.resourceAnnotations.length);
        _populateSection(formId, json);
        _disableSection($section);
    }

    function _inheritCollectionInformation(formId, json) {
    	var $section = $('#relatedCollectionsSection');
        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable('#divSourceCollectionControl', json.collectionInformation.sourceCollections.length);
        TDAR.inheritance.resetRepeatable('#divRelatedComparativeCitationControl', json.collectionInformation.sourceCollections.length);
        _populateSection(formId, json.collectionInformation);
        _disableSection($section);
    }

    function _inheritNoteInformation(formId, json) {
    	var $section = $('#resourceNoteSection');
        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable('#resourceNoteSection', json.noteInformation['resourceNotes'].length);
        _populateSection(formId, json.noteInformation);
        _disableSection($section);
    }


    function _populateSelect2Keywords($section, array) {
        if (select2) {
            var $gk = $(".keyword-autocomplete", $section);
            $gk.empty();
            $.each(array, function (i, item) {
                $gk.append($('<option>', { 
                    value: item,
                    text : item
                }));
            });
            $gk.val(array).trigger("change");
        }
    }
    
    function _inheritSpatialInformation(formId, json) {
        console.log("inherit spatial information(%s, %s)", formId, json);
        var $section = $('#divSpatialInformation');
        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable('#geographicKeywordsRepeatable', json.spatialInformation['geographicKeywords'].length);
        _populateSection(formId, json.spatialInformation);
        _populateSelect2Keywords($section, json.spatialInformation['geographicKeywords']);
        _disableSection($section);

        // clear the existing redbox and draw new one;
        _populateLatLongTextFields();

        var si = json.spatialInformation;
        if (si.miny != null && si.minx != null && si.maxy != null && si.maxx != null) {
            $(".locateCoordsButton",$section).click();
        }
        _disableMap();

    }

    function _inheritTemporalInformation(formId, json) {
        var $section = $('#divTemporalInformation');
        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable('#temporalKeywordsRepeatable', json.temporalInformation.temporalKeywords.length);
        TDAR.inheritance.resetRepeatable('#coverageDateRepeatable', json.temporalInformation.coverageDates.length);
        _populateSection(formId, json.temporalInformation);
        _populateSelect2Keywords($section, json.temporalInformation.temporalKeywords);
        _disableSection($section);
    }

    function _inheritCreditInformation(divSelector, creators) {
    	var $section = $(divSelector);
        _clearFormSection($section);
        TDAR.inheritance.resetRepeatable(divSelector, creators.length);
        if (creators.length > 0) {
            _populateSection(divSelector, {creditProxies: creators});
            //now set the correct toggle state for eachrow
            var $proxyRows = $(divSelector).find(".repeat-row");
            $proxyRows.each(function (i, rowElem) {
                var $rowElem = $(rowElem);
                var $creatorPerson = $rowElem.find(".creatorPerson");
                var $creatorInstitution = $rowElem.find(".creatorInstitution");
                var $personButton =  $rowElem.find(".personButton");
                var $institutionButton = $rowElem.find(".institutionButton");
                $rowElem.find(".resourceCreatorId").val("");
                if (creators[i].type === TYPE_PERSON) {
                    $creatorPerson.removeClass("hidden");
                    $creatorInstitution.addClass("hidden");
                    $personButton.addClass("active");
                    $institutionButton.removeClass("active");
                } else {
                    $creatorPerson.addClass("hidden");
                    $creatorInstitution.removeClass("hidden");
                    $personButton.removeClass("active");
                    $institutionButton.addClass("active");

                }
            });

        }

        _disableSection($section);
    }

    function applyInheritance(formSelector) {
        var $form = $(formSelector);
        //collection of 'options' objects for each inheritance section. options contain info about
        //the a section (checkbox selector, div selector,  callbacks for isSafe, inheritSection, enableSection);
        $form.data("inheritOptionsList", []);

        //hack:  formId is no longer global, and updateInheritableSections() needs it...
        var formId = $(formSelector).attr("id");
        if(TDAR.inheritance.project == undefined) {
            TDAR.inheritance.project = _getBlankProject();
        };
        TDAR.inheritance.json = _convertToFormJson(TDAR.inheritance.project);

        console.log("applying inheritance to:" + formSelector);
        // if we are editing, set up the initial form values
        var $projectId = $('#projectId',formSelector);
        var pid = $projectId.val();
        if (pid != undefined && pid > -1) {
            TDAR.inheritance.json = _convertToFormJson(TDAR.inheritance.project);
            console.log(TDAR.inheritance.json);
            _updateInheritableSections(formId, TDAR.inheritance.json);
        }

        // update the inherited form values when project selection changes.
        $projectId.change(function (e) {
            var sel = this;
            var $sel = $(this);
            if ($sel.val() !== '' && $sel.val() > 0) {
                $.ajax({
                    url: TDAR.uri() + "project/json/" + $sel.val() ,
                    dataType: "jsonp",
                    success: _projectChangedCallback,
                    error: function (msg) {
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

        $cbSelectAllInheritance.click(_selectAllInheritanceClicked);

        _projectChangedCallback(TDAR.inheritance.project);
    }

//return skeleton project
    function _getBlankProject() {
    var skeleton = {
            "approvedCultureKeywords": [],
            "approvedSiteTypeKeywords": [],
            "activeCultureKeywords": [],
            "dateCreated": {},
            "description": null,
            "firstLatitudeLongitudeBox": null,
            "activeGeographicKeywords": [],
            "id": null,
            "activeInvestigationTypes": [],
            "approvedMaterialKeywords": [],
            "activeOtherKeywords": [],
            "resourceType": null,
            "activeSiteNameKeywords": [],
            "activeSiteTypeKeywords": [],
            "submitter": null,
            "activeTemporalKeywords": [],
            "activeCoverageDates": [],
            "title": null,
            "activeResourceNotes": [],
            "activeSourceCollections": [],
            "activeRelatedComparativeCollections": [],
            "activeResourceAnnotations": [],
            "uncontrolledCultureKeywords": [],
            "uncontrolledMaterialKeywords": [],
            "uncontrolledSiteTypeKeywords": [],
            "activeIndividualAndInstitutionalCredit": []
        };
        return skeleton;
    }

//update the project json variable and update the inherited sections
    function _projectChangedCallback(data) {
        TDAR.inheritance.project = data;
        // if user picked blank option, then clear the sections
        if (!TDAR.inheritance.project.id || TDAR.inheritance.project.id == -1) {
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
                cbSelector: '#cbInheritingSiteInformation',
                divSelector: '#siteSection',
                mappedData: "siteInformation", // curently not used 
                isSafeCallback: function () {
                    var allKeywords = TDAR.inheritance.json.siteInformation.siteNameKeywords.concat(TDAR.inheritance.json.siteInformation.uncontrolledSiteTypeKeywords);
                    return _inheritingCheckboxesIsSafe('#divSiteInformation', TDAR.inheritance.json.siteInformation.approvedSiteTypeKeywordIds)
                        && _inheritingRepeatRowsIsSafe('#siteNameKeywordsRepeatable', TDAR.inheritance.json.siteInformation.siteNameKeywords)
                        && _inheritingRepeatRowsIsSafe('#uncontrolledSiteTypeKeywordsRepeatable', TDAR.inheritance.json.siteInformation.uncontrolledSiteTypeKeywords)
                        ;

                },
                inheritSectionCallback: function () {
                    _inheritSiteInformation("#siteSection", TDAR.inheritance.json);
                }
            },
            {
                sectionNameSelector: "#temporalInfoSectionLabel",
                cbSelector: '#cbInheritingTemporalInformation',
                divSelector: '#temporalSection',
                mappedData: "temporalInformation", // curently not used 
                isSafeCallback: function () {
                    return _inheritingRepeatRowsIsSafe('#temporalKeywordsRepeatable', TDAR.inheritance.json.temporalInformation.temporalKeywords) && _inheritingDatesIsSafe('#divTemporalInformation', TDAR.inheritance.json.temporalInformation);
                },
                inheritSectionCallback: function () {
                    _inheritTemporalInformation("#temporalSection", TDAR.inheritance.json);
                },
                enableSectionCallback: function () {
                	var $section = $('#temporalSection');
                    _enableSection($section);
                    $section.find(".coverageTypeSelect").each(function (i, elem) {
                    	//FIXME: required?
                        TDAR.validate.prepareDateFields(elem);
                    });
                }

            },
            {
                sectionNameSelector: "#materialInfoSectionLabel",
                cbSelector: '#cbInheritingMaterialInformation',
                divSelector: '#allMaterialInformation',
                mappedData: "materialInformation", // curently not used 
                isSafeCallback: function () {
                    return _inheritingCheckboxesIsSafe('#allMaterialInformation', TDAR.inheritance.json.materialInformation.approvedMaterialKeywordIds) &&
                            _inheritingRepeatRowsIsSafe('#uncontrolledMaterialKeywordsRepeatable', TDAR.inheritance.json.materialInformation.uncontrolledMaterialKeywords);
                },
                inheritSectionCallback: function () {
                    _inheritMaterialInformation('#allMaterialInformation', TDAR.inheritance.json);
                }
            },
            {
                sectionNameSelector: "#culturalInfoSectionLabel",
                cbSelector: '#cbInheritingCulturalInformation',
                divSelector: '#divCulturalInformation',
                mappedData: "culturalInformation", // curently not used 
                isSafeCallback: function () {
                    return _inheritingCheckboxesIsSafe('#divCulturalInformation', TDAR.inheritance.json.culturalInformation.approvedCultureKeywordIds) && 
                    _inheritingRepeatRowsIsSafe('#uncontrolledCultureKeywordsRepeatable', TDAR.inheritance.json.culturalInformation.uncontrolledCultureKeywords);
                },
                inheritSectionCallback: function () {
                    _inheritCulturalInformation('#divCulturalInformation', TDAR.inheritance.json);
                }
            },
            {
                sectionNameSelector: "#generalInfoSectionLabel",
                cbSelector: '#cbInheritingOtherInformation',
                divSelector: '#divOtherInformation',
                mappedData: "otherInformation", // curently not used 
                isSafeCallback: function () {
                    return _inheritingRepeatRowsIsSafe('#otherKeywordsRepeatable', TDAR.inheritance.json.otherInformation.otherKeywords);
                },
                inheritSectionCallback: function () {
                    _inheritOtherInformation('#divOtherInformation', TDAR.inheritance.json, "#divOtherInformation", "otherKeywords");
                }
            },
            {
                sectionNameSelector: "#investigationInfoSectionLabel",
                cbSelector: '#cbInheritingInvestigationInformation',
                divSelector: '#divInvestigationInformation',
                mappedData: "investigationInformation", // curently not used 
                isSafeCallback: function () {
                    return _inheritingCheckboxesIsSafe('#divInvestigationInformation', TDAR.inheritance.json.investigationInformation.investigationTypeIds);
                },
                inheritSectionCallback: function () {
                    _inheritInformation('#divInvestigationInformation', TDAR.inheritance.json.investigationInformation, '#divInvestigationInformation');
                }
            },
            {
                sectionNameSelector: "#notesInfoSectionLabel",
                cbSelector: '#cbInheritingNoteInformation',
                divSelector: '#resourceNoteSection',
                mappedData: "noteInformation", // curently not used 
                isSafeCallback: function () {
                    var $resourceNoteSection = $('#resourceNoteSection');
                    var projectNotes = TDAR.inheritance.json.noteInformation.resourceNotes;

                    //it's always safe to overwrite an empty section
                    var $textareas = $resourceNoteSection.find('textarea');
                    if ($textareas.length === 1 && $.trim($textareas.first().val()).length === 0) {
                        return true;
                    }

                    //distill the resourcenote objects to one array and the form section to another array, then compare the two arrays.
                    var formVals = [], projectVals = [];
                    projectVals = projectVals.concat($.map(projectNotes, function (note) {
                        return note.type;
                    }));
                    projectVals = projectVals.concat($.map(projectNotes, function (note) {
                        return note.note;
                    }));

                    $resourceNoteSection.find('select').each(function () {
                        formVals.push($(this).val());
                    });

                    $resourceNoteSection.find('textarea').each(function () {
                        formVals.push($.trim($(this).val()));
                    });

                    return $.compareArray(projectVals, formVals, true);

                },
                inheritSectionCallback: function () {
                    _inheritNoteInformation('#resourceNoteSection', TDAR.inheritance.json);
                }
            },
            {
                sectionNameSelector: "#relatedCollectionInfoSectionLabel",
                cbSelector: '#cbInheritingCollectionInformation',
                divSelector: '#relatedCollectionsSection',
                mappedData: "collectionInformation", // curently not used 
                isSafeCallback: function () {
                    var $textareas = $('#relatedCollectionsSection').find("textarea");
                    //overwriting empty section is always safe
                    if ($textareas.length === 2 && $.trim($textareas[0].value).length === 0 && $.trim($textareas[1].value).length === 0) {
                        return true;
                    }

                    var formVals = [], projectVals = [];
                    $textareas.each(function () {
                        formVals.push($.trim(this.value));
                    });

                    projectVals = projectVals.concat($.map(TDAR.inheritance.json.collectionInformation.sourceCollections, function (obj) {
                        return obj.text;
                    }));
                    projectVals = projectVals.concat($.map(TDAR.inheritance.json.collectionInformation.relatedComparativeCollections, function (obj) {
                        return obj.text;
                    }));

                    return $.compareArray(formVals, projectVals, true);
                },
                inheritSectionCallback: function () {
                    _inheritCollectionInformation('#relatedCollectionsSection', TDAR.inheritance.json);
                }
            },
            {
                sectionNameSelector: "#identifierInfoSectionLabel",
                cbSelector: '#cbInheritingIdentifierInformation',
                divSelector: '#divIdentifiers',
                mappedData: "resourceAnnotations", // curently not used 
                isSafeCallback: function () {
                    // flatten json to array of values [key, val, key, val, ...], and
                    // compare to field values.
                    var vals = [];
                    $.each(TDAR.inheritance.json.resourceAnnotations, function (i, annotation) {
                        vals.push(annotation.resourceAnnotationKey.key); // identifier
                        // key
                        vals.push(annotation.value); // identifier value;
                    });
                    return _inheritingRepeatRowsIsSafe('#divIdentifiers', vals);
                },
                inheritSectionCallback: function () {
                    _inheritIdentifierInformation('#divIdentifiers', TDAR.inheritance.json);
                }
            },
            {
                sectionNameSelector: "#spatialInfoSectionLabel",
                cbSelector: '#cbInheritingSpatialInformation',
                divSelector: '#divSpatialInformation',
                mappedData: "collectionInformation", // curently not used 
                isSafeCallback: function () {
                    return _inheritingMapIsSafe(TDAR.inheritance.json.spatialInformation) && 
                    _inheritingRepeatRowsIsSafe('#geographicKeywordsRepeatable', TDAR.inheritance.json.spatialInformation.geographicKeywords);
                },
                inheritSectionCallback: function () {
                    _inheritSpatialInformation("#divSpatialInformation", TDAR.inheritance.json);
                },
                enableSectionCallback: function () {
                    _enableSection($('#divSpatialInformation'));
                    _enableMap();
                }
            },

            {
                sectionNameSelector: "#creditInfoSectionLabel",
                cbSelector: "#cbInheritingCreditRoles",
                divSelector: "#creditSection",
                mappedData: "creditProxies",
                isSafeCallback: _inheritingCreditInfoIsSafe,
                inheritSectionCallback: function () {
                    _inheritCreditInformation('#creditTable', TDAR.inheritance.json.creditProxies);

                }
            }

        ];

        $.each(optionsList, function (idx, options) {
            console.log("register inheritance:"+ options.divSelector);
            TDAR.inheritance.registerInheritSection(options);
//            $(options.cbSelector).change();
        });

        /* We want to disable the map when a user inherits spatialInformation, however, the map isn't available immediately after pageload. So we wait for the
        browser to load the map gmap api and initialize the map. */
        // $('#editmapv3').one('mapready', function (e) {
        //     if ($('#cbInheritingSpatialInformation').prop('checked')) {
        //         _disableMap();
        //     }
        // });
    }

    function _inheritingCreditInfoIsSafe() {
        var $creditRows = $("#creditTable > .repeat-row");
        var array1 = $.map(TDAR.inheritance.json.creditProxies,function(obj){
            return obj.id;
        });

        var array2 = $.map($creditRows.toArray(), function(row){
            var el = $(row).find("[name$='person.id']").first();
            var personId = parseInt(el.val());
            var instEl = $(row).find("[name$='institution.id']").first();
            var institutionId = parseInt(instEl.val());
            var retid = personId;
            

            if(personId === -1 || isNaN(personId)) {
                if(institutionId !== -1 && !isNaN(institutionId)) {
                    retid = institutionId;
                }
            }
            if(retid !== -1 && !isNaN(retid)) {
                return retid;
            }
        });

        console.log("comparing ar1:", array1);
        console.log("comparing ar2:", array2);
        return array2.length === 0 || $.compareArray(array1, array2);


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
        _enableSection($('#divInvestigationInformation'));
        _enableSection($('#divSiteInformation'));
        _enableSection($('#divMaterialInformation'));
        _enableSection($('#divCulturalInformation'));
        _enableSection($('#divSpatialInformation'));
        _enableMap();
        _enableSection($('#divTemporalInformation'));
        _enableSection($('#divOtherInformation'));
        _enableSection($('#divIdentifiers'));
        _enableSection($('#relatedCollectionsSection'));
        _enableSection($('#resourceNoteSection'));
        _enableSection($("#creditTable"));
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
        var iData = $form.data("inheritOptionsList");
        if (iData != undefined) {
            $.each(iData, function (idx, options) {
                if ($(options.cbSelector).prop('checked')) {
                    options.inheritSectionCallback();
                }
            });
        };
    }

    function _selectAllInheritanceClicked() {
        var $elem = $("#cbSelectAllInheritance");
        try {
            $elem.data('isUpdatingSections', true);

            var checked = $elem.prop('checked');
            var $sectionCheckboxes = $('.divInheritSection input[type=checkbox]');

            // make all of the section checkboxes just like this checkbox.
            if(checked) {
                _attemptMultipleInheritance($sectionCheckboxes.not(':checked'));
            } else {
                // uncheck all of the checked sections
                $sectionCheckboxes.filter(':checked').each(function () {
                    $(this).click();
                });
            }
        } finally {
            //never leave this checkbox in indeterminate state
            $elem.data('isUpdatingSections', false);
        }
    }

    /**
     * Display the overwrite warning prompt w/ a list of affected sections
     * @param optionsList
     * @param okaySelected callback called  if user select okay
     * @param cancelSelected callback called if user selects cancel
     * @private
     */
    function _displayOverwritePrompt(optionsList, okaySelected, cancelSelected) {
        var $modalDiv = $('#inheritOverwriteAlert');

        //populate list of conflicting sections
        var $ul = $("<ul></ul>");
        $.each(optionsList, function (idx, options) {
            $ul.append("<li>" + $(options.sectionNameSelector).text() + "</li>");
        });
        $modalDiv.find('.list-container').empty().append($ul);

        //by default, treat 'hidden' event as a 'cancel'
        $modalDiv.one("hidden", cancelSelected);

        //if 'okay' clicked,  swap out the 'cancel'  and perform multi-inheritance once the modal is completely gone
        $('#btnInheritOverwriteOkay').one("click", function () {
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
        var optionsList = $.map($checkboxes, function (checkbox, idx) {
            var options = $(checkbox).data("inheritOptions");
            if (!options.isSafeCallback()) {
                unsafeSections.push(options);
            }
            return options;
        });

        //action we'll take if all sections safe OR user expressly permits overwrite
        var _inheritMultiple = function () {
            $checkboxes.prop("checked", true);
            $.each(optionsList, function (idx, options) {
                options.inheritSectionCallback();
            });
        };

        //if any sections unsafe (i.e. inheritance would overwrite previous values),  throw a prompt to the user
        if (unsafeSections.length) {
            _displayOverwritePrompt(unsafeSections, _inheritMultiple, function () {
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
            _enableSection($('#divInheritFromProject'));

        } else {
            $cbSelectAllInheritance.removeAttr('checked');
            _disableSection($('#divInheritFromProject'));
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

    function _populateLatLongTextFields() {
        $("#d_minx").val(Geo.toLon($("#minx").val()));
        $("#d_miny").val(Geo.toLat($("#miny").val()));
        $("#d_maxx").val(Geo.toLon($("#maxx").val()));
        $("#d_maxy").val(Geo.toLat($("#maxy").val()));
    }

    function _disableMap() {
        var $mapdiv = $('#divSpatialInformation');
        $mapdiv.addClass('disable-map');
    }

    function _enableMap() {
        var $mapdiv = $('#divSpatialInformation');
        console.log('enable-map');
        $mapdiv.removeClass('disable-map');
    }

    /**
     * "Reset" a repeat-row table so that it contains N blank rows.  Any non-default input field values are destroyed.
     * If the specified repeat-row table contains more that N rows, this function destroys the extraneous rows.
     *
     * @param repeatableSelector selector for the repeatrow table.
     * @param newSize the number of rows the table will contain.
     */
    var resetRepeatable = function (repeatableSelector, newSize) {
        //ignrore the element if it is a backing <select> for a select2 control.
        if($(repeatableSelector).is(".select2-hidden-accessible")) {return;}
        $(repeatableSelector).find(".repeat-row:not(:first)").remove();
        var $firstRow = $('.repeat-row', repeatableSelector);
        _resetIndexedAttributes($firstRow);
        for (var i = 0; i < newSize - 1; i++) {
            TDAR.repeatrow.cloneSection($('.repeat-row:last', repeatableSelector)[0]);
        }
    };

    /**
     * Convenience function, equivalent to resetRepeatable(repeatableSelector, keywords.length)
     *
     * @param keywords array of strings. length of the array dictates the rowcount after the reset.
     * @param repeatableSelector selector for the repeatrow table
     */
    var resetKeywords = function (keywords, repeatableSelector) {
        var $repeatable = $(repeatableSelector);
        resetRepeatable(repeatableSelector, keywords.length);
    };

    /**
     * Display a confirm prompt and call a callback corresponding to the user's choice
     * @param msg message to display in the prompt.
     * @param okaySelected  handler to call if user clicks "okay"
     * @param cancelSelected handler to call if user clicks "cancel" or dismisses prompt
     * @private
     */
    var _confirm = function (msg, okaySelected, cancelSelected) {
        var confirmed = confirm(msg);
        if (confirmed) {
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
    var registerInheritSection = function (options) {
        var $checkbox = $(options.cbSelector);
        if ($checkbox.length === 0) {
            return;
        }

        var $form = $checkbox.closest("form");
        var formId = $form.attr("id");
        var _options = {
            sectionNameSelector: "",
            isSafeCallback: function () {
                return true;
            },
            inheritSectionCallback: function () {
                _inheritInformation(formId, TDAR.inheritance.json[_options.mappedData]);
            },
            enableSectionCallback: _enableSection 
        };
        $.extend(_options, options);
        //_wrapInheritCallback(_options);
        $form.data("inheritOptionsList").push(_options);
        $checkbox.data("inheritOptions", _options)

        //update contents/state of section when checkbox for that section is toggled
        $(_options.cbSelector).change(function (e) {
            var cb = this;
            var $cb = $(cb);
            if ($cb.prop("checked")) {
                // determine if inheriting would overrwrite existing values
                var isSafe = _options.isSafeCallback();
                if (isSafe) {
                    _options.inheritSectionCallback();
                } else {
                    //not safe!  ask the user for confirmation
                    _confirm("Inheriting from '" + TDAR.common.htmlEncode(TDAR.inheritance.project.title) + "' will overwrite existing values. Continue?", function () {
                                _options.inheritSectionCallback();
                            }, function () {
                                $cb.prop("checked", false);
                                _options.enableSectionCallback($(_options.divSelector));
                                _updateSelectAllCheckboxState();
                            });
                }
            } else {
                //user unchecked inheritance - enable the controls
                _options.enableSectionCallback($(_options.divSelector));
                _updateSelectAllCheckboxState();
                $(_options.divSelector).find('input[type=hidden].dont-inherit').val("");
            }
        });
    };


    function _clearSection(divSection) {
        var $section = $(divSection);

        //first, reset all of the checkboxes
        $section.find(":checkbox").each(function(i) {
            var  elem = this;
            $(elem).prop("checked", elem.defaultChecked);
        });

        //now remove the repeatrows  (sorry, 'reset' not an option)
        $section.find(".repeat-row").each(function() {TDAR.repeatrow.deleteRow(this);});
    }

    function _registerClearSectionButtons(formSelector) {
        $(formSelector || "#metadataForm").find(".btn.clear-section").each(function() {
            var button = this;
            var targetSelector = button.getAttribute("data-clear-target");

            //clear section when button clicked
            $(button).click(function() {
                _clearSection($(targetSelector)[0]);
            });

            //disable when inheritance enabled
            var $cb = $(button).closest(".divInheritSection").find("label.checkbox :checkbox");
            var _onchange = function() {
                $(button).prop("disabled", $cb.prop("checked"));
            };
            $cb.change(_onchange);
            _onchange();
        });
    }

    return{
        resetRepeatable: resetRepeatable,
        resetKeywords: resetKeywords,
        registerInheritSection: registerInheritSection,
        applyInheritance: applyInheritance,
        clearSection: _clearSection,
        registerClearSectionButtons: _registerClearSectionButtons,
    };
})();
