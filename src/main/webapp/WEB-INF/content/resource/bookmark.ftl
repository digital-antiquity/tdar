<#if callback??>
 ${callback}(<#rt />
</#if>
{
<#if success >
    "success" : true
<#else>
    "success" : false
</#if>
}<#if callback??>
 );<#rt />
</#if>
