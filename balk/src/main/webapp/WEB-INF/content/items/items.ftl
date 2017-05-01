<#setting url_escaping_charset="UTF-8">

        <div class="row">
          <div class="col-md-9" role="main">
         <h3> ${path!'Everything'}</h3>
<ul class="pagination">
	<#list 0 .. (total / size) as pageNum >
	    <li  <#if pageNum == page>class="disabled"</#if>><a href="<@s.url>
		<@s.param name="path" value="'${path!''?url}'" />
		<@s.param name="page" value="${pageNum}"/>
		<@s.param name="size" value="${size}" />
		<@s.param name="managed" value="${managed?c}" />
</@s.url>">${pageNum+1}</a></li>
    </#list>
</ul>

<#list children>
    <h5>Child Paths</h5>
    <ul>
        <#items as item>
            <li><a href="?path=${path!''}/${item}&managed=${managed?c}">${item}</a></li>
        </#items>
    </ul>
</#list>

<table class="table">
<thead>
    <tr>
        <th>extension</th> <th>path</th> <th>date</th><th>size</th><th colspan=4>to PDF</th> <th colspan=4>done PDF</th> <th colspan=5>to Upload</th>
    </tr>
<tr>
    <th></th> <th></th>
    <th></th> <th></th>
    <th> date</th> <th> who</th> <th> size</th><th></th>
    <th> date</th> <th> who</th> <th> size</th><th></th>
    <th> date</th> <th> who</th> <th> size</th> <th> tDAR ID</th><th></th>
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
    <th colspan=50 style="background:#efefef">${_parentPath}</th>
    <#assign _path = path?keep_before_last("/") />
</#if>
<tr>
<td style="padding:1em;text-align:center"><span class="label label-default label-sm">${row.first.extension}</span></td>
<td><a href="https://www.dropbox.com/work${row.first.parentDirName?ensure_starts_with("/")?url_path}?preview=${row.first.name}" target="_blank">${row.first.name}</a></td>
<td>${row.first.dateModified?string.short}</td>
<td>${row.first.size!''}

<#if !row.usingWorkflow>
    <@s.form action="/startWorkflow/?" method="POST">
        <@s.hidden name="id" value="${row.first.dropboxId}"/>
        <@s.hidden name="path" value="${_path}"/>
        <@s.hidden name="phase" value="UPLOAD_TDAR"/>
        <@s.submit name="approve" value="Upload" />
    </@s.form>
    <@s.form action="/startWorkflow/?" method="POST">
        <@s.hidden name="id" value="${row.first.dropboxId}"/>
        <@s.hidden name="path" value="${_path}"/>
        <@s.hidden name="phase" value="TO_PDFA"/>
        <@s.submit name="approve" value="PDF" />
    </@s.form>

</#if>
</td>
	<@_printrow itemStatusReport row "TO_PDFA" />
	<@_printrow itemStatusReport row "DONE_PDFA"/>
	<@_printrow itemStatusReport row "UPLOAD_TDAR"/>
    <td>
        <#if (row.toUpload.tdarReference.tdarId)?has_content><a href="http://core.tdar.org/resource/${row.toUpload.tdarReference.tdarId?c}">${row.toUpload.tdarReference.tdarId?c}</a></#if>
    </td>
</tr>
</#list>
</table>
</div>
</div>

<#macro _printrow report row phase>
<#if phase == 'TO_PDFA' && row.toPdf?has_content >
    <#local item=row.toPdf />
</#if>
<#if phase == 'DONE_PDFA' && row.doneOcr?has_content >
    <#local item=row.doneOcr/>
</#if>
<#if phase == 'UPLOAD_TDAR' && row.toUpload?has_content>
    <#local item=row.toUpload />
</#if>
<#if item?has_content>
    <td><#if item.dateModified?is_date><a href="https://www.dropbox.com/work${item.parentDirName?ensure_starts_with("/")?url_path}?preview=${item.name}" target="_blank">${item.dateModified?string.short}</#if></td>
    <td>${item.ownerId!''} </td>
    <td>${item.size!''}</td>
<#else>
    <td></td>
    <td></td>
    <td></td>
</#if>
<td style="border-right:1px solid #999">
<#if row.nextPhase?has_content && item?has_content && userInfo?has_content && userInfo.token?has_content && row.currentPhase == phase>
	<@s.form action="/approve/?" method="POST">
		<@s.hidden name="id" value="${item.dropboxId}"/>
		<@s.hidden name="phase" value="${row.nextPhase}"/>
		<@s.hidden name="path" value="${_path}"/>
		<@s.submit name="approve" value="Approve" />
	</@s.form>
</#if>
</td>
</#macro>

<#macro _blankrow>
<td></td>
<td></td>
<td></td>
</#macro>
<script src="<@s.url value="/components/StickyTableHeaders/js/jquery.stickytableheaders.js"/>"/>
<script>
$(document).ready(function() {
    $("table").stickyTableHeaders();
});
</script>