The following accounts are overdrawn:
<#list accounts as account>
${account.name} ( ${account.owner.properName} - ${account.owner.email} )
===================================================
Resources: ${account.totalNumberOfResources} (Used: ${account.resourcesUsed}) 
Files: ${account.totalNumberOfFiles} (Used: ${account.filesUsed}) 
Space: ${account.totalSpaceInMb} (Used: ${account.spaceUsedInMb})

Flagged Items:
<#list account.flaggedResources as resource>
  - ${resource.id} ${resource.resourceType} ${resource.title}
</#list>
</#list>