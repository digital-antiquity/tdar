Dear ${owner.properName},
  ${user.properName} has signed up for tDAR and now has access to the following items you shared with them:
 <#list items as item>
  - ${item.name} (id: ${item.id?c}) 
 </#list>