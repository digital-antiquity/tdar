<#import "../email-macro.ftl" as mail /> 
<@mail.content>
${from.properName} is requesting that the following 'people' be merged into their user:<br />
<#list merge![]>
<ul>
    <#items as entry>
    <li>${entry}</li>
    </#items>
</ul>
</#list>
<hr/>
Note: please do not reply to this automated email
</@mail.content>