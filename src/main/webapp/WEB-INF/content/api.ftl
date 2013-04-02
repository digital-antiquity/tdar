<apiResult>
  <#if status?has_content><status>${status?html}</status></#if>
  <#if id?has_content><recordId>${id?c}</recordId></#if>
  <#if message?has_content><message>${message?c}</message></#if>
</apiResult>
