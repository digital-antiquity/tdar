<title>download: ${fileName}</title>
<div class="glide">
    <h3>You have requested the following file for download:</h3>
    <div>
        <#assign download>/filestore/${informationResourceFileId?c}</#assign>
        You requested this file 
        (<a href="${download}"  onClick="registerDownload('<@s.url value='/filestore/${informationResourceFileId?c}/get'/>', '${informationResourceId?c}')" >${fileName}</a>) 
        when you were not logged into ${siteAcronym}, please click 
        <a href="${download}" onClick="registerDownload('<@s.url value='/filestore/${informationResourceFileId?c}/get'/>', '${informationResourceId?c}')" >here</a> 
        to begin your download.
        <br/><br/>
        <span style="font-size:115%">
            <a href="${download}" onClick="registerDownload('<@s.url value='/filestore/${informationResourceFileId?c}/get'/>', '${informationResourceId?c}')" >Download ${fileName}</a>
        </span>
    </div>
</div>