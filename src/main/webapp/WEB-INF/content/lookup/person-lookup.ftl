<#if callback??>${callback}(</#if>
{"people":[
<@s.iterator value="people" var="person" status="status">
${person.toJSON().toString()}<@s.if test="!#status.last">,</@s.if>
</@s.iterator>
]}
<#if callback??>);</#if>
