TDAR.advancedSearch = {}
TDAR.advancedSearch = (function () {
    "use strict";

    /**
     * Initialize the "advanced search" page (including form validation,  autocompletes, term UI, backbutton safety)
     */
    function _initAdvancedSearch() {
        TDAR.common.initFormValidation($('#searchGroups')[0]);
        TDAR.repeatrow.registerRepeatable(".repeatLastRow");

        // when user changes searchType: swap out the term ui snippet
        $('#searchGroups').on('change', '.searchType', function (evt) {
            "use strict";

            //console.log("change event on %s", this.id);
            var $select = $(this);
            var searchType = $select.val();
            var $controlGroup = $select.parent();

            // get a copy of the template term controls
            var row = $('div.' + searchType, '#template').clone();
            // figure out which group, rownum we are in, and then update attribute
            // values of the cloned row
            var groupnum = $(this).closest(".grouptable").data("groupnum");

            var $searchType = $(".searchType",$controlGroup);
            var name = $searchType.attr("name");
            name = name.replace(/groups\[(\d+)\](.+)/,"groups["+groupnum+"]$2");
//            console.log($(".searchType",$controlGroup).attr("name"),name);
            $searchType.attr("name",name);
            var rownum = $(this).closest(".grouptable").children(".termrow:visible").index($(this).closest(".termrow"));
            _updateAttributesForRow($(row), groupnum, rownum);

            // remove whatever is currently inside of the term-container and replace with the new term
            var $termContainer = $controlGroup.find('div.term-container');

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

            _initializeSection(row);
        });

        // after rows added, replace attribute values
        $('.grouptable').on('repeatrowadded', function (evt, parentElement, row, idx) {
            var $row = $(row);
            // console.log('added row:' + $row + ' p2:' + idx);
            var $repeatable = $(this);
            var $select = $row.find('.searchType');
            $select.val($select.find("option:first").val()).trigger("change");
            $select.removeAttr("readonly");
            _updateAttributesForRow($row, $repeatable.data('groupnum'), idx);
        });

        // more handling of added/removed rows
        // TODO: this needs to be refactored if we implement multiple groups (i
        // think)
        $('#searchGroups').on('repeatrowadded', '.grouptable', function (evt) {
            var $groupDiv = $(this).closest('.searchgroup');
            _showGroupingSelectorIfNecessary($groupDiv);
        });
        $('#searchGroups').on('repeatrowdeleted', '.grouptable', function (evt) {
            var $groupDiv = $(this).closest('.searchgroup');
            _showGroupingSelectorIfNecessary($groupDiv);
        });

        // mimic combobox - show complete list when user clicks down-arrow
        $('#searchGroups').on("click", ".down-arrow", function () {
            $(this).siblings('.projectcombo, .collectioncombo').focus().autocomplete("search", "");
        });

        // if coming here via the backbutton the submit button may be disabled, so
        // we force it to be enabled
        $('#searchButton').prop('disabled', false);

        // perform load handlers on anything currently in the dom (e.g. stuff added
        // by our backbutton hack, or from the server via 'refine your search' or
        // INPUT action)
        _initializeSection($('#searchGroups')[0]);
    }

    /**
     * Initialize advanced-search-specific  UI controls, and register event handlers within a container element.
     *
     * Useful for wiring up an entire form (e.g. after we reconstruct completed form DOM after back-button navigation),
     * or a single div (e.g. when user adds a new search term).
     *
     * @param containerElem
     * @private
     */
    function _initializeSection(containerElem) {
        TDAR.common.applyWatermarks(containerElem);

        // HACK: registering datetype fields if any were created. We should just
        // make a smarter validator rule
        if ($('.coverageDateType', containerElem).length) {
            TDAR.common.prepareDateFields($('.coverageDateType', containerElem));
        }

        // register any treeviews
        $('.tdar-treeview', containerElem).treeview({
            collapsed: true
        });

        // register any person autocomplete fields (don't cull nonusers, don't show
        // the 'create person' option)
        var $personAutoFields = $('.nameAutoComplete', containerElem);
        if ($personAutoFields.length) {
            TDAR.autocomplete.applyPersonAutoComplete($personAutoFields, false, false);
        }

        // similar setup for institution autocompletes
        var $institutionAutoFields = $('.institutionAutoComplete', containerElem);
        if ($institutionAutoFields.length) {
            TDAR.autocomplete.applyInstitutionAutocomplete($institutionAutoFields, false);
        }

        $('.datepicker', containerElem).datepicker({
            dateFormat: 'm/d/y'
        });

        // collection, project combo boxes
        TDAR.autocomplete.applyResourceAutocomplete($('.projectcombo', containerElem), "PROJECT");
        TDAR.autocomplete.applyCollectionAutocomplete($('.collectioncombo', containerElem), {
            minLength: 0
        }, {});

        $('#searchGroups').find('.searchgroup').each(function () {
            _showGroupingSelectorIfNecessary($(this));
        });
    }

    /**
     * Perform a stringformat on the specified element attribute value,  using a map to serve as
     * the search/replace values.
     * @param $elem  element
     * @param attr name of attribute value to format
     * @param map  jsobject with replacement mappings.
     * @private
     */
    function _replaceAttributeValues($elem, attr, map) {
        if (typeof $elem.attr(attr) === 'undefined') {
            return;
        }
        var newstr = $elem.attr(attr);
        for (var key in map) {
            newstr = newstr.split(key).join(map[key]);
        }
        $elem.attr(attr, newstr);
    }

    /**
     * Modify certain attributes within container element so to indicate they belong to the specified group, row number
     * (according to the attribute naming conventions that we use for elements in a 'repeatrow' section, e.g
     * "<input type=hidden id="personRow_1_5"> would be the id value for an element in group#2, row 6).
     *
     * @param $root container element
     * @param groupnum group number (at time of writing, only one search 'group' is supported, so this is always '0'
     * @param rownum  row number
     * @private
     */
    function _updateAttributesForRow($root, groupnum, rownum) {
        // todo: *[id], *[name] instead
        var map = {
            "{groupid}": groupnum,
            "{termid}": rownum};
        $.each($root.find('*'), function (ignored, elem) {
            // don't update all attributes, just id, name, for, etc.
            $.each([ "id", "name", "for", "autocompleteIdElement", "autocompleteParentElement" ], function (ignored, attrName) {
                _replaceAttributeValues($(elem), attrName, map);
            });
        });
    }

    /**
     * Show the "match all / match any" form field  if the form contains multiple search terms.  Otherwise hide it
     *
     * @param $groupDiv
     * @private
     */
    function _showGroupingSelectorIfNecessary($groupDiv) {
        // if more than one term, show the grouping selector
        var $visibleRows = $('.grouptable .repeat-row:visible', $groupDiv);
        var $groupingControl = $groupDiv.find(".groupingSelectDiv");
        if ($visibleRows.length > 1) {
            $groupingControl.find('select').show();
            $groupingControl.addClass("in");
        } else {
            $groupingControl.removeClass("in");
            $groupingControl.find('select').hide();

        }
    }

    /**
     * Serialize the current state of the search form DOM and write it to a hidden TEXTAREA in a different form.
     *
     * Useful when rendering the page when user navigates to the search form via a back-button navigation. We rely on
     * ubiquitous (but non-standard) browser behavior to recover this state and rebuild the DOM as it existed at
     * the moment the user submitted the search form.
     *
     * @private
     */
    function _serializeFormState() {
        $("#searchGroups").submit(function () {
            var frm = this;
            var $frm = $(this);

            // modify attribute to reflect *current* value of property
            _setAttributesToPropValue($('option', frm), "selected");
            _setAttributesToPropValue($('input[type=checkbox]', frm), "checked");
            // Note: in theory radio buttons and disabled items need similar
            // treatment, but they aren't used in this form

            $("input[type=text],input[type=hidden]", frm).each(function (idx, inputElem) {
                $(inputElem).attr("value", inputElem.value);
            });

            //don't persist dom elements created by google map api
            //console.log("clearing map information")
            $('#large-google-map').removeData();
            $('#large-google-map').empty("div");

            $("#autosave").val($frm.html());

            //if doing resource search, clear collection search field so we aren't confused about which tab to display
            $("#queryField").val("");
        });
    }

    /**
     * Back-button hackery: set the attribute values of a form input element to match the current value of the same-named element property.
     *
     * This is done during serialization so that the 'select, checkbox, and radio inputs will render with the last
     * -selected value prior to when the user submitted the form.
     *
     * @param $formElements
     * @param booleanPropertyName
     * @private
     */
    function _setAttributesToPropValue($formElements, booleanPropertyName) {

        // assumption: the property named ${booleanPropertyName} is a boolean
        // assumption: the attribute named ${booleanPropertyName} is a string, and
        // the only acceptable value (if exists) is
        // ${booleanPropertyName}="${booleanPropertyName}"
        $formElements.each(function (idx, elem) {
            var $elem = $(elem);
            if ($elem.prop(booleanPropertyName)) {
                $elem.attr(booleanPropertyName, booleanPropertyName);
                //console.log("setting %s attribute for elem:%s", booleanPropertyName, elem);
            } else {
                $elem.removeAttr(booleanPropertyName);
                //console.log("removing %s attribute for elem:%s", booleanPropertyName, elem);
            }
        });
    }

    function _initializeResultsPage() {
        $("#recordsPerPage").change(function () {
            var url = window.location.search.replace(/([?&]+)recordsPerPage=([^&]+)/g, "");
            //are we adding a querystring or merely appending a name/value pair, i.e. do we need a '?' or '&'?
            var prefix = "";
            if (url.indexOf("?") != 0) {
                prefix = "?";
            }
            url = prefix + url + "&recordsPerPage=" + $('#recordsPerPage').val();
            TDAR.windowLocation(url);
        });

        $("#sortField").change(function () {
            var url = window.location.search.replace(/([?&]+)sortField=([^&]+)/g, "");
            //are we adding a querystring or merely appending a name/value pair, i.e. do we need a '?' or '&'?
            var prefix = "";
            if (url.indexOf("?") != 0) {
                prefix = "?";
            }
            url = prefix + url + "&sortField=" + $('#sortField').val();
            TDAR.windowLocation(url);
        });

    }
    
    return {
        serializeFormState: _serializeFormState,
        initAdvancedSearch: _initAdvancedSearch,
        "initializeResultsPage" : _initializeResultsPage
    };
})();

