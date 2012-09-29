<#if callback??>${callback}(</#if>
{ "percentDone" : ${percentDone!0}, "phase" : "${phase!'initializing...'}" }
<#if callback??>);</#if>
