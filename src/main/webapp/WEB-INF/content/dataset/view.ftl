<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>


<#macro toolbarAdditions>
    <#if editable>
    <#assign disabled = resource.dataTables?size==0 />
    <@view.makeLink "dataset" "columns" "table metadata" "columns" current true disabled "hidden-tablet hidden-phone"/>
    <@view.makeLink "dataset" "columns" "metadata" "columns" current true disabled "hidden-desktop"/>
    </#if>
</#macro>


<#macro afterBasicInfo>
<#if (dataset.dataTables?has_content)>

<#if authenticatedUser??  && ((resource.latestUploadedVersion?? && resource.latestUploadedVersion.informationResourceFile.public) || allowedToViewConfidentialFiles)>
<h3>Browse the Data Set</h3>

<#if (dataset.dataTables?size > 1)>
<form>
    <label for="table_select">Choose Table:</label>
    <select id="table_select" name="dataTableId" onChange="window.location =  '?dataTableId=' + $(this).val()">
    <#list dataset.dataTables as dataTable_>
      <option value="${dataTable_.id?c}" <#if dataTable_.id == dataTable.id>selected </#if>
      >${dataTable_.displayName}</option>
    </#list>
    </select>
</form>
</#if>

<p><@view.embargoCheck /></p>

<div class="row">
    <div class="span9">
        <table id="dataTable" class="table tableFormat table-striped table-bordered" ></table>
    </div>
</div>


</#if>

<h3>Data Set Structure</h3>
<div class="row">
    <div class="span3"><span class="columnSquare measurement"></span> Measurement Column</div>
    <div class="span3"><span class="columnSquare count"></span>Count Column</div>
    <div class="span3"><span class="columnSquare coded"></span>Coded Column</div>
</div>
<div class="row">
    <div class="span3"><span class="columnSquare mapped"></span>Mapping Column</div>
    <div class="span6"><span class="columnSquare integration"></span>Integration Column (has Ontology)</div>
</div>

<#list dataset.dataTables as dataTable>
 <h4>Table Information: <span>${dataTable.displayName}</span></h4>
     <table class="tableFormat table table-striped table-bordered">
        <thead class='highlight'>
         <tr>
         <th class="guide">Column Name</th>
         <th>Data Type</th>
         <th>Type</th>
         <th>Category</th>
         <th>Coding Sheet</th>
         <th>Ontology</th>
         </tr>
         </thead>
            <#list dataTable.dataTableColumns?sort_by("sequenceNumber") as column>
            <tr>
                <#assign typeLabel = ""/>
                <#if column.measurementUnit?has_content><#assign typeLabel = "measurement"/></#if>
                <#if column.defaultCodingSheet?has_content><#assign typeLabel = "coded"/></#if>
                <#if column.defaultOntology?has_content><#assign typeLabel = "integration"/></#if>
                <#if column.columnEncodingType?has_content && column.columnEncodingType == 'COUNT'><#assign typeLabel = "count"/></#if>
                <#if column.mappingColumn?has_content && column.mappingColumn ><#assign typeLabel = "mapped"/></#if>
                <td class="guide" nowrap><span class="columnSquare ${typeLabel}"></span><b>
                    ${column.displayName}
                </b> </td>
                 <td><#if column.columnDataType??>${column.columnDataType.label}&nbsp;</#if></td>
                <td><#if column.columnEncodingType??>${column.columnEncodingType.label}</#if>
                <#if column.measurementUnit??> (${column.measurementUnit.label})</#if> </td>
                <td>
                <#if column.categoryVariable??>
                <#if column.categoryVariable.parent??>
                ${column.categoryVariable.parent} :</#if> ${column.categoryVariable}
                <#else>uncategorized</#if> </td>
                <td>
                    <#if column.defaultCodingSheet??>
                    <a href="<@s.url value="/coding-sheet/${column.defaultCodingSheet.id?c}" />">
                    ${column.defaultCodingSheet.title!"no title"}</a>
                    <#else>none</#if>
                </td><td>
                <#if column.defaultOntology?? >
                <a href="<@s.url value="/ontology/${column.defaultOntology.id?c}"/>">
                    ${column.defaultOntology.title!"no title"}</a>
                <#else>none</#if>
                </td>
            </tr>
            </#list>
         </table>
 
</#list>
</#if>
</#macro>

<#macro localJavascript>
	<#if (dataset.dataTables?size > 1)>
    jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages =3;
        $.extend( $.fn.dataTableExt.oStdClasses, {
        "sWrapper": "dataTables_wrapper form-inline"
    } );
    
//        sDom:'<"datatabletop"ilrp>t<>', //omit the search box
    var options = { 
        "sAjaxDataProp":"results.results",
          "sDom": "<'row'<'span6'l><'span3'>r>t<'row'<'span4'i><'span5'p>>",
        "bProcessing": true,
        "bServerSide":true,
        "bScrollInfinite": false,
        "bScrollCollapse": true,
        tableSelector: '#dataTable',
        sPaginationType:"bootstrap",
        sScrollX: "100%",  
        //turn off vertical scrolling since we're paging (feels weird to advance through records using two mechanisms)
        "sScrollY": "",
        "aoColumns":[
        
                 <#list dataTable.dataTableColumns?sort_by("sequenceNumber") as column>
                    <#if column.visible?? && column.visible>
                    { "bSortable": false,
                       "sName" : "${column.jsSimpleName?js_string}", 
                       "sTitle" : "${column.displayName?js_string}",
                       "fnRender": function(obj){
                           var val = obj.aData[${column_index?c}];
                           var str = htmlEncode(val);
                           return str;
                       }  }<#if column_has_next >,</#if>
                     </#if>
                 </#list>
           ],
           "sAjaxSource": "<@s.url value="/datatable/browse?id=${dataTable.id?c}" />"
    };
    registerLookupDataTable(options);    
</#if>
</#macro>
</#escape>
