<#-- from controlheader and controlheader-core.ftl -->
<#-- cast colcount to number,  default to 1 -->
<#assign _numColumns = (parameters.numColumns!'1')?number>
<#assign hasFieldErrors = parameters.name?? && fieldErrors?? && fieldErrors[parameters.name]??/>

<div class="control-group <#rt/>
<#if hasFieldErrors> 
 error <#rt/>
</#if>
"><#rt/>
<#if parameters.label??>
    <label class="control-label" <#t/>
<#if parameters.id??>
        for="${parameters.id?html}" <#rt/>
</#if>
    ><#rt/>
<#if parameters.required?default(false) && parameters.requiredposition?default("right") != 'right'>
        <span class="required">*</span><#rt/>
</#if>
${parameters.label?html}<#t/>
<#if parameters.required?default(false) && parameters.requiredposition?default("right") == 'right'>
 <span class="required">*</span><#rt/>
</#if>
${parameters.labelseparator?default("")?html}<#rt/>
<#include "/${parameters.templateDir}/bootstrap/tooltip.ftl" />
</label><#rt/>
</#if>
<#if _numColumns &gt; 1>
    <div class="controls controls-row">
<#else>
    <div class="controls">
</#if>
<#lt/>
<#if (parameters.dynamicAttributes?? && parameters.dynamicAttributes?size > 0 && parameters.dynamicAttributes["helpText"]??)><#rt/>
<#assign helpText = parameters.dynamicAttributes.remove("helpText")/><#rt/>
</#if><#rt/>