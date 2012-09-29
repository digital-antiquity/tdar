var searchControls;
var selEntityType;
var dataTable = null;
var g_selectedDupeIds = {};

var g_settingsMap = {
        person:{
            tableSelector: '#dupe_datatable',
            sAjaxSource:'/lookup/person',
            "bLengthChange": false,
            "bFilter": true,
            aoColumns:[
                       {sTitle:"id", bUseRendered: false, mDataProp:"id", tdarSortOption:'ID'},
                       {sTitle:"First", mDataProp:"firstName", tdarSortOption:'FIRST_NAME'},
                       {sTitle:"Last", mDataProp:"lastName", tdarSortOption:'LAST_NAME'},
                       {sTitle:"Email", mDataProp:"email", tdarSortOption:'CREATOR_EMAIL', bSortable:false}], //FIXME: make sortable
            sPaginationType:"full_numbers",
            sAjaxDataProp: 'people',
            selectableRows: true,
            requestCallback: getPersonSearchData,
            sDom:'<"datatabletop"ilrp>t<>' //omit the search box
        },
        institution:{
            tableSelector: '#dupe_datatable',
        	sAjaxSource:'/lookup/institution',
            "bLengthChange": false,
            "bFilter": true,
            aoColumns:[
                       {sTitle:"id", bUseRendered: false, mDataProp:"id", tdarSortOption:'ID'},
                       {sTitle:"Name", mDataProp:"name", tdarSortOption:'CREATOR_NAME'}],
            sPaginationType:"full_numbers",
        	sAjaxDataProp: 'institutions',
            selectableRows: true,
            requestCallback: function(searchBoxContents) {
            	return {
            		minLookupLength:0,
            		institution: $('#txtInstitution').val()
            	};
            },
            sDom:'<"datatabletop"ilrp>t<>' //omit the search box
       },
        keyword:{
            tableSelector: '#dupe_datatable',
            sAjaxSource:'/lookup/keyword',
            "bLengthChange": false,
            "bFilter": true,
            aoColumns:[
                       {sTitle:"id", bUseRendered: false, mDataProp:"id", tdarSortOption:'ID'},
                       {sTitle:"Label", mDataProp:"label", tdarSortOption:'LABEL'}],
            sPaginationType:"full_numbers",
            sAjaxDataProp: 'items',
            selectableRows: true,
            requestCallback: function(searchBoxContents) {
                return {keywordType: getKeywordType(selEntityType.val()),
                			term: $('#txtKeyword').val()
                };
            },
            sDom:'<"datatabletop"ilrp>t<>' //omit the search box
        }
};


function registerDataTable() {
    if(dataTable) {
        dataTable.fnDestroy();
        dataTable.empty();
    }
     var lookupType = selEntityType.data('lookupType');
     var settings = g_settingsMap[lookupType];
     
     settings.rowSelectionCallback =  renderSelectedDupes;
     
     dataTable = registerLookupDataTable(settings);
}

//show the correct search control based on the value of the 'entity type' dropdown
function updateSearchControl() {
    searchControls.hide();
    selEntityType = $('#selEntityType');
    var entityTypeVal=selEntityType.val();
    if(entityTypeVal) {
        if(entityTypeVal == 'PERSON') {
            $("#divPersonSearchControl").show();
            selEntityType.data('lookupType', 'person');
        } else if(entityTypeVal == 'INSTITUTION') {
            $("#divInstitutionSearchControl").show();
            selEntityType.data('lookupType', 'institution');
        } else {
            $("#divKeywordSearchControl").show();
            selEntityType.data('lookupType', 'keyword');
        }
    }
    $('#hdnEntityType').val(entityTypeVal);
    
    if(entityTypeVal) {
        registerDataTable();
        clearDupeList();
    }
}

function doSearch() {
    var lookupType  =  selEntityType.data('lookupType');
    if(!lookupType) return;
    if(lookupType == 'person') {
        //since person search is comprised of several fields it doesn't matter what we pass to  fnFilter(), we just want a redraw
        dataTable.fnFilter('IGNORED');
    } else if (lookupType == 'institution') {
        dataTable.fnFilter($('#txtInstitution').val());
    } else if (lookupType == 'keyword') {
        dataTable.fnFilter($('#txtKeyword').val());
    }
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

function getPersonSearchData()  {
	var data = {minLookupLength:0};
    $.each ($(':text', '#divPersonSearchControl'), function(ignored, txtElem){
        data[txtElem.name] = $(txtElem).val();
    });
    return data;
}


//for now we just render everything each time something is checked
function renderSelectedDupes(id, obj, isAdded) {
    $('#frmDupes ul').remove();
    var $ul = $(document.createElement('ul'));
    var dupeCount = 0;
    
    $.each(dataTable.data('selectedRows'), function(idx, data){
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
	dataTable.data('selectedRows', {});
	$('input[type=checkbox]', dataTable).prop('checked', false);
	renderSelectedDupes(null, null, null);
    return false;
}



