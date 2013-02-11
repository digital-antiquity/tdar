<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<title>Edit Table Metadata for ${dataset.title}</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>
<body>
<@edit.sidebar/>

    <h1>Edit Table Metadata for ${dataset.title}</h1>
    <h3>Table ${dataTable.displayName}, ${dataTable.dataTableColumns?size } columns</h3>



<#if dataTable.dataTableColumns?has_content>

<!--TODO: .container sets content width, it should be outside of grid layout  (or a grid-layout parent) --> 
<!-- we break this rule so that navbar will be correct with when it is .affix'd, for all responsive profiles --> 
<div id='subnavbar2' class="subnavbar"  data-offset-top="250" data-spy="affix">
    <div class="navbar">
        <#-- <select name="chooseColumn" onChange="goToColumn(this)"> -->
        <div class="navbar-inner">
            <ul class="nav">
                <li>
                	<a href="#top"><b>top</b></a>
            	</li>
                <li>
<span style="display:inline-block">
                        <b style="margin-top: 10px !important;display: inline-block;margin-left: 10px;">Jump to a column</b>
<!--                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                        <b class="caret"></b> -->
                    <form class="inline" style="display:inline">
			         <select name="chooseColumn" id="chooseColumn" style="display: inline-block;margin-bottom: -1px;"> 
                        <#list dataTableColumns?sort_by("sequenceNumber") as column>
                        <option value="columnDiv_${column_index}">${column.displayName}</option>
                        </#list>
                    	</select>
                    </form>
                    </span>
<#--                    <ul class="dropdown-menu">
                        <#list dataTableColumns?sort_by("sequenceNumber") as column>
                        <li><a href="#" data-targetdiv="columnDiv_${column_index}">${column.displayName}</a></li>
                        </#list>
                    </ul> -->
                </li>
            </ul>
            <div id="fakeSubmitDiv" class="pull-right">
                <button type=button class="button btn btn-primary submitButton" id="fakeSubmitButton">Save</button>
                <img src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner"  style="display:none"/>
            </div>
        </div>
    </div>
</div>

</#if>


<@s.form method='post' id="edit-metadata-form" cssClass="form-horizontal"  action='save-column-metadata'>
<@s.hidden name='id' value='${resource.id?c}'/>
<@s.hidden name='dataTableId' value='${dataTable.id?c}'/>
<#if ( dataset.dataTables?size > 1 )>
<h2>Column Description and Mapping: ${dataTable.displayName}</h2>
<div class="">
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

</div>
</#if>



<#if dataTable.dataTableColumns??>
<div id="datatablecolumns">
<#list dataTableColumns?sort_by("sequenceNumber") as column>
    <#if column_index != 0><hr/></#if>

<div class="datatablecolumn" id="columnDiv_${column_index}" >
  <h3> 
  <span id="columnDiv_${column_index}lgnd" tooltipcontent="#generalToolTip" tiplabel="Column Mapping Instructions" class="columnSquare">&nbsp;</span>
  <!-- Column: -->
  <span class="displayName">${column.displayName}</span> 
  <!-- <small style="float:right">jump to: <a href="#top">top</a> | <a href="#submitButton">save</a></small> --></h3>

    <span tooltipcontent="#columnTypeToolTip" tiplabel="Column Type">
    <@s.radio id='columnEncoding_${column_index}' name='dataTableColumns[${column_index}].columnEncodingType' label="Column Type:"
    cssClass="columnEncoding" target="#columnDiv_${column_index}"
         listValue='label' emptyOption='false' list='%{allColumnEncodingTypes}'/>
    </span>
    <@s.hidden name="dataTableColumns[${column_index}].id" value="${column.id?c}" />
    <@s.hidden name="dataTableColumns[${column_index}].columnDataType" value="${column.columnDataType}" cssClass="dataType" />
    <@s.hidden name="dataTableColumns[${column_index}].name" value="${column.name}" />
    <@s.textfield name="dataTableColumns[${column_index}].displayName" value="${column.displayName}" label="Display Name:" tooltipcontent="#displayNameToolTip" tiplabel="Display Name" cssClass="input-xxlarge" />
    <div class="measurementInfo" style='display:none;'>
    <@s.select name='dataTableColumns[${column_index}].measurementUnit' cssClass="measurementUnit"
         label="Meas. Unit:" listValue='fullName' emptyOption='true' list='%{allMeasurementUnits}'/>
    </div>
    <div tooltipcontent="#categoryVariableToolTip" tiplabel="Category Variable" class="control-group">
        <label class="control-label">Category:</label>
        <#assign subCategoryId="" />
        <#assign categoryId="" />
        <#if column.categoryVariable??>
            <#assign subCategoryId="${column.categoryVariable.id?c}" />
            <#if column.categoryVariable.parent??>
                <#assign categoryId="${column.categoryVariable.parent.id?c}" />
            <#else>
                <#assign categoryId="${column.categoryVariable.id?c}" />
            </#if> 
        </#if> 
        <div class="controls">
            <@s.select id='categoryVariableId_${column_index}' 
                    name='dataTableColumns[${column_index}].categoryVariable.id' 
                    onchange='changeSubcategory("#categoryVariableId_${column_index}","#subcategoryId_${column_index}")'
                    headerKey="-1"
                    headerValue=""
                    cssClass="categorySelect span3"
                    listKey='id'
                    listValue='name'
                    list='%{allDomainCategories}'
                    label="Category:"
                    theme="simple"
                    autocompleteName="sortCategoryId"
                    value="${categoryId}"
                    />
            
            <span id="subcategoryDivId_${column_index}">
                <#if subCategoryId != "">
                <@s.select  target="#columnDiv_${column_index}"
                    id='subcategoryId_${column_index}'
                    cssClass="subcategorySelect span3" 
                    name='dataTableColumns[${column_index}].tempSubCategoryVariable.id'
                    list='%{subcategories[${column_index}]}'
                    theme="simple"
                    headerKey="-1"
                    headerValue=""
                    listKey='id'
                    listValue='name'
                    emptyOption='false'
                    label="Subcategory:"
                    autocompleteName="subCategoryId"
                    value="${subCategoryId}"
                />
                <#else>
                    <select id='subcategoryId_${column_index}' name='dataTableColumns[${column_index}].tempSubCategoryVariable.id'
                            class="span3"    autocompleteName="subCategoryId">
                        <option value='-1'>N/A</option>
                    </select>
                </#if>
            </span>
               <img src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="visibility:hidden"/>
        </div>
    </div>
    <span tooltipcontent="#descriptionToolTip" tiplabel="Column Description">
    <@s.textarea label="Column Description" name='dataTableColumns[${column_index}].description' rows='2' cols='12' cssClass="resizable input-xxlarge" />

    </span>
    <div id='divCodingSheet-${column_index}' class="codingInfo" tooltipcontent="#codingSheetToolTip" tiplabel="Coding Sheet">
            <#assign codingId="" />
            <#assign codingTxt="" />
            <#if column.defaultCodingSheet?? && column.defaultCodingSheet.id??>
                <#assign codingId=column.defaultCodingSheet.id?c />
                <#assign codingTxt="${column.defaultCodingSheet.title} (${column.defaultCodingSheet.id?c})"/>
            </#if>
            <@s.hidden id="${column_index}_cid" name="dataTableColumns[${column_index}].defaultCodingSheet.id" cssClass="codingsheetidfield" value="${codingId}" />
            <@edit.combobox name="dataTableColumns[${column_index}].defaultCodingSheet.title"  target="#columnDiv_${column_index}"
             label="Translate your data using a Coding Sheet:"
             autocompleteParentElement="#columnDiv_${column_index}"
             autocompleteIdElement="#${column_index}_cid"
             placeholder="Enter the name of a Coding Sheet"
             addNewLink="/coding-sheet/add?returnToResourceMappingId=${resource.id?c}"
            value="${codingTxt}" cssClass="input-xxlarge codingsheetfield" />
    </div>
     <div id='divOntology-${column_index}' class="ontologyInfo " tooltipcontent="#ontologyToolTip" tiplabel="Ontology">
            <#assign ontologyId="" />
            <#assign ontologyTxt="" />
            <#if column.defaultOntology??  && column.defaultOntology.id??>
                <#assign ontologyId=column.defaultOntology.id?c />
                <#assign ontologyTxt="${column.defaultOntology.title} (${column.defaultOntology.id?c})"/>
            </#if>
            <@s.hidden name="dataTableColumns[${column_index}].defaultOntology.id" value="${ontologyId}" id="${column_index}_oid" />
            <@edit.combobox name="dataTableColumns[${column_index}].defaultOntology.title" target="#columnDiv_${column_index}"
             value="${ontologyTxt}"  
             label="Map it to an Ontology:"
             placeholder="Enter the name of an Ontology"
             autocompleteParentElement="#columnDiv_${column_index}"
             autocompleteIdElement="#${column_index}_oid"
             addNewLink="/ontology/add?returnToResourceMappingId=${resource.id?c}"
             
             cssClass="input-xxlarge ontologyfield" />
    </div>
    <br/>
    <div class="mappingInfo" tooltipcontent="#mappingToolTip" tiplabel="Mapping ${siteAcronym} Resources">
    <#if column.dataTable?? && column.dataTable.dataset.project?? && column.dataTable.dataset.project.id != -1 >
        <#assign mapping = "false" />
        <#if column.mappingColumn??>
            <#assign mapping =column.mappingColumn /></#if>

    <@edit.boolfield name="dataTableColumns[${column_index}].mappingColumn"
        label="Use column values to map table rows to resources?"
        id="mapping_${column_index}" value=mapping cssClass="mappingValue" />
    <div class="mappingDetail well">
       <p>${siteAcronym} can associate groups of documents and images with this dataset as long as they're all part of the same project. 
       If this column has filenames in it, ${siteAcronym} will associate the filename with the filename of the image or document and load the row
       data as additional fields in the ${siteAcronym} record.
       </p>
        <@s.textfield name="dataTableColumns[${column_index}].delimiterValue" value="${column.delimiterValue!''}" placeholder="eg. ; , |" label="Delimiter" labelposition="left" maxLength="1"/>
        <br/>
        <#assign ignoreExt = "false" />
        <#if column.ignoreFileExtension??>
            <#assign ignoreExt = column.ignoreFileExtension /></#if>
        <@edit.boolfield 
          name="dataTableColumns[${column_index}].ignoreFileExtension"
          label="ignore file extension"
          id="dataTableColumns[${column_index}].ignoreFileExtension"
          value=ignoreExt
          />
        
        <br/>
<#--
        <#assign visible = "true" />
        <#if column.visible??>
            <#assign visible = column.visible /></#if>
        <@edit.boolfield 
          name="dataTableColumns[${column_index}].visible"
          label="visible?"
          id="dataTableColumns[${column_index}].visible"
          value=visible
          /> -->
          </div> 
    <#else>
    <i>cannot map this dataset to a set of ${siteAcronym} resources because the dataset is not in a project</i>
    </#if>
    </div>
</div>
</#list>

<div class="hidden" style="visibility:hidden;display:none">
    <span class="hidden" id="generalToolTip">
         Each "column" subform shown on the table metadata page represents a column in the dataset, and provides fields to describe the data in that column. This is important documentation for researchers that wish to use the dataset, and where relevant the form links to coding sheets and ${siteAcronym} ontologies to faciliate research. 
    </span>
    <span class="hidden" id="columnTypeToolTip">
        Select the option that best describes the data in this column. The form will display fields relevant to your selection. 
    </span>
    <span class="hidden" id="displayNameToolTip">
        If needed, edit the name displayed for the column to help users understand the column's contents.
    </span>
    <span class="hidden" id="categoryVariableToolTip"> 
        Select the category and subcategory that best describes the data in this column.
    </span>
    <span class="hidden" id="descriptionToolTip">
        Add any notes that would help a researcher understand the data in the column. 
    </span>
    <span class="hidden" id="codingSheetToolTip">
        If the data in this column is coded and the right coding sheet has been added to ${siteAcronym}, please select a coding sheet that translates and explains the codes. 
    </span>
    <span class="hidden" id="ontologyToolTip">
        If you would like to link this column to a ${siteAcronym} ontology, make that selection here. This is important if you (or other researchers) intend to integrate this dataset with other datasets using the ${siteAcronym} data integration tool. 
    </span>
    <span class="hidden" id="mappingToolTip">
        Use column values to map table rows to resources? - Select this check box if the data in the column links to a specific ${siteAcronym} resource, usually an image or document.
    </span>
</div>

</div>
</#if>
<h2>Summary</h2>
<table id="summaryTable" class="table tableFormat">
    <tr><th></th><th></th></tr>
    <tr><td><span class="columnSquare invalid">&nbsp;</span><span class="error_label"></span></td><td>columns with errors</td></tr>
    <tr><td><span class="columnSquare measurement">&nbsp;</span><span class="measurement_label"></span></td><td>measurment columns</td></tr>
    <tr><td><span class="columnSquare count">&nbsp;</span><span class="count_label"></span></td><td>count columns</td></tr>
    <tr><td><span class="columnSquare coded">&nbsp;</span><span class="coded_label"></span></td><td>coded columns</td></tr>
    <tr><td><span class="columnSquare uncoded">&nbsp;</span><span class="uncoded_label"></span></td><td>uncoded columns</td></tr>
    <tr><td><span class="columnSquare integration">&nbsp;</span><span class="integration_label"></span></td><td>integration columns</td></tr>
    <tr><td><span class="columnSquare mapped">&nbsp;</span><span class="mapped_label"></span></td><td>mapping columns</td></tr>
</table>

<@edit.submit "Save" false>
<p>
    <@s.radio name="postSaveAction" listValue="label" emptyOption="false" list="%{allSaveActions}" numColumns=1 
    cssClass="inline radio" theme="simple" />
    <br/>
    <br/>
</p>
</@edit.submit>

</@s.form>


<script type='text/javascript'>


var pageInitialized = false;
$(document).ready(function() {
    var $form = $(formId);

    if (!Modernizr.cssresize) {
        $('textarea.resizable:not(.processed)').TextAreaResizer();
    }

    // set up ajax calls, no caching
    $.ajaxSetup({ 
        cache: false 
    });
     
    applyWatermarks(document);
 
      
    $('#table_select').change(function() {
        window.location='?dataTableId='+$(this).val();
    });

    $form.delegate(":input", "blur change", registerCheckboxInfo);
    
    TDAR.contexthelp.initializeTooltipContent("#edit-metadata-form");
    
    console.debug('binding autocompletes');
    
    //bugfix: deferred registration didn't properly register expando button. If this is too slow,  but delegate inside of applyComboboxAutocomplete
    applyComboboxAutocomplete($('input.codingsheetfield', $form), "CODING_SHEET");
    applyComboboxAutocomplete($('input.ontologyfield', $form), "ONTOLOGY");
    
    console.debug('intitializing columns');
    //determine when to show coding-sheet, ontology selection based on column encoding value
    // almost all of the startup time is spent here
    $('input.ontologyfield').change(registerCheckboxInfo).change();
      pageInitialized = true;
      updateSummaryTable();
      // clear all hidden ontology/coding sheet hidden fields to avoid polluting the controller
      $form.submit(function() {
           $('input', $('.ontologyInfo:hidden')).val('');
           $('input', $('.codingInfo:hidden')).val('');
      });

    $("#fakeSubmitButton").click(function() {$("#submitButton").click();});      
    
    
    var $window = $(window);
    
    $("#chooseColumn").change(function(e) {
        gotoColumn($(this));
    });
    
    TDAR.common.initFormValidation($("#edit-metadata-form")[0]);
});



/* this function manages the display of the checkboxes next to a column field when someone changes one of the values, it changes
   the color if mapped properly to something */

function registerCheckboxInfo() {
    var $target = $($(this).parents(".datatablecolumn").first());
    var val = $('.columnEncoding:checked',$target).val();
    var square = $target.find("span.columnSquare");
    var mapping = $target.find("div.mappingInfo");
    var ontologyInfo = $target.find("div.ontologyInfo");
    var codingInfo = $target.find("div.codingInfo");
    var measurementInfo = $target.find("div.measurementInfo");
    
    if (val == 'CODED_VALUE' || val == 'UNCODED_VALUE') {
      mapping.show();
    } else {
      mapping.hide();
    }

    if (val == 'COUNT' || val == 'MEASUREMENT' || val == 'MAPPED_VALUE') {
      ontologyInfo.hide();
    } else {
      ontologyInfo.show();
    }

    if (val != 'CODED_VALUE') {
      codingInfo.hide();
    } else {
      codingInfo.show();
      ontologyInfo.hide();
    }        

    if (val == "MEASUREMENT") {
       measurementInfo.show();
    } else {
       measurementInfo.hide();
    }

    square.removeClass();

    square.addClass("columnSquare");

    
    var ontolog = $target.find("input.ontologyfield:visible").first().val();
    var dataType = $target.find("input.dataType").first().val();
    var codig = $target.find("input.codingsheetfield:visible").first();
    var codingSheetId = $('.codingsheetidfield', $target).val();
    var unit = $target.find("select.measurementUnit:visible").first();
    var map = $target.find(':input.mappingValue:visible:checked').first();
    var mapDetail = $target.find('.mappingDetail').first();
    mapDetail.hide();

    if (dataType == undefined || dataType.indexOf('INT') == -1 && dataType.indexOf('DOUBLE') == -1) {
      $(".columnEncoding[value='MEASUREMENT']", $target).attr('disabled','disabled');
      $(".columnEncoding[value='COUNT']", $target).attr('disabled','disabled');
    }
    
    if (val == 'COUNT') {
       square.addClass("count");
    } else if (val == 'MEASUREMENT')  {
        if (unit != undefined && unit.val() != '') {
          square.addClass("measurement");
        } else {
          square.addClass("invalid");
        } 
    } else if (ontolog != undefined && ontolog != '') {
      square.addClass("integration");
    } else if (val == 'CODED_VALUE') {
    console.log(codingSheetId);
        if (codingSheetId != undefined && codingSheetId != '') {
          square.addClass("coded");
        } else {
          square.addClass("invalid");
        }
    } else if (val == 'UNCODED_VALUE') {
        square.addClass("uncoded");
    }

    if (map != undefined && map.val() == "true") {
        square.addClass("mapped");
        mapDetail.show();
    }
    
    
    var subcat = $target.find(".categorySelect").first();
    var txt = $target.find("textarea.resizable").first();
    if (subcat != undefined && subcat.val() > 0 && txt != undefined && txt.val().length > 0) {
        square.addClass("complete");
    }

    if (pageInitialized) {
        // ... then it's safe to update the summary table (otherwise, the summary table would be updated n times
        // as the page loads)
        updateSummaryTable();
    }
}
    var formId = "#edit-metadata-form";


function gotoColumn($el) {
    var idVal = $el.val();
    document.getElementById(idVal).scrollIntoView();
}


function updateSummaryTable() {
    $("#summaryTable .integration_label").html($("div.datatablecolumn .columnSquare.integration").length);
    $("#summaryTable .coded_label").html($("div.datatablecolumn .columnSquare.coded").length);
    $("#summaryTable .uncoded_label").html($("div.datatablecolumn .columnSquare.uncoded").length);
    $("#summaryTable .error_label").html($("div.datatablecolumn .columnSquare.invalid").length);
    $("#summaryTable .count_label").html($("div.datatablecolumn .columnSquare.count").length);
    $("#summaryTable .mapped_label").html($("div.datatablecolumn .columnSquare.mapped").length);
    $("#summaryTable .measurement_label").html($("div.datatablecolumn .columnSquare.measurement").length);
}


</script>
</body>
</#escape>

