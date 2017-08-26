<#import "../email-macro.ftl" as mail /> 
<@mail.content>
Dear ${owner.properName},<br />
  ${user.properName} has signed up for tDAR and now has access to the following items you shared with them:
  <ul>
 <#list items as item>
  <li> ${item.name} (id: ${item.id?c})</li> 
 </#list>
 </ul>
 </@mail.content>