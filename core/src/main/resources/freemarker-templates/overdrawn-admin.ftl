<#import "email-macro.ftl" as mail /> 
<@mail.content>
The following accounts are overdrawn:<br />
<#list accounts as account>
    <p>
    ${account.name} ( ${account.owner.properName} - ${account.owner.email} )<br />
    <hr />
    Resources: ${account.totalNumberOfResources} (Used: ${account.resourcesUsed}) <br />
    Files: ${account.totalNumberOfFiles} (Used: ${account.filesUsed}) <br />
    Space: ${account.totalSpaceInMb} (Used: ${account.spaceUsedInMb})<br />
    </p>

    <p>
        Flagged Items:
        <ul>
        <#list account.flaggedResources as resource>
          <a href="${baseUrl}${resource.detailUrl}">${resource.id} ${resource.resourceType} ${resource.title}</a>
        </#list>
        </ul>
    </p>
</#list>
</@mail.content>