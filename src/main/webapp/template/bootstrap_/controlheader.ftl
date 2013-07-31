<#-- cast colcount to number,  default to 1 -->
<#assign _numColumns = (parameters.numColumns!'1')?number>

<div class="control-group">
<#if parameters.label?? >
    <label class="control-label">${parameters.label?html}</label>
</#if>

<#if _numColumns &gt; 1>
    <div class="controls controls-row">
<#else>
    <div class="controls">
</#if>    