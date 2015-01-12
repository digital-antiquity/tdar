<#if parameters.labelposition?exists && parameters.labelposition.equalsIgnoreCase('right') >
    <#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" />
</#if>