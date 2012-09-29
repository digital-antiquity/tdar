{
    "name":"${uploadFileFileName!"error"}",
    "type":"${uploadFileContentType!"error"}",
    "size":"${(uploadFileSize!0)?c}",
    "errors":  [
    <#if (actionErrors?? && actionErrors.size() > 0) >
    <#list actionErrors as _err>
    {"message": "${_err?js_string}", "details":null}<#if _err_has_next>,</#if>
    </#list>    
    <#else>
    {"message":"an unspecified error occured", "details":null}
    </#if>
    ]
}