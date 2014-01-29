<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#-- FIXME: remove this page and controller hooks (TDAR-3545)
<#-- These macros were in edit-macros.ftl, but were incomplete and only referenced here.

<#macro keywordNodeOptions node selectedKeyword>
    <option>not implemented</option>
</#macro>

<#macro keywordNodeSelect label node selectedKeywordId selectTagId='keywordSelect'>
    <label for="${selectTagId}">${label}</label>
    <select id="${selectTagId}">
        <@keywordNodeOptions node selectedKeywordId />
    </select>
</#macro>
 -->
<div class="glide">
    <h3>Keyword Information</h3>
    
    <@s.hidden name="id" />
    <@s.textfield name="keyword.label" label="Label" labelPosition="left" required="true" />
    <#if suggestedKeyword>
        <br />
        <@edit.boolfield "keyword.approved" "Approved?" />
    </#if>
    <br />
    <@s.textarea name="keyword.definition" label="Definition" labelposition="top" />
    <#if hierarchicalKeyword>
        <@edit.keywordNodeSelect "select a damn parent" potentialParents keyword.id /> 
    </#if>
    
</div>
</#escape>