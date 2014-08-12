The following users were added to ${siteAcronym} in the last day (${date?string.short}):

<#list users as user>
 - ${user.properName} (${user.email}) - ${user.institutionName}  : ${user.contributorReason!''}
</#list>