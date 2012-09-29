<#if callback??>${callback}(</#if>
{"${lookupSource}":[
<@s.iterator value="jsonResults" var="jsonResult" status="status">
${jsonResult.toJSON().toString()}<@s.if test="!#status.last">,</@s.if>
</@s.iterator>
]}
<#if callback??>);</#if>
