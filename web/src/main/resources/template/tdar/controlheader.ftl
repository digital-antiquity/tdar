<#if !parameters.labelposition?exists || parameters.labelposition?exists && 
    (parameters.labelposition.equalsIgnoreCase('left') || parameters.labelposition.equalsIgnoreCase('top')) >
    <#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" />
</#if>