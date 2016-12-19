Dear ${account.owner.properName},
  Your ${acronym} account is overdrawn.  Until additional funds have been added to your account, resources within this account may not be available. 


Account Details:
===================================================
Resources: ${account.totalNumberOfResources} (Used: ${account.resourcesUsed}) 
Files: ${account.totalNumberOfFiles} (Used: ${account.filesUsed}) 
Space: ${account.totalSpaceInMb} (Used: ${account.spaceUsedInMb})

Flagged Items:
<#list account.flaggedResources as resource>
  - ${resource.id} ${resource.resourceType} ${resource.title}
</#list>

View Your Account Online:
${baseUrl}${account.detailUrl}


Add Funds:
${baseUrl}/cart/add
