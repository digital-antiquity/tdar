<#if callback??>${callback}(</#if><#t>
{"percentDone":${percentDone?c},"phase":"${phase}","errors":"${asyncErrors?j_string}"}<#t>
<#if callback??>);</#if>
