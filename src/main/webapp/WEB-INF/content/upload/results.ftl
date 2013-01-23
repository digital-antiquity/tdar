[
<#list uploadFileFileName as fileName>
{    
    "name":"${fileName!"error"}",
    "type":"${uploadFileContentType[fileName_index]!"error"}",
    "size":${(uploadFileSize[fileName_index]!0)?c},
    "delete_type": "DELETE"
}<#if fileName_has_next>,</#if>
</#list>
]