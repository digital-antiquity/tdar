<#import "email-macro.ftl" as mail /> 
<@mail.content>
<b>The following accounts are overdrawn:</b><br />
<#list accounts as account>
    <p>
    ${account.name} ( ${account.owner.properName} - ${account.owner.email} )<br />
    <hr />
    Resources: ${account.totalNumberOfResources} (Used: ${account.resourcesUsed}) <br />
    Files: ${account.totalNumberOfFiles} (Used: ${account.filesUsed}) <br />
    Space: ${account.totalSpaceInMb} (Used: ${account.spaceUsedInMb})<br />
    </p>

    <p>
        <b>Flagged Items:</b>
        <ul>
        <#list account.flaggedResources as resource>
          <a href="${baseUrl}${resource.detailUrl}">${resource.title}</a> (${resource.id})
        </#list>
        </ul>
    </p>
</#list>
</@mail.content>