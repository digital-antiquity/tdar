<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<head>
 <style type="text/css">

.drg {
//z-index:50000 !important;
}
 .showhide {
	 display: inline;
	 font-size: 80%;
	 right: 0px;
 }
 
 .collapse {
 	overflow:visible !important;
 }
 
.fixed {
    position: fixed;
    top: 0;
    z-index:1000;
    border: 1px solid #AAA;
    background-color: #DEDEDE;
    padding: 4px;
    margin:0px;
    opacity:.95;
}
.buttontable .integrationTableNumber {
    display:none;
    visibility:hidden;
}

.integrationColumn div[table] .ontology {
 display:none !important;
}

.status {
  color:#660033;
  font-weight:bold;
}

#drplist {border:1px solid #ccc;}

#submitbutton {
    right: -540px !important;
    position: relative !important;
}

.addAnother { margin-left:1em !important; font-weight:bold;}

.addAnother img {
    bottom: 2px !important;
    position: relative !important;
} 
</style>

</head>
 
<body>
    <@s.form name='selectDTColForm' method='post' action='filter' id="selectDTColForm">

<h3>Data Integration</h3>
<div class="glide">
Drag columns from your selected data tables onto the integration table .
</div>
<div class="glide">
<h3>Create your Integration Table:</h3>

<#macro setupIntegrationColumn column idx=0 init=false>
<td colNum="${idx}" class="<#if column.displayColumn><#if !init>displayColumn<#else>defaultColumn</#if><#else>integrationColumn</#if>">
  <div class="label">Column ${idx + 1} <span class="colType"></span>
  <input type="hidden" name="integrationColumns[${idx}].columnType" value="<#if column.displayColumn>DISPLAY<#else>INTEGRATION</#if>" class="colTypeField"/>
  <input type="hidden" name="integrationColumns[${idx}].sequenceNumber" value="${idx}" class="sequenceNumber" />
</div>
<#if column.columns.empty><span class="info">Drag variables from below into this column to setup your integration<br/><br/><br/><br/></span></#if>
<#list column.columns as col>
  <input type="hidden" name="integrationColumns[${idx}].columns[${col_index}].id" value="${col.id?c}" />
  
</#list>
</td>

</#macro>

<div id='fixedList' class="affix-top no-indent span12 row navbar-static"  data-offset-top="250" data-offset-bottom="250" data-spy="affix">
<h4>Each Column Below will be a Column In Excel</h4>
<table width="100%">
    <tr>
        <td>
            <label for="autoselect">
            <input type="checkbox" id="autoselect" class="checkbox inline"/>
            Auto-select integratable columns
            </label>
        </td>
        <td>
            <label for="clear">
            <input type="checkbox" id="clear"  class="checkbox inline" /> 
            Clear all columns</label>
        </td>
    </tr>
</table>
<table id="drplist" width="100%">
<tr>
<#if integrationColumns?? && !integrationColumns.empty >
 <#list integrationColumns as integrationColumn>
   <@setupIntegrationColumn integrationColumn integrationColumn_index />
 </#list>
<#else>
  <@setupIntegrationColumn blankIntegrationColumn 0 true/>
</#if>

</tr>
</table>
  <div class="status"></div>
<button class="addAnother btn" id="addColumn"><i class="icon-plus-sign"></i> Add a new Column</button>
<@s.submit value='Next: filter values' id="submitbutton" cssClass="submitbutton submitButton btn button btn-primary" />

</div>
</div>
<div class="glide">
<h2>Select Variables</h2>
<table width="100%" class="legend">
<tr>
<td class="legend displayColumn">&nbsp;</td> <td><b>Display Variable</b></td>
<td class="legend integrationColumn">&nbsp;&nbsp;</td> <td><b>Integration Variable with mapped Ontology</b></td>
<td class="legend measurementColumn">&nbsp;&nbsp;</td> <td><b>Measurement Variable</b></td>
<td class="legend countColumn">&nbsp;&nbsp;</td> <td><b>Count Variable</b></td>
</tr>
</table>
<br/>

<div class="accordion" id="accordion">
      <#assign numCols = 6 />

     <div class="accordion-group">

      <#list selectedDataTables as table>
      <!-- setting for error condition -->
       <input type="hidden" name="tableIds[${table_index}]" value="${table.id?c}"/>

	       <div class="accordion-heading">
			   <h4>${table_index  +1}: ${table.dataset.title}
	         <a class="showhide accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapse${table_index}">(show/hide)</a> </h4>
	       </div>
	       <div id="collapse${table_index}" class="accordion-body collapse in">
	         <div class="accordion-inner">
			     <table class="buttontable">
			         <tbody>
			              <#if leftJoinDataIntegrationFeatureEnabled>
			                 <#assign columns = table.leftJoinColumns>
			             <#else>
			                 <#assign columns = table.sortedDataTableColumns>
			             </#if>
			           <#assign count = 0>
			               <#list columns as column>
			               <#assign description = ""/>
			               <#if column?? && column.description??>
			                   <#assign description = column.description />
			               </#if>
			                 <#if count % numCols == 0><tr></#if>
			                 <td width="${(100 / numCols)?floor }%"><div class="drg ui-corner-all" <#if column.defaultOntology??>hasOntology="${column.defaultOntology.id?c}"</#if>
			                 <#if column.measurementUnit??>hasMeasurement="${column.measurementUnit}"</#if> 
			                 title="${description?html}"
			                 <#if column.columnEncodingType?? && column.columnEncodingType=='COUNT'>hasCount="true"</#if> 
			                 table="${table.id?c}"><span class="columnName"><span class="integrationTableNumber">T${table_index +1}. </span>
							<span class="name">${column.displayName}</span>
		    			     <#if column.defaultOntology??> <span class="ontology">- ${column.defaultOntology.title}</span></#if>
			               <input type="hidden" name="integrationColumns[{COLNUM}].columns[{CELLNUM}].id"  value="${column.id?c}"/></span>
			                   <#assign count = count+1 />
			                </div> </td>
			                 <#if count % numCols == 0></tr></#if>
			               </#list>
			                 <#if count % numCols != 0></tr></#if>

			         </tbody>
			         </table>

	         </div>
	       </div>
	     </div>

      </#list>
  </div>
	  <div >

<br/><br/>
<@s.submit value='Next: filter values' cssClass="submitbutton btn btn-primary submitButton" />


</div>
</@s.form>
<form name="autosave" style="display:none;visibility:hidden">
<textarea  id="autosave"></textarea>
</form>

<script>

jQuery(document).ready(function($){
    TDAR.integration.initDataIntegration();
});
</script>

</body>
</#escape>