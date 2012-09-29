<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<head>
<title>Data Integration: Filtered results</title>
<meta name="lastModifiedDate" content="$Date$"/>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
</head>

<script type='text/javascript'>
$(document).ready(function() {
  applyZebraColors();
  });
</script>

<div class="glide">
<p>
The integrated data results are displayed below.<br/>
<b><a href='<@s.url value="download?ticketId=${ticketId?c}" />'>DOWNLOAD all results as an Excel file</a></b>.
</p>
</div>

<div class="glide">

<h2>Summary of Integration Results (counts of integration column values)</h2>

<table  class="tableFormat width99percent zebracolors">
<thead>
 <tr>
  <#list integrationColumns as column>
    <th>
  	<#if column.integrationColumn>
	    ${column.name}
    </#if>
    </th>
  </#list>
  <#list selectedDataTables as table>
    <th>${table.displayName}</th>
  </#list>
  <th>Total</th>
  </tr>
</thead>
<tbody>
 <#assign keys = pivotData?keys >
  <#list keys?sort as key >
  <tr>
   <#list key as node >
        <td><#if node??>
           ${node.displayName!''}</#if></td>
       </#list>
       <#assign totals = 0/>
       <#list selectedDataTables as table>
        <td>  <#if pivotData.get(key)?? && pivotData.get(key).get(table)??>
            ${pivotData.get(key).get(table)}
            <#assign totals = totals + pivotData.get(key).get(table) />
             <#else>0</#if></td>
  </#list>
  <td>${totals}</td>
  </tr>
  </#list>
</tbody>
</table>
</div>

<@s.iterator value='integrationDataResults' var='integrationDataResult'>
<div class="glide">

<h2>Integration results for ${integrationDataResult.dataTable.displayName} (from
${integrationDataResult.dataTable.dataset.title})</h2>
<table class="tableFormat width99percent zebracolors">
<thead>
<tr>
<th>Table</th>

<#list integrationDataResult.integrationColumns as integrationColumn>
    <th>${integrationColumn.name}</th>
    <#if !integrationColumn.displayColumn>
    <th>Mapped ontology value for ${integrationColumn.name}</th>
    </#if>
</#list>


</tr>
</thead>
<tbody>

<#assign count=0>
<#list integrationDataResult.rowData as row>
    <#if (row_index < 100) >
    <tr>

    <#list row as col>
    <td>
    	<#if col?has_content>${col}<#else>
	    	<#noescape>&nbsp;</#noescape>
    	</#if>
    </td>
    </#list>

    </tr>
    </#if>
    <#if count == 100 >
    <tr>
    <td colspan="100">This result set has been truncated after 100 rows.  To view the entire results, please <a href='<@s.url value="download?ticketId=${ticketId?c}" />'>download the excel file</a>.</td>
    </tr>    
    </#if>
    <#assign count=count+1>
</#list>
    <#if count == 0 >
    <tr>
    <td colspan="100">There was no data.</td>
    </tr>    
    </#if>


</tbody>
</table>
</div>
</@s.iterator>
</#escape>