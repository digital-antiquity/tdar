<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<head>
<title>Data Integration: Filtered results</title>
<meta name="lastModifiedDate" content="$Date$"/>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
</head>

<script type='text/javascript'>
function selectAllChildren(id, value) {
    $("input[id*='" + id + "']").attr('checked', value);
}
function selectChildren(index, value) {
    $("input[id$='" + index + "']").attr('checked', value);
    $("input[id*='" + index + "\\.']").attr('checked', value);
}
$(applyZebraColors);
</script>

<div class="glide">
<p>
The integrated data results are displayed below.<br/>
<b><a href='<@s.url value="download?ticketId=${ticketId?c}" />'>DOWNLOAD all results as an Excel file</a></b>.
</p>
</div>

<@s.iterator value='integrationDataResults' var='integrationDataResult'>
<div class="glide">
<@rlist.showControllerErrors />

<h2>Integration results for ${integrationDataResult.dataTable.displayName} (from
${integrationDataResult.dataTable.dataset.title})</h2>
<table class="tableFormat width99percent zebracolors">
<thead>
<tr>
<th>Table</th>

<@s.iterator value='#integrationDataResult.integrationColumns' var='integrationColumn'>
    <th>${integrationColumn.displayName}</th>
</@s.iterator>

<@s.iterator value='#integrationDataResult.columnsToDisplay' var='displayColumn'>
    <th>${displayColumn.displayName}</th>
</@s.iterator>

<@s.iterator value='#integrationDataResult.integrationColumns' var='integrationColumn'>
    <th>Mapped ontology value for ${integrationColumn.displayName}</th>
</@s.iterator>

</tr>
</thead>
<tbody>

<#assign count=0>
<@s.iterator value='#integrationDataResult.rowData' var='rowData' status='rowStatus'>
    <#if (count < 100) >
    <tr>
    <td><small>${integrationDataResult.dataTable.dataset.title}</small></td>

    <@s.iterator value='#rowData.dataValues' var='dataValue'>
    <td>${dataValue!""}</td>
    </@s.iterator>

    <@s.iterator value='#rowData.ontologyValues' var='ontologyValue'>
    <#if ontologyValue??>
    <td>${ontologyValue.displayName}</td>
    </#if>
    </@s.iterator>
    </tr>
    </#if>
    <#if count == 100 >
    <tr>
    <td colspan="100">This result set has been truncated after 100 rows.  To view the entire results, please <a href='<@s.url value="download?ticketId=${ticketId?c}" />'>download the excel file</a>.</td>
    </tr>    
    </#if>
    <#assign count=count+1>
</@s.iterator>
    <#if count == 0 >
    <tr>
    <td colspan="100">There was no data.</td>
    </tr>    
    </#if>


</tbody>
</table>
</div>
</@s.iterator>


