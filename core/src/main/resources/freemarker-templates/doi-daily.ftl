DOI Daily Update run on ${date?datetime}

<#if deleted?has_content>
ERRORS: <@join "," errors />
</#if>

<#if created?has_content>
CREATED: <@join "," created />
</#if>

<#if updated?has_content>
UPDATED: <@join "," updated />
</#if>

<#if deleted?has_content>
DELETED: <@join "," deleted />
</#if>

<#macro join delim lst>
<#list lst as itm><#if itm_index != 0>${delim}</#if>${itm}</#list> 
</#macro>