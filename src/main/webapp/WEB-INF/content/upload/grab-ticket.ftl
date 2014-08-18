<#if callback??>${callback}(</#if>
<#if personalFilestoreTicket??>
${personalFilestoreTicket.toJSON()}
<#else>
{}
</#if>
<#if callback??>)</#if>
