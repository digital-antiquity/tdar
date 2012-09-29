<#if parameters.autocompleteName??>
 autocompleteName="${parameters.autocompleteName?html}"<#rt/>
</#if>
<#if parameters.callback??>
 callback="${parameters.callback?html}"<#rt/>
</#if>
<#if parameters.autocompleteIdElement??>
 autocompleteIdElement="${parameters.autocompleteIdElement?html}"<#rt/>
</#if>
<#if parameters.target??>
 target="${parameters.target?html}"<#rt/>
</#if>
<#if parameters.autocompleteParentElement??>
 autocompleteParentElement="${parameters.autocompleteParentElement?html}"<#rt/>
</#if>
<#if parameters.watermark??>
 watermark="${parameters.watermark?html}"<#rt/>
</#if>
<#if parameters.tooltipcontent??>
 tooltipcontent="${parameters.tooltipcontent?html}"<#rt/>
</#if>
<#if parameters.tiplabel??>
 tiplabel="${parameters.tiplabel?html}"<#rt/>
</#if>
<#if parameters.autocomplete??>
 autocomplete="${parameters.autocomplete?html}"<#rt/>
</#if>
<#-- spellcheck attribute is new to html 5 - this custom attribute may not be necessary in future versions of struts -->
<#if parameters.spellcheck??>
 spellcheck="${parameters.spellcheck?html}"<#rt/>
</#if>
