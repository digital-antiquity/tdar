<#if  (parameters.template == 'checkbox') || parameters.labelposition?exists && parameters.labelposition.equalsIgnoreCase('right') >
    <#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" />
</#if>
<#if (parameters.includeGroup)!true == false || parameters.template=='checkbox'><#elseif (parameters.cssClass!'')?contains("col-") && (!parameters.labelposition?exists || !parameters.labelposition.equalsIgnoreCase('left'))><#else>
</div>
</#if>
