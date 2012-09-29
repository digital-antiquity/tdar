<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<title>Edit Table Metadata for ${dataset.title}</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@edit.toolbar "dataset" "columns" />

<@s.form method='post' id="edit-metadata-form" action='save-column-metadata'>
<@s.hidden name='id' value='${resource.id?c}'/>
<@s.hidden name='dataTableId' value='${dataTable.id?c}'/>
    <#if ( dataset.dataTables?size > 1 )>
    
<div class='glide'>
    <h2>Column Description and Mapping: ${dataTable.displayName}</h2>
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

<div class="glide">
<#if dataTable.dataTableColumns??>
<style>
.legend {
    width: auto;
    clear: none;
    display: inline-block;
    margin: 2px;
}
</style>
<h3>The ${dataTable.displayName} table has ${dataTable.dataTableColumns?size } columns</h3>
<b>Click on a column name below to edit the column's metadata.</b><br/>

<select name="chooseColumn" onChange="goToColumn(this)">
<#list dataTableColumns?sort_by("sequenceNumber") as column>
 <option value="columnDiv_${column_index}">${column.displayName}</option>
</#list>
</select>
</#if> 
</div>

<#if dataTable.dataTableColumns??>
<div id="datatablecolumns">
<#list dataTableColumns?sort_by("sequenceNumber") as column>
<div class="glide datatablecolumn" id="columnDiv_${column_index}" >
  <h3><span id="columnDiv_${column_index}lgnd" tooltipcontent="#generalToolTip" tiplabel="Column Mapping Instructions" class="columnSquare">&nbsp;</span>Column: <span class="displayName">${column.displayName}</span> <small style="float:right">jump to: <a href="#top">top</a> | <a href="#submitButton">save</a></small></h3>

    <span tooltipcontent="#columnTypeToolTip" tiplabel="Column Type">
    <@s.radio id='columnEncoding_${column_index}' name='dataTableColumns[${column_index}].columnEncodingType' label="Column Type:"
    cssClass="columnEncoding" target="#columnDiv_${column_index}"
         listValue='label' emptyOption='false' list='%{allColumnEncodingTypes}'/>
    </span>
<br/>
    <@s.hidden name="dataTableColumns[${column_index}].id" value="${column.id?c}" /> <br/>
    <@s.hidden name="dataTableColumns[${column_index}].columnDataType" value="${column.columnDataType}" cssClass="dataType" />
    <@s.hidden name="dataTableColumns[${column_index}].name" value="${column.name}" />
    <@s.textfield name="dataTableColumns[${column_index}].displayName" value="${column.displayName}" label="Display Name:" 
    tooltipcontent="#displayNameToolTip" tiplabel="Display Name"/>
    <div class="measurementInfo" style='display:none;'>
    <@s.select name='dataTableColumns[${column_index}].measurementUnit' cssClass="measurementUnit"
         label="Meas. Unit:" listValue='fullName' emptyOption='true' list='%{allMeasurementUnits}'/>
    </div>
    <br>
    <div tooltipcontent="#categoryVariableToolTip" tiplabel="Category Variable">
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
        <@s.select id='categoryVariableId_${column_index}' 
                name='dataTableColumns[${column_index}].categoryVariable.id' 
                onchange='changeSubcategory("#categoryVariableId_${column_index}","#subcategoryId_${column_index}")'
                headerKey="-1"
                headerValue=""
                cssClass="categorySelect"
                listKey='id'
                listValue='name'
                list='%{allDomainCategories}'
                label="Category:"
                autocompleteName="sortCategoryId"
                value="${categoryId}"
                />
        
        <span id="subcategoryDivId_${column_index}">
            <#if subCategoryId != "">
            <@s.select  target="#columnDiv_${column_index}"
                id='subcategoryId_${column_index}'
                cssClass="subcategorySelect" 
                name='dataTableColumns[${column_index}].tempSubCategoryVariable.id'
                list='%{subcategories[${column_index}]}'
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
                            autocompleteName="subCategoryId">
                    <option value='-1'>N/A</option>
                </select>
            </#if>
        </span>
           <img src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="visibility:hidden"/>
    </div>
    <br/>
    <span tooltipcontent="#descriptionToolTip" tiplabel="Column Description">
    <b>Please describe the data collected in this column</b><br/>
    <@s.textarea name='dataTableColumns[${column_index}].description' rows='2' cols='12' cssClass="resizable" />

    </span>
    <br/>    
    <div id='divCodingSheet-${column_index}' class="codingInfo" tooltipcontent="#codingSheetToolTip" tiplabel="Coding Sheet">
        <br />
        <b>Translate your data using a Coding Sheet:</b>
            <#assign codingId="" />
            <#assign codingTxt="" />
            <#if column.defaultCodingSheet?? && column.defaultCodingSheet.id??>
                <#assign codingId=column.defaultCodingSheet.id?c />
                <#assign codingTxt="${column.defaultCodingSheet.title} (${column.defaultCodingSheet.id?c})"/>
            </#if>
            <@s.hidden id="${column_index}_cid" name="dataTableColumns[${column_index}].defaultCodingSheet.id" cssClass="codingsheetidfield" value="${codingId}" />
            <@s.textfield name="dataTableColumns[${column_index}].defaultCodingSheet.title"  target="#columnDiv_${column_index}"
             autocompleteParentElement="#columnDiv_${column_index}"
             autocompleteIdElement="#${column_index}_cid"
             watermark="Enter the name of a Coding Sheet"
            value="${codingTxt}" cssClass="longfield codingsheetfield" />
            <div class="down-arrow"></div>
            <small><a target="_blank" onclick="setAdhocTarget(this);" href='<@s.url value="/coding-sheet/add?returnToResourceMappingId=${resource.id?c}"/>'>Create Coding Sheet</a> </small>
            <br/>
    </div>
     <div id='divOntology-${column_index}' class="ontologyInfo" tooltipcontent="#ontologyToolTip" tiplabel="Ontology">
        <b>Map it to an Ontology:</b><br/>
            <#assign ontologyId="" />
            <#assign ontologyTxt="" />
            <#if column.defaultOntology??  && column.defaultOntology.id??>
                <#assign ontologyId=column.defaultOntology.id?c />
                <#assign ontologyTxt="${column.defaultOntology.title} (${column.defaultOntology.id?c})"/>
            </#if>
            <@s.hidden name="dataTableColumns[${column_index}].defaultOntology.id" value="${ontologyId}" id="${column_index}_oid" />
            <@s.textfield name="dataTableColumns[${column_index}].defaultOntology.title" target="#columnDiv_${column_index}"
             value="${ontologyTxt}"  
             watermark="Enter the name of an Ontology"
             autocompleteParentElement="#columnDiv_${column_index}"
             autocompleteIdElement="#${column_index}_oid"
             cssClass="longfield ontologyfield" />
            <div class="down-arrow"></div>
            <small><a target="_blank" onclick="setAdhocTarget(this);" href='<@s.url value="/ontology/add?returnToResourceMappingId=${resource.id?c}"/>'>Create Ontology</a> </small>
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
    <fieldgroup class="mappingDetail">
       <legend>${siteAcronym} can associate groups of documents and images with this dataset as long as they're all part of the same project. 
       If this column has filenames in it, ${siteAcronym} will associate the filename with the filename of the image or document and load the row
       data as additional fields in the ${siteAcronym} record.
       </legend>
        <@s.textfield name="dataTableColumns[${column_index}].delimiterValue" value="${column.delimiterValue!''}" label="Delimiter" labelposition="left" maxLength="1"/>
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
          </fieldgroup> 
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

    <@edit.submit "Save" false><br/>
    <table id="summaryTable">
        <tr><th></th><th>Summary</th></tr>
        <tr><td><span class="columnSquare invalid">&nbsp;</span><span class="error_label"></span></td><td>columns with errors</td></tr>
        <tr><td><span class="columnSquare measurement">&nbsp;</span><span class="measurement_label"></span></td><td>measurment columns</td></tr>
        <tr><td><span class="columnSquare count">&nbsp;</span><span class="count_label"></span></td><td>count columns</td></tr>
        <tr><td><span class="columnSquare coded">&nbsp;</span><span class="coded_label"></span></td><td>coded columns</td></tr>
        <tr><td><span class="columnSquare uncoded">&nbsp;</span><span class="uncoded_label"></span></td><td>uncoded columns</td></tr>
        <tr><td><span class="columnSquare integration">&nbsp;</span><span class="integration_label"></span></td><td>integration columns</td></tr>
        <tr><td><span class="columnSquare mapped">&nbsp;</span><span class="mapped_label"></span></td><td>mapping columns</td></tr>
        </table>
        <@s.radio name="postSaveAction" listValue="label" emptyOption="false" list="%{allSaveActions}" numColumns=1 />
    </@edit.submit>
</@s.form>
<@edit.sidebar>
</@edit.sidebar>


<script type='text/javascript'>


var pageInitialized = false;
$(document).ready(function() {
    $.validator.addMethod("columnEncoding", function(value, element) {
        //when using this method on radio buttons, validate only calls this function once per radio group (using the first element of the group for value, element)
        //However, we still need to put put the error message in the title attr of the first element in group. Feels hokey but there you are.
        console.log('validating:' + element.id + "\t value:" + value);
        var displayName = "'" + $(element).closest('div').find('h3 > .displayName').text() + "'";
        var $selectedElement = $(element).parent().find(':checked');
    
        //if we came here by way of a form 're-validate', we need to make sure that validation logic in registerCheckboxInfo happens first.
        registerCheckboxInfo.apply(element);
        
        if($selectedElement.is(':disabled')) {
            var val = $selectedElement.val().toLowerCase().replace('_', ' ')
            element.title  = "The selection '" + val + "' is no longer valid for column " + displayName;
            return false;
        }
        
        var $target = $(element).closest(".datatablecolumn");
        var square = $target.find(".columnSquare.invalid");
        if (square == undefined || square.length == 0 ) {
            return true;
        } else {
            element.title = "Column " + displayName + " contains errors or invalid selections";   
            return false;
        }
    });


    if ($.browser.msie || $.browser.mozilla && getBrowserMajorVersion() < 4 ) {
        $('textarea.resizable:not(.processed)').TextAreaResizer();
    }

    // set up ajax calls, no caching
    $.ajaxSetup({ 
        cache: false 
    });
     
     $("#edit-metadata-form").FormNavigate("Leaving the page will cause any unsaved data to be lost!");
     $("#table_select").unbind("change");
       applyWatermarks(document);
      setupFormValidate("#edit-metadata-form");
      
          $('#table_select').change(function() {
        window.location='?dataTableId='+$(this).val();
    });

    $(formId).delegate(":input", "blur change", registerCheckboxInfo);
    $(formId).delegate(".down-arrow", "click",autocompleteShowAll);
    initializeTooltipContent();
    
    console.debug('binding autocompletes');
    $(formId).delegate('input.codingsheetfield',"focusin", function() {
        applyResourceAutocomplete($('input.codingsheetfield'), "CODING_SHEET");
    });
    $(formId).delegate('input.ontologyfield',"focusin", function() {
        applyResourceAutocomplete($('input.ontologyfield'), "ONTOLOGY");
    });
    console.debug('intitializing columns');
    //determine when to show coding-sheet, ontology selection based on column encoding value
    // almost all of the startup time is spent here
    $('input.ontologyfield').change(registerCheckboxInfo).change();
      pageInitialized = true;
      updateSummaryTable();
      // clear all hidden ontology/coding sheet hidden fields to avoid polluting the controller
      $(formId).submit(function() {
           $('input', $('.ontologyInfo:hidden')).val('');
           $('input', $('.codingInfo:hidden')).val('');
      });
      
});



/* this function manages the display of the checkboxes next to a column field when someone changes one of the values, it changes
   the color if mapped properly to something */
//FIXME: validation logic should be in validate methods
//FIXME: confirm these calls to .first() are pointless (THEY ARE NOT), then remove

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

    $("#error").html('');
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
    var mapDetail = $target.find('fieldgroup.mappingDetail').first();
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


function goToColumn(select) {
    var idVal = $(select).val();
    $(select).attr('selectedIndex', 0);
    $("option",$(select))[0].selected=true;
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