        <div class="row">
          <div class="col-md-9" role="main">

hi ${authenticatedUser.username}

<table class="table">
<thead>
    <tr>
        <th>path</th> <th>extension</th> <th colspan=3>to PDF</th> <th colspan=3>done PDF</th> <th colspan=4>to Upload</th>
    </tr>
<tr>
    <th></th> <th></th>
    <th> date</th> <th> who</th> <th> size</th>
    <th> date</th> <th> who</th> <th> size</th>
    <th> date</th> <th> who</th> <th> size</th> <th> tDAR ID</th>
</tr>
</thead>
<#list itemStatusReport?keys as key>
<#assign row = itemStatusReport[key] />
<tr>
<td>${row.first.path}</td>
<td>${row.first.extension}</td>
<#if row.toPdf?has_content>
	<@_printrow row.toPdf />
<#else>
	<@_blankrow />
</#if>
<#if row.doneOcr?has_content>
	<@_printrow row.doneOcr />
<#else>
	<@_blankrow />
</#if>
<#if row.toUpload?has_content>
	<@_printrow row.toUpload />
    <td>
        <#if (row.toUpload.tdarId)?has_content><a href="http://core.tdar.org/resource/${row.toUpload.tdarId?c}">${row.toUpload.tdarId?c}</a></#if>
    </td>
 <#else>
	<@_blankrow />
</#if>
</tr>
</#list>
</table>
</div>
</div>

<#macro _printrow item>
<#if item?has_content>
<td><#if item.dateModified?is_date>${item.dateModified?string.short}</#if></td>
<td>${item.ownerId!''} </td>
<td>${item.size!''}</td>
</#if>
</#macro>

<#macro _blankrow>
<td></td>
<td></td>
<td></td>
</#macro>