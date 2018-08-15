<#if (parameters.includeGroup?exists && parameters.includeGroup == false) || parameters.template=='checkbox'><#elseif (parameters.cssClass!'')?contains("col-") && (!parameters.labelposition?exists || !parameters.labelposition.equalsIgnoreCase('left'))><#else>
<div class="form-group <#if parameters.formGroupClass?has_content>${parameters.formGroupClass}</#if><#if parameters.labelposition?exists &&  parameters.labelposition.equalsIgnoreCase('left')>row</#if>">
</#if>
<#if !parameters.labelposition?exists && (parameters.template != 'checkbox') || parameters.labelposition?exists && 
    (parameters.labelposition.equalsIgnoreCase('left') || parameters.labelposition.equalsIgnoreCase('top')) >
    <#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" />
</#if>