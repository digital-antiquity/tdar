<#if callback??>${callback}(</#if>
{"institutions":[
<@s.iterator value="institutions" var="inst" status="status">
${inst.toJSON().toString()}<@s.if test="!#status.last">,</@s.if>
</@s.iterator>
]}
<#if callback??>);</#if>
