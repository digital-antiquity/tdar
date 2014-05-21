<#if callback??>${callback}(</#if><#t>
{"percentDone":${percentDone!0?c},"phase":"${phase!""}","errors":"${(asyncErrors!"")?j_string}"}<#t>
<#if callback??>);</#if>
