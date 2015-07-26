[<#list activePeople as person>
"${person.properName?js_string}"<#if person_has_next>, </#if><#t>
</#list>]