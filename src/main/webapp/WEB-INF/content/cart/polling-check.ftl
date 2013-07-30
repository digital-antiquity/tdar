<#if callback??>${callback}(</#if>
${invoice.toJSON().toString()}
<#if callback??>);</#if>
