<title>download: ${fileName}</title>
<div class="glide">
<h3>You have requested the following file for download:</h3>
<div>
<#assign download>/filestore/${informationResourceFileId?c}</#assign>
You requested this file (<a href="${download}">${fileName}</a>) when you were not logged into tDAR, please click <a href="${download}">here</a> to begin your download.<br/><br/>
<span style="font-size:115%">
<a href="${download}">Download ${fileName}</a>
</span>
</div>
</div>