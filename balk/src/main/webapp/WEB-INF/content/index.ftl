<#setting url_escaping_charset="UTF-8">

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
<#assign _path = "/" />

<#list itemStatusReport?keys as key>
<#assign row = itemStatusReport[key] />
<#assign path = key />
<#if !row.usingWorkflow>
    <#assign path = row.first.path/> 
</#if>
<#assign _parentPath = (path?keep_before_last("/"))!'' />

<#if (_parentPath != _path) >
<tr>
    <td colspan=50>${_parentPath}</td>
    <#assign _path = path?keep_before_last("/") />
</#if>
<tr>
<td>${path}</td>
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
<td><#if item.dateModified?is_date><a href="https://www.dropbox.com/work${item.parentDirName?ensure_starts_with("/")?url_path}?preview=${item.name}" target="_blank">${item.dateModified?string.short}</#if></td>
<td>${item.ownerId!''} </td>
<td>${item.size!''}</td>
</#if>
</#macro>

<#macro _blankrow>
<td></td>
<td></td>
<td></td>
</#macro>