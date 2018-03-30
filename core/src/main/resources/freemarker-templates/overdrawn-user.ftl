<#import "email-macro.ftl" as mail /> 
<@mail.content>
Dear ${account.owner.properName},<br />
Your ${siteAcronym} account is overdrawn.  Until additional funds have been added to your account, resources within this account may not be available. 
<br />
<p>
Account Details:
<hr />
Resources: ${account.totalNumberOfResources} (Used: ${account.resourcesUsed}) <br />
Files: ${account.totalNumberOfFiles} (Used: ${account.filesUsed}) <br />
Space: ${account.totalSpaceInMb} (Used: ${account.spaceUsedInMb})<br />
</p>

<p>
Flagged Items:
<ul>
<#list account.flaggedResources as resource>
  <li>
  <a href="${baseUrl}${resource.detailUrl}">${resource.title}</a> (${resource.id})
  </li>
</#list>
<ul>
</p>
<a href="${baseUrl}${account.detailUrl}">View Your Account Online</a><br />
<br />
<a href="${baseUrl}/cart/add">Add Funds</a>
</@mail.content>