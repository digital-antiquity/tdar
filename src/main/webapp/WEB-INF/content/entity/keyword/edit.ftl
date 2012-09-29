<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
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