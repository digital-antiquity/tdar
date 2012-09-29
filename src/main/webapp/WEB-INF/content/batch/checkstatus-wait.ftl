<#if callback??>${callback}(</#if>
{ "percentDone" : ${percentDone}, "phase" : "${phase}", "errors": "${asyncErrors?j_string}" }
<#if callback??>);</#if>
