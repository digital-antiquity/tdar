var $searchControls;
var $selEntityType;
var $dataTable = null;
var g_selectedDupeIds = {};

var g_settingsMap = {
        person:{
            sAjaxSource:'/lookup/person',
            aoColumns:[
                       {sTitle:"id", bUseRendered: false, mDataProp:"id", fnRender: fnRenderIdColumn, tdarSortOption:'ID'},
                       {sTitle:"First", mDataProp:"firstName", tdarSortOption:'PERSON_FIRST_NAME'},
                       {sTitle:"Last", mDataProp:"lastName", tdarSortOption:'PERSON_LAST_NAME'},
                       {sTitle:"Email", mDataProp:"email", tdarSortOption:'CREATOR_EMAIL'}],
            sAjaxDataProp: 'people',
            sDom:'<"H"lr>t<"F"ip>' //omit the search box
        },
        institution:{
            aoColumns:[
                       {sTitle:"id", bUseRendered: false, mDataProp:"id", fnRender: fnRenderIdColumn, tdarSortOption:'ID'},
                       {sTitle:"Name", mDataProp:"name", tdarSortOption:'CREATOR_NAME'}],
            sAjaxDataProp: 'institutions',
            sAjaxSource:'/lookup/institution'
        },
        keyword:{
            aoColumns:[
                       {sTitle:"id", bUseRendered: false, mDataProp:"id", fnRender: fnRenderIdColumn, tdarSortOption:'ID'},
                       {sTitle:"Label", mDataProp:"label", tdarSortOption:'LABEL'}],
            sAjaxDataProp: 'items',
            sAjaxSource:'/lookup/keyword'
        }
};

function initIndexPageJavascript() { //onload stuff
    var $selEntityType = $("#selEntityType");
    $searchControls = $('.searchControl');
    var $divKeywordSearchControl = $("#divKeywordSearchControl");
    var $divInstitutionSearchControl = $('#divInstitutionSearchControl');
    var $divPersonSearchControl = $('#divPersonSearchControl');
    updateSearchControl();
    $selEntityType.change(updateSearchControl).change();
    applyWatermarks();
    
    $("#btnSearch").click(doSearch);
    
    //register datatable checkbox changes
    $('#dupe_datatable').delegate('input[type=checkbox]', 'change', rowSelected);
    
    $('span.button').button().click(clearDupeList);
}


function rowSelected(evt) {
    var elem = this;
    var $elem = $(elem);
    var id = $elem.val();
    //console.log('rowSelected:' + id + ':' + $elem.prop('checked'));
    
    if($elem.prop('checked')) {
        //get the json data associated w/ the selected row
        var data = $dataTable.fnGetData($elem.parents('tr')[0]);
        console.log(data);
        g_selectedDupeIds[id] = data; 
    } else {
        delete g_selectedDupeIds[id];
    }
    
    renderSelectedDupes(g_selectedDupeIds);
}


function registerDataTable(entityTypeVal) {
    if($dataTable) {
        $dataTable.fnDestroy();
        $dataTable.empty();
        g_selectedDupeIds = {};
    }
     var lookupType = $selEntityType.data('lookupType');
     var settings = g_settingsMap[lookupType];
     var datatableSettings = $.extend({
         "bJQueryUI": true,
         "bProcessing": true,
         "bServerSide": true,
         "sScrollY": "350px",
         "fnServerData": _fnServerData,
         "fnRowCallback": fnRowCallback
     }, settings);
     
     $dataTable = $("#dupe_datatable").dataTable(datatableSettings);
}


function updateSearchControl() {
    $searchControls.hide();
    $selEntityType = $('#selEntityType');
    var entityTypeVal=$selEntityType.val();
    if(entityTypeVal) {
        if(entityTypeVal == 'PERSON') {
            $("#divPersonSearchControl").show();
            $selEntityType.data('lookupType', 'person');
        } else if(entityTypeVal == 'INSTITUTION') {
            $("#divInstitutionSearchControl").show();
            $selEntityType.data('lookupType', 'institution');
        } else {
            $("#divKeywordSearchControl").show();
            $selEntityType.data('lookupType', 'keyword');
        }
    }
    $('#hdnEntityType').val(entityTypeVal);
    
    if(entityTypeVal) {
        registerDataTable(entityTypeVal);
        clearDupeList();
    }
}

function doSearch() {
    var lookupType  =  $selEntityType.data('lookupType');
    if(!lookupType) return;
    if(lookupType == 'person') {
        //since person search is comprised of several fields it doesn't matter what we pass to  fnFilter(), we just want a redraw
        $dataTable.fnFilter('IGNORED');
    } else if (lookupType == 'institution') {
        $dataTable.fnFilter($('#txtInstitution').val());
    } else if (lookupType == 'keyword') {
        $dataTable.fnFilter($('#txtKeyword').val());
    }
}

function handleLookupResult(data) {
    console.log("handleLookupResult:" +  data);
}



//intercept the server request,  and translate the parameters to server format.  similarly,  take the json returned by the jserver
//and translate to format expected by the client.
function _fnServerData(sSource, aoData, fnCallback) {
    //don't retreive any data until we have valid search parameters.
    var lookupType = $selEntityType.data('lookupType');

    $.ajax({
        dataType: 'jsonp',
        url: sSource,
        data: convertRequest(aoData, lookupType),
        success: function(_data) {
            fnCallback(convertResponse(_data, lookupType));  //intercept data returned by server, translate to client format
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.error("your query sucks:" + errorThrown);
        }
    });
}


function fnRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
    //determine whether the user selected this item already (if so check the box)
    var $cb = $(nRow).find('input[type=checkbox]'); 
    var id = $cb.val();
    if(g_selectedDupeIds[id]) {
        $cb.prop('checked', true);
    }
    return nRow;
}


/**
 * alter the request that prior to being sent to the server
 * @param aoData
 * @param lookupType
 */
function convertRequest(aoData, lookupType) {
    var oData = {};
    //first convert the request from array of key/val pairs to map<string,string>.
    $.each(aoData, function(){
        oData[this.name] = this.value;
    });

    //derive sort column from the field name and reversed status
    var aoColumns = g_settingsMap[lookupType].aoColumns;
    var tdarSortOption = aoColumns[oData["iSortCol_0"]].tdarSortOption;
    var sSortReversed = {desc:'true'}[oData["sSortDir_0"]];
    if(sSortReversed) tdarSortOption += '_REVERSE';
    var translatedData = {
            startRecord:oData.iDisplayStart,
            recordsPerPage:oData.iDisplayLength,
            minLookupLength:0,
            sortField: tdarSortOption
    };
    
    switch(lookupType) {
    case "person":
        getPersonSearchData(translatedData);
        break;
    case "keyword":
        translatedData.keywordType = getKeywordType($selEntityType.val());
        translatedData.term = oData.sSearch;
        break;
    case "institution":
        translatedData.institution = oData.sSearch;
        break;
    }
    return translatedData;
}

//FIXME: this is a dumb, hackey way to get the keywordType to send the lookup controller.
//a better way would be to send enum value to lookupcontroller   
function getKeywordType(enumVal) {
	return {
	    KEYWORD_CULTURE_KEYWORD:'CultureKeyword',
	    KEYWORD_GEOGRAPHIC_KEYWORD:'GeographicKeyword',
	    KEYWORD_INVESTIGATION_TYPE:'InvestigationType',
	    KEYWORD_MATERIAL_KEYWORD:'MaterialKeyword',
	    KEYWORD_OTHER_KEYWORD:'OtherKeyword',
	    KEYWORD_SITE_NAME_KEYWORD:'SiteNameKeyword',
	    KEYWORD_SITE_TYPE_KEYWORD:'SiteTypeKeyword'
	}[enumVal];
}

function getPersonSearchData(translatedData)  {
    $.each ($(':text', '#divPersonSearchControl'), function(ignored, txtElem){
        translatedData[txtElem.name] = $(txtElem).val();
    });
}

/**
 *  alter the response returned from the server before it is received by the datatable
 * @param data - xhr response
 * @param lookupType - lookupType stored in $selEntityType.data
 */
function convertResponse(data, lookupType) {
    var translatedData = {
            iTotalDisplayRecords:data.status.totalRecords,
            iTotalRecords:data.status.totalRecords    
    };
    
    var sAjaxDataProp = g_settingsMap[lookupType].sAjaxDataProp;
    translatedData[sAjaxDataProp] = data[sAjaxDataProp];
    return translatedData;
}


function renderSelectedDupes(duplicateMap) {
    $('#frmDupes ul').remove();
    var $ul = $(document.createElement('ul'));
    var dupeCount = 0;
    
    
    $.each(duplicateMap, function(idx, data){
        dupeCount++;
        //FIXME: need a better way to display summary label for selected item, or maybe we should just have the same table structure as datatable.
        var label = data.name;
        if(!label) label = data.label; 
        var $li = $('<li></li>').append('<input type="hidden" name="selectedDupeIds" value="' + idx + '" />').
            append('<span>' + label + ' (id:' + idx + ')</span>');
        $ul.append($li);
        console.log('renderdupes:item:' + idx + ":" + data.name);
    });
    $('#frmDupes').append($ul);
    $('#spanDupeCount').text(dupeCount);
    $('#pDupeInfo').toggle(dupeCount > 0);
}


function clearDupeList() {
    //todo use jqueryui button instead of html button
    g_selectedDupeIds = {};
    renderSelectedDupes(g_selectedDupeIds);
    $dataTable.fnDraw();
    return false;
}



