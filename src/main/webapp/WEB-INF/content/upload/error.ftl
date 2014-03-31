{
    "name":"${uploadFileFileName!"error"}",
    "type":"${uploadFileContentType!"error"}",
    "size":"${(totalUploadFileSize!0)?c}",
    "errors":  [
    <#if (actionErrors?has_content) >
    <#list actionErrors as _err>
    {"message": "${_err?js_string}", "details":null}<#if _err_has_next>,</#if>
    </#list>    
    <#else>
    {"message":"an unspecified error occurred", "details":null}
    </#if>
    ]
}