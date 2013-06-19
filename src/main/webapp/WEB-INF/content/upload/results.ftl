<#assign ticketjson = "{}">
<#if personalFilestoreTicket??>
<#assign ticketjson = personalFilestoreTicket.toJSON()>
</#if>

{"files":[
<#list uploadFileFileName as fileName>
{   
    "name":"${(fileName!"error")?js_string}",
    "type":"${(uploadFileContentType[fileName_index]!"error")?js_string}",
    "size":${(uploadFileSize[fileName_index]!0)?c},
    "delete_type": "DELETE"
}<#if fileName_has_next>,</#if>
</#list>
],
    "ticket": ${ticketjson}
}
