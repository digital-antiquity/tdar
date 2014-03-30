<#assign ticketjson = "{}">
<#if personalFilestoreTicket??>
<#assign ticketjson = personalFilestoreTicket.toJSON()>
</#if>
{"files":[<#t>
<#list uploadFileFileName as fileName>
{"name":"${(fileName!"error")?js_string?replace("\\'", "'")}",<#t>
    "type":"${(uploadFileContentType[fileName_index]!"error")?js_string}",<#t>
    "size":${(uploadFileSize[fileName_index]!0)?c},<#t>
    "delete_type": "DELETE"<#t>
}<#if fileName_has_next>,</#if><#t>
</#list><#t>
],<#t>
    "ticket": ${ticketjson}<#t>
}
