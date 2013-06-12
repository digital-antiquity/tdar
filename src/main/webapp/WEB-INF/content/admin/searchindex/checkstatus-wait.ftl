<#if callback??>${callback}(</#if>
{ 
    "percentDone": ${percentComplete!0}, 
    "phase" : "${(status!'initializing...')?js_string}",
    "errorHtml" : "${(htmlAsyncErrors!"")?j_string}"
}
<#if callback??>);</#if>
