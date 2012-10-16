<div class="control-group">
<#if parameters.label?? >
    <label class="control-label">${parameters.label?html}</label>
</#if>

<#if parameters.numColumns?has_content>
    <div class="controls-row">
<#else>
    <div class="controls">
</#if>    