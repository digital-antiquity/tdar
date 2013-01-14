 [
<#if !subcategories?? || subcategories.isEmpty() >
        {"value": -1, "label": "N/A"}
<#else>
 <#list subcategories as subcategory>
        <#if subcategory_index != 0>,</#if>
        {"value": ${subcategory.id?c}, "label": "${subcategory.label?js_string}"}
 </#list>
</#if>
]
