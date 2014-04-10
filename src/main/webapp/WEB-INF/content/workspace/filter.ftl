<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<head>
<title>Filter Ontology Values</title>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<style type='text/css'>
//input+label {position: relative; }
input[disabled] + label {
font-weight: normal !important;
}
.inline-label {clear:none;display:inline-block; }
.wf-loading {
    visibility: visible !important;
}
</style>
</head>
<body>
<h1>Filter Ontology Values</h1>
<p>You can filter data values for the datasets listed below.  Only checked values mapped to an
ontology will be reported below.  Select checkboxes next to the
values that you would like to be included or aggregated to that level.  Checkboxes are automatically checked if values are present in ALL datatables.
</p>
<p>
    Indented unchecked values are aggregated to the next higher level that is checked.
Unchecked values at the top (leftmost) level are ignored, along with  any unchecked
subdivision categories.  Values that occur in each dataset are indicated with blue
checks, absent values are indicated with red x's.
</p>
<#assign integrationcolumn_index =0>

<#-- display links with taxonomy expanded -->
<p class="text-right"><button id="btnDisplaySelections" type="button" class="btn btn-mini">Copy/Paste Codes</button></p>
<@s.form method='post' action='display-filtered-results' id="filterForm">

<#assign totalCheckboxCount=0>
<#list integrationColumns as integrationColumn>
 <#if integrationColumn.displayColumn >

  <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columnType" value="${integrationColumn.columnType}" />
  <#list integrationColumn.columns as col_ >
   <#if col_??>
    <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columns[${col__index}].id" value="${col_.id?c}" />
   </#if>
  </#list>
 <#else>
 <#if integrationColumn.sharedOntology??>
 <table class='tableFormat table table-striped integrationTable'>
    <thead>
        <tr>
        <th>Ontology labels from ${integrationColumn.sharedOntology.title} [${integrationColumn.name}]<br/>

        (<span class="link" onclick='TDAR.integration.selectAllChildren("onCbId_${integrationColumn.sharedOntology.id?c}_", true);'>Select All</span> | <span class="autocheck link">Select All With Shared Values</span>
        | <span class="link"onclick='TDAR.integration.selectAllChildren("onCbId_${integrationColumn.sharedOntology.id?c}_", false);'>Clear All</span> | <span class="link hideElements">Hide Unmapped</span>)</th>
        <#list integrationColumn.columns as column>
        <th>${column.name}<br/> <small>(${column.dataTable.dataset.title})</small></th>
        </#list>
        </tr>
    </thead>
    <tbody>
  <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columnType" value="${integrationColumn.columnType!"integration"}" />
  <#list integrationColumn.columns as col>
    <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columns[${col_index}].id" value="${col.id?c}" />
  </#list>

<#list integrationColumn.flattenedOntologyNodeList as ontologyNode>
    <#assign numberOfParents=ontologyNode.numberOfParents>
    <#assign checkForUser=true />
    <#assign disabled=true />
    <#if ontologyNode.parent><#assign disabled=false /></#if>
    <#list ontologyNode.columnHasValueArray as hasValue>
        <#if !hasValue>
            <#assign checkForUser=false />
        <#else>
            <#assign disabled=false />
        </#if>
    </#list>
    <#assign node_id="onCbId_${integrationColumn.sharedOntology.id?c}_${ontologyNode.index?replace('.', '_')}_${ontologyNode.id?c}" />
    <tr class="<#if disabled>disabled</#if>">
    <td style="white-space: nowrap;">
    <label class="inline-label nodeLabel" for='${node_id}'>
    <#list 1..numberOfParents as indentationLevel>
        &nbsp;&nbsp;&nbsp;&nbsp;
    </#list>
     <input type='checkbox' id='${node_id}'
    name='integrationColumns[${integrationcolumn_index}].filteredOntologyNodes[${ontologyNode_index}].id' value='${ontologyNode.id?c}'
    <#if checkForUser>canautocheck="true"</#if>     <#if disabled>disabled="disabled"</#if> />
    <#assign totalCheckboxCount=totalCheckboxCount+1>
        <#if !disabled><b></#if>
            <span class="nodeName">${ontologyNode.displayName}</span> <!--(${ontologyNode.index})-->
        <#if !disabled></b></#if>
    </label>
    <#if ontologyNode.parent ><span class="right">
    &nbsp;(<span class="link" onclick='selectChildren("${node_id}", true);'>all</span>
    | <span class="link" onclick='selectChildren("${node_id}", false);'>clear</span>)</span>
    </#if>
    
    </td>
    <#list ontologyNode.columnHasValueArray as hasValue>
    <td>
        <#if hasValue>
    	    <img src="<@s.url value="/images/checked.gif" />"/>
        <#else>
    	    <img src="<@s.url value="/images/unchecked.gif" />"/>
        </#if>
    </td>
    </#list>
</tr>
</#list>
</tbody>
</table>
 
 <#else>
    These columns do not share a common ontology but ontology integration has not been
    fully implemented yet.  
 </#if>
</#if>
<#assign integrationcolumn_index = integrationcolumn_index+1>
 
 </#list>

<@edit.submit "Next: Apply filter" false/>

  <#list selectedDataTables as table>
      <!-- setting for error condition -->
       <input type="hidden" name="tableIds[${table_index}]" value="${table.id?c}"/>
  </#list>

</@s.form>


<script type='text/javascript'>
    $(function(){
        TDAR.integration.initOntologyFilterPage();
    })
</script>
<div id="divModalStore" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="divModalStoreLabel" aria-hidden="true">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
    <h3 id="divModalStoreLabel">Ontology Filter Codes</h3>
      <span>The codes in the textbox below represent your current ontology selections. </span>
      <span>To restore ontology filter selections from a previous integration, paste those selection codes into the textbox. </span>
  </div>
  <div class="modal-body">
    <textarea id="txtStr2cb" cols=300 rows=5 style="width:100%; font-family:monospace; font-size:smaller; line-height: normal" spellcheck="false"></textarea>
  </div>
  <div class="modal-footer">
    <button  class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
    <button id="btnStr2cb" class="btn btn-primary">Load my previous selections</button>
  </div>
</div>
</body>
</#escape>