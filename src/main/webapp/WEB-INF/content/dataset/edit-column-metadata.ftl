<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<title>tDAR column metadata registration for ${dataset.title}</title>
<meta name="lastModifiedDate" content="$Date$"/>

<style type="text/css"> 
    .ui-autocomplete {
        max-height: 144pt;
        overflow-y: auto;
        overflow-x: hidden;
        line-height: 1em;
    }
    /* IE 6 doesn't support max-height
     * we use height instead, but this forces the menu to always be this tall
     */
    * html .ui-autocomplete {
        height: 144pt;
        overflow-x: hidden;
    }

    .ui-menu .ui-menu-item {
        border: 0px;
        margin: 0px;
        padding: 1pt;
    }
    
    .ui-menu-item a {
        border:1px solid transparent;
        min-height: 1em;
    }
    
    .ui-state-hover {
        
    }    
</style> 

<style type="text/css"> 

</style>

<script type="text/javascript" src="<@s.url value='/includes/jquery.textarearesizer.js'/>"></script> 
<script type='text/javascript' src='<@s.url value="/includes/jquery.FormNavigate.js"/>'></script> 

<script type='text/javascript'>
$(document).ready(function() {
    $('textarea.resizable:not(.processed)').TextAreaResizer();
    // set up ajax calls, no caching
    $.ajaxSetup({ 
        cache: false 
    });
    // XXX: to handle refresh properly, iterate through columns and display 
    // measurement units if column encoding is set to "Measurement"
    <@s.iterator status='columnStatus' value='dataTable.sortedDataTableColumns' var='column'>
    updateMeasurementUnitDiv(${columnStatus.index});
    </@s.iterator>
    
    //determine when to show coding-sheet, ontology selection based on column encoding value
    $('.columnEncoding').change(columnEncodingChanged).change();
    
     $("save-column-metadata").FormNavigate("Leaving the page will cause any unsaved data to be lost!");
     $("#table_select").unbind("change");
    
});

//TODO: this probably should be merged with columnEncodingChanged
function updateMeasurementUnitDiv(index) {
    // hard coded to match "measurement" id
    if ($("#columnEncoding_" + index).val() == "MEASUREMENT") {
        $("#measurementUnitDiv_header").show();
        $("#measurementUnitDiv_" + index).show();
    }
    else {
        $("#measurementUnitDiv_" + index).hide();
    }
    
}

function columnEncodingChanged() {
    var selectElement = this;
    var val = $(selectElement).children('option:selected').val();
    var text = $(selectElement).children('option:selected').text().toLowerCase();
    var codingSheetDiv = findSibling(selectElement, 'divCodingSheet');
    var ontologyDiv = findSibling(selectElement, 'divOntology');       
    console.debug('columnEncodingChanged:' + this + ' val:' + val +  ' text:' + text);
    
    //only show the coding sheet selection for coded entries 
    if((text.indexOf('coded') != -1) || (text.indexOf('other') != -1)) {
        $(codingSheetDiv).show();
        console.debug('showing:' + codingSheetDiv);
    } else {
        $(codingSheetDiv).hide();
        console.debug('hiding:' + codingSheetDiv);
    }
    
    //don't show the ontology list for arbitrary real or arbitrary integer encoding
    if( (text.indexOf('numeric') != -1) || (text.indexOf('count') != -1) || (text.indexOf('measurement') != -1) || (text.indexOf('other') != -1)) {
        $(ontologyDiv).hide();
    } else {
        $(ontologyDiv).show();
    }
    
    //don't show the label for the mapping subsection if both of its elements are hidden
    var translateOrMapLabel = $(selectElement).siblings('.translateOrMapLabel');
    if( $(codingSheetDiv).is(":visible") || $(ontologyDiv).is(":visible")) {
        $(translateOrMapLabel).show();
    } else {
        $(translateOrMapLabel).hide();
    }
    
}

function changeSubcategory(index) {
    var selectedCategoryId = $('#categoryVariableId_' + index).val();
    var selectedSubcategoryId = $('#subcategoryId_'+index).val();
    $.get("<@s.url value='/resource/ajax/column-metadata-subcategories'/>", { 
        "categoryVariableId" : selectedCategoryId,
        "index" : index,

    },
    function (data, textStatus) {
        $('#subcategoryDivId_' + index).html(data);
    });
}


// Coding Sheets
var codingSheets = [
{id:"", name:""}<#rt>
<@s.iterator status="stat" var="sheet" value="%{allCodingSheets}">
    ,{id:"${sheet.id?c}", name:"${sheet.title?js_string}  (tDAR ID:${sheet.id?c})" } 
</@s.iterator>
];


// Ontologies
var ontologies = [
{id:"", name:""}<#rt>
<@s.iterator status="stat" var="ont" value="%{allOntologies}">
    ,{id:"${ont.id?c}",  name:"${ont.title?js_string} (tDAR ID:${ont.id?c})"} 
</@s.iterator>
];




function applyLocalAutoComplete(selector, db) {
    $(selector).autocomplete({
        source: function(request,response) {
            var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
            response($.map(db, function(item) {
                if(matcher.test(item.name)){
                    console.debug('returing ' + item.name);
                    return {value: item.name, id: item.id}
                }
                }));
        },
        minLength:0,
        select: function(event, ui) {
            var input = this; //'this' points to the target element 
            console.debug('selected:' + ui.item);
            //get the hidden input next to the textbox and set the id field
            var elem  = findSibling(input, 'ids');
            console.debug(elem);
            elem.val(ui.item.id);
            $(input).removeClass("error");
            
        },
        change: function(event, ui) {
            var input = this;  //'this' points to the target element
            //don't allow invalid selection
            //FIXME: per jquery combobox demo,  ui.item should be null when the user manually types in the input value (as opposed to selecting from dropdown).  
            //But I'm not seeing that behavior (ui.item has a value) So,  this block execcutes even after select() fires,  which is redundant. 
//            if ( !ui.item ) {
            if(true) { 
                //did they type in an exact match to an elment?
                var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( $(this).val() ) + "$", "i" );
                var valid = false;
                $.each(db, function(k,v){
                    if(v.name.match(matcher)) {
                        valid = true;
                        findSibling(input, "ids").val(v.id);
                        return false;
                    }
                });
                if(!valid) {
                    console.debug("invalid entry - clearing input box and hidden id value");
                    findSibling(input,"ids").val("");
                    $(input).val("");
                    $(input).addClass("error");
                } 
                
            }
        }
        
    });
    
    //add a button to expand the full list        
    $(selector).each(function(k,v){
        var input = $(v);
        var container = $(input).next();
        container.click(function(){
            var expanded = input.autocomplete("widget").is(":visible");
            console.debug("expanded:" + expanded);
            //close the droplist if already expanded, otherwise show the full list
            if(expanded) {
                input.autocomplete('close');
            } else {
                input.autocomplete('search', '');
                input.focus();
            }
            return false;
        });

        //FIXME: the 'text' option is being ignored, so i'm just having the content of the button be blank
        container.removeClass( "ui-corner-all" );
        container.addClass( "ui-corner-right ui-button-icon" )
    });
}

$(function() {
    console.debug('binding autocompletes');
    $('#table_select').change(function() {
    window.location='?dataTableId='+$(this).val();
    });
    applyLocalAutoComplete('.codingsheet', codingSheets);
    applyLocalAutoComplete('.ontology', ontologies);
    $('button.ui-button').hover(
        function() {$(this).addClass("ui-state-hover");},
        function() {$(this).removeClass("ui-state-hover");}
    );
});

</script>
</head>
<body>
<@edit.toolbar "dataset" "columns" />
<@edit.showControllerErrors/>

<@s.form method='post' action='save-column-metadata'>
<@s.hidden name='resourceId' value='${resource.id?c}'/>
<@s.hidden name='dataTableId' value='${dataTable.id?c}'/>
<div class='glide'>
<h2>Column Description and Mapping: ${dataTable.displayName}</h2>
<#if dataset.dataTables.size() gt 1>
<p>
There are multiple tables in your dataset.  You can switch between them by clicking
on one of the links below.  Please remember to save any changes before you switch.
</p>

<label for="table_select">Choose Table:</label>
<select id="table_select" name="table_select">
<#assign count=0>
<@s.iterator value='%{dataset.dataTables}' var='table' status="status1">
  <#assign count=count+1 />
  <option value="${table.id?c}" <#if table?? && table.id == dataTable.id>selected
  </#if>
  >${count}. ${table.displayName}</option>
</@s.iterator>
</select>

<#-- <@s.select id="table_select" name="table_select" label="Choose Table:  "
   listValue="displayName" listKey="id"  emptyOption="false" list="%{dataset.dataTables}"/>
-->
</#if>

</div>

<#if dataTable.dataTableColumns??>
<@s.iterator status='columnStatus' value='dataTable.sortedDataTableColumns' var='column'>

<div class="glide" id="columnDiv_${columnStatus.index}">
  <h3>Column: ${column.displayName}</h3>
  <!-- <small>(${column.columnDataType})</small> -->
<@s.select id='columnEncoding_${columnStatus.index}' name='columnEncodingTypes[${columnStatus.index}]'
label="Column Type:"
onchange='updateMeasurementUnitDiv(${columnStatus.index});' cssClass="columnEncoding"
 listValue='label' emptyOption='true' list='%{allColumnEncodingTypes}'/>
<span id='measurementUnitDiv_${columnStatus.index}' style='display:none;'>
<@s.select name='measurementUnits[${columnStatus.index}]'
 listValue='fullName' emptyOption='true' list='%{allMeasurementUnits}'/>
</span>
<br>
<b>Categorize the data to simplify integration:</b><br/>

<@s.select id='categoryVariableId_${columnStatus.index}' 
        name='categoryVariableIds[${columnStatus.index}]' 
        onchange='changeSubcategory(${columnStatus.index})'
        emptyOption='true'
        listKey='id'
        listValue='name'
        list='%{allDomainCategories}'
        label="Category:"
        />

<span id="subcategoryDivId_${columnStatus.index}">
    <#if subcategories[columnStatus.index]??>
    <@s.select 
        id='subcategoryId_${columnStatus.index}' 
        name='subcategoryIds[${columnStatus.index}]'
        list='%{subcategories[${columnStatus.index}]}'
        listKey='id'
        listValue='name'
        emptyOption='true'
        label="Subcategory:"
    />
    <#else>
        <select id='subcategoryId_${columnStatus.index}' name='subcategoryIds[${columnStatus.index}]'>
            <option value='-1'>N/A</option>
        </select>
    </#if>
</span>
<br/>
<b>Please describe the data in this column, how it was collected, tools used, screen size, calibration, etc.</b><br/>
<@s.textarea name='columnDescriptions[${columnStatus.index}]' rows='2' cols='12' cssClass="resizable" />

<span class ="translateOrMapLabel">
    <br />
    <b>Translate your data using a Coding Sheet or map it to an Ontology:</b>
</span>
<div id='divCodingSheet-${columnStatus.index}'>
<#if allCodingSheets?? && ! allCodingSheets.isEmpty()>
        <@s.hidden name="codingSheetIds[${columnStatus.index}]" />
        <@s.textfield name="codingSheetNames[${columnStatus.index}]" label="Coding Sheet:" cssClass="longfield codingsheet" />
        <button title="Expand entire list" tabindex="-1" class="ui-button ui-widget ui-state-default ui-button-icon-only ui-corner-right ui-button-icon" role="button" aria-disabled="false" style="height:18px; position:relative;top:4px;left:-5px;clear:none;">
            <span class="ui-button-icon-primary ui-icon ui-icon-triangle-1-s"></span></button>
<#else>
    None available.
</#if>
<span>
(<a href='<@s.url value="/coding-sheet/add?returnToResourceMappingId=${resource.id?c}"/>'><small>create new coding sheet</small></a>)
</span>
</div>
 <div id='divOntology-${columnStatus.index}'>
<#if allOntologies?? && ! allOntologies.isEmpty()>
        <@s.hidden name="ontologyIds[${columnStatus.index}]" />
        <@s.textfield name="ontologyNames[${columnStatus.index}]" label="Ontology:" cssClass="longfield ontology" />
        <button title="Expand entire list" tabindex="-1" class="ui-button ui-widget ui-state-default ui-button-icon-only ui-corner-right ui-button-icon" role="button" aria-disabled="false" style="height:18px; position:relative;top:4px;left:-5px;clear:none;">
            <span class="ui-button-icon-primary ui-icon ui-icon-triangle-1-s"></span></button>
<#else>
    None available.
</#if>
<span>
(<a href='<@s.url value="/ontology/add" projectId='${resource.project.id?c}' returnToResourceMappingId="${resource.id?c}"/>'><small>create new ontology</small></a>)
</span>
</div>
</div>

</@s.iterator>
</#if>
<@edit.submit "Next: Map column values to Ontologies" false/>
</@s.form>

</body>