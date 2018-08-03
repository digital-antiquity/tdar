/**
 * TDAR  authority-management library.  Used by the Authority-management (aka record-deduper) pages.
 * @type {{}}
 */
TDAR.authority = {};
TDAR.authority = function () {
    "use strict";
    var self = {};

    var searchControls;
    var selEntityType;
    var dataTable = null;

    //map of datatable init options for each search type
    var g_settingsMap = {
        person: {
            tableSelector: '#dupe_datatable',
            sAjaxSource: TDAR.uri() + 'api/lookup/person',
            "bLengthChange": true,
            "bFilter": true,
            aoColumns: [
                {sTitle: "person id", bUseRendered: false, mDataProp: "id", tdarSortOption: 'ID'},
                {sTitle: "registered", mDataProp: "registered", tdarSortOption: 'ID', bSortable: false},
                {sTitle: "First", mDataProp: "firstName", tdarSortOption: 'FIRST_NAME'},
                {sTitle: "Last", mDataProp: "lastName", tdarSortOption: 'LAST_NAME'},
                {sTitle: "Email", mDataProp: "email", tdarSortOption: 'CREATOR_EMAIL', bSortable: false}
            ], //FIXME: make sortable
            sPaginationType: "bootstrap",
            sAjaxDataProp: 'people',
            selectableRows: true,
            requestCallback: _getPersonSearchData,
            "sDom": "<'row'<'col-6'l><'pull-right col-3'r>>t<'row'<'col-4'i><'col-5'p>>"
        },
        institution: {
            tableSelector: '#dupe_datatable',
            sAjaxSource: TDAR.uri() + 'api/lookup/institution',
            "bLengthChange": true,
            "bFilter": true,
            aoColumns: [
                {sTitle: "id", bUseRendered: false, mDataProp: "id", tdarSortOption: 'ID'},
                {sTitle: "Name", mDataProp: "name", tdarSortOption: 'CREATOR_NAME'}
            ],
            "sDom": "<'row'<'col-6'l><'pull-right col-3'r>>t<'row'<'col-4'i><'col-5'p>>",  //no text filter!
            sPaginationType: "bootstrap",
            sAjaxDataProp: 'institutions',
            selectableRows: true,
            requestCallback: function (searchBoxContents) {
                return {
                    minLookupLength: 0,
                    institution: $('#txtInstitution').val()
                };
            }
//            sDom:'<"datatabletop"ilrp>t<>' //omit the search box
        },
        keyword: {
            tableSelector: '#dupe_datatable',
            sAjaxSource: TDAR.uri() + 'api/lookup/keyword',
            "bLengthChange": true,
            "bFilter": true,
            aoColumns: [
                {sTitle: "id", bUseRendered: false, mDataProp: "id", tdarSortOption: 'ID'},
                {sTitle: "Label", mDataProp: "label", tdarSortOption: 'LABEL'}
            ],
            sPaginationType: "bootstrap",
            sAjaxDataProp: 'items',
            selectableRows: true,
            requestCallback: function (searchBoxContents) {
                return {keywordType: _getKeywordType(selEntityType.val()),
                    term: $('#txtKeyword').val()
                };
            },
            "sDom": "<'row'<'col-6'l><'pull-right col-3'r>>t<'row'<'col-4'i><'col-5'p>>"
        }
    };

    /**
     * initialize a datatable that will display search results for the currently selected search type
     * @private
     */
    function _registerDataTable() {
        if (dataTable) {
            dataTable.fnDestroy();
            dataTable.empty();
        }
        var lookupType = selEntityType.data('lookupType');
        var settings = g_settingsMap[lookupType];
        settings.rowSelectionCallback = _renderSelectedDupes;
        dataTable = TDAR.datatable.registerLookupDataTable(settings);
    }

    /**
     * show the correct search control based on the value of the 'entity type' dropdown
     */
    function _updateSearchControl() {
        searchControls.hide();
        selEntityType = $('#selEntityType');
        var entityTypeVal = selEntityType.val();
        if (entityTypeVal) {
            if (entityTypeVal == 'PERSON') {
                $("#divPersonSearchControl").show();
                selEntityType.data('lookupType', 'person');
            } else if (entityTypeVal == 'INSTITUTION') {
                $("#divInstitutionSearchControl").show();
                selEntityType.data('lookupType', 'institution');
            } else {
                $("#divKeywordSearchControl").show();
                selEntityType.data('lookupType', 'keyword');
            }
        }
        $('#hdnEntityType').val(entityTypeVal);

        if (entityTypeVal) {
            _registerDataTable();
            _clearDupeList();
        }
    }

    //FIXME: this is a dumb, hackey way to get the keywordType to send the lookup controller.
    /**
     * return the keyword class name for the specified enumVal (used for "lookupType" when performing keyword lookup
     * search.
     *
     * @param enumVal value of the "search type" select input
     * @returns {string} lookupType value to include in keyword lookup request
     * @private
     */
    function _getKeywordType(enumVal) {
        return {
            KEYWORD_CULTURE_KEYWORD: 'CultureKeyword',
            KEYWORD_GEOGRAPHIC_KEYWORD: 'GeographicKeyword',
            KEYWORD_INVESTIGATION_TYPE: 'InvestigationType',
            KEYWORD_MATERIAL_KEYWORD: 'MaterialKeyword',
            KEYWORD_OTHER_KEYWORD: 'OtherKeyword',
            KEYWORD_SITE_NAME_KEYWORD: 'SiteNameKeyword',
            KEYWORD_SITE_TYPE_KEYWORD: 'SiteTypeKeyword'
        }[enumVal];
    }

    /**
     * Convert name, value attributes in person search fields into a jsobject  (for use as "data" argument in $.ajax request)
     * @returns {{minLookupLength: number}}
     * @private
     */
    function _getPersonSearchData() {
        var data = {minLookupLength: 0};
        $.each($(':text', '#divPersonSearchControl'), function (ignored, txtElem) {
            data[txtElem.name] = $(txtElem).val();
        });
        return data;
    }

    /**
     * This is a row-selection callback for the datatable widget.  It displays a running list of selected rows
     * in a separate UL under the table.  The callback parameters are unused - they exist merely to satisfy the
     * datatable callback spec
     * @param ignored1
     * @param ignored2
     * @param ignored3
     * @private
     */
    function _renderSelectedDupes(ignored1, ignored2, ignored3) {
        $('#frmDupes ul').remove();
        var $ul = $(document.createElement('ul'));
        var dupeCount = 0;

        $.each(dataTable.data('selectedRows'), function (idx, data) {
            dupeCount++;
            //FIXME: need a better way to display summary label for selected item, or maybe we should just have the same table structure as datatable.
            var label = data.name;
            if (!label) {
                label = data.label;
            }
            var $li = $('<li></li>').append('<input type="hidden" name="selectedDupeIds" value="' + idx + '" />').append('<span>' + label + ' (id:' + idx + ')</span>');
            $ul.append($li);
            //console.log('renderdupes:item:' + idx + ":" + data.name);
        });
        $('#frmDupes').append($ul);
        $('#spanDupeCount').text(dupeCount);
        $('#pDupeInfo').toggle(dupeCount > 0);
    }

    /**
     * clear current list of selected dupelicates.
     * @returns {boolean} false.  because.
     */
    function _clearDupeList() {
        dataTable.data('selectedRows', {});
        $('input[type=checkbox]', dataTable).prop('checked', false);
        _renderSelectedDupes(null, null, null);
        return false;
    }

    /**
     * Initialize form controls, event listeners.
     */
    function _initAuthTable() {
        var selEntityType = $("#selEntityType");
        if (selEntityType != undefined) {
            searchControls = $('.searchControl');
            selEntityType.change(_updateSearchControl).change();
            TDAR.common.applyWatermarks(document);
            $('span.button').button().click(_clearDupeList);
        }

    }

    return {
        clearDupeList: _clearDupeList,
        updateSearchControl: _updateSearchControl,
        initAuthTable: _initAuthTable
    };
}();
