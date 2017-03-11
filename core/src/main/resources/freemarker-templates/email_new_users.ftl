<#list users>
The following users registered with ${siteAcronym} since (${date?date?string.short}):

 <#items as user>
${user.properName}
 ${siteAcronym} page: https://core.tdar.org/browse/creators/${user.id?c}
 email: ${user.email}
 institution: ${(user.institutionName)!"None"}
 affiliation: ${(user.affiliation.label)!"None"}
 contributor reason: ${(user.contributorReason)!"None"}


</#items>
</#list>