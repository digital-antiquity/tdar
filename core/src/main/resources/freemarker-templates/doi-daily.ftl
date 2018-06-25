<#import "email-macro.ftl" as mail /> 
<@mail.content>
<b>DOI Daily Update run on ${date?datetime}</b>
<br><br>
<#if deleted?has_content>
ERRORS: <@join "," errors />
<br><br>
</#if>
<#if created?has_content>
CREATED: <@join "," created />
<br><br>
</#if>
<#if updated?has_content>
UPDATED: <@join "," updated />
<br><br>
</#if>
<#if deleted?has_content>
DELETED: <@join "," deleted />
<br><br>
</#if>
</@mail.content>

<#macro join delim lst>
<#list lst as itm><#if itm_index != 0>${delim}</#if>${itm}</#list> 
</#macro>