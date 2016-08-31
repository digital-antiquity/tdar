        <div class="row">
          <div class="col-md-9" role="main">

hi ${authenticatedUser.username}

<table class="table">
<thead>
<tr>
<th>path</th>
<th colspan=3>to PDF</th>
<th colspan=3>done PDF</th>
<th colspan=3>to Upload</th>
</tr>
<tr>
<th></th>
<th> date</th>
<th> who</th>
<th> size</th>
<th> date</th>
<th> who</th>
<th> size</th>
<th> date</th>
<th> who</th>
<th> size</th>
</tr>
</thead>
<#list itemStatusReport?keys as key>
<tr>
<td>${key}</td>
<#if itemStatusReport[key].toPdf?has_content>
	<@_printrow itemStatusReport[key].toPdf />
<#else>
	<@_blankrow />
</#if>
<#if itemStatusReport[key].doneOcr?has_content>
	<@_printrow itemStatusReport[key].doneOcr />
<#else>
	<@_blankrow />
</#if>
<#if itemStatusReport[key].toUpload?has_content>
	<@_printrow itemStatusReport[key].toUpload />
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