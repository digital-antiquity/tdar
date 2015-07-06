<#escape _untrusted as _untrusted?html >
<style>
th {border-right:1px solid #DDD}
</style>
<h3>Comparing ${resources?size?c} Resources</h3>

<table class="table">
    <tr>
        <th>Id:</th>
        <#list resources as resource>
        <td>${resource.id?c}</td>
        </#list>
    </tr>
    <tr>
        <th>Type:</th>
        <#list resources as resource>
        <td>${resource.resourceType}
            <#if resource.resourceType.document>
                (${resource.documentType})
            </#if>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Project:</th>
        <#list resources as resource>
        <td><#if resource.project??>
            ${resource.project.title}
        </#if></td>
        </#list>
    </tr>
    <tr>
        <th>Description:</th>
        <#list resources as resource>
        <td>${resource.title}</td>
        </#list>
    </tr>
    <tr>
        <th>Creators:</th>
        <#list resources as resource>
        <td>${resource.primaryCreators}</td>
        </#list>
    </tr>
    <tr>
        <th>Contributors/Credit:</th>
        <#list resources as resource>
        <td>${resource.activeIndividualAndInstitutionalCredit}</td>
        </#list>
    </tr>
    <tr>
        <th>Resource Provider:</th>
        <#list resources as resource>
        <td><#if resource.resourceProviderInstitution??>
            ${resource.resourceProviderInstitution}
        </#if></td>
        </#list>
    </tr>
    <tr>
        <th>Collections:</th>
        <#list resources as resource>
        <td>
            ${resource.sharedResourceCollections}
        </td>
        </#list>
    </tr>
    <tr>
        <th>Files:</th>
        <#list resources as resource>
        <td>
            <#if resource.informationResourceFiles??>
            ${resource.informationResourceFiles}
            </#if>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Investigation Types:</th>
        <#list resources as resource>
        <td>
            ${resource.activeInvestigationTypes}
        </td>
        </#list>
    </tr>
    <tr>
        <th>Material Keywords:</th>
        <#list resources as resource>
        <td>
            ${resource.activeMaterialKeywords}
        </td>
        </#list>
    </tr>
    <tr>
        <th>Culture Keywords:</th>
        <#list resources as resource>
        <td>
            ${resource.activeCultureKeywords}
        </td>
        </#list>
    </tr>
    <tr>
        <th>Site Name Keywords:</th>
        <#list resources as resource>
        <td>
            ${resource.activeSiteNameKeywords}
        </td>
        </#list>
    </tr>
    <tr>
        <th>Site Type Keywords:</th>
        <#list resources as resource>
        <td>
            ${resource.activeSiteTypeKeywords}
        </td>
        </#list>
    </tr>
    <tr>
        <th>Geographic Keywords:</th>
        <#list resources as resource>
        <td>
            ${resource.activeGeographicKeywords}
        </td>
        </#list>
    </tr>
    <tr>
        <th>Temporal Keywords:</th>
        <#list resources as resource>
        <td>
            ${resource.activeTemporalKeywords}
        </td>
        </#list>
    </tr>
    <tr>
        <th>Site Type Keywords:</th>
        <#list resources as resource>
        <td>
            ${resource.activeSiteTypeKeywords}
        </td>
        </#list>
    </tr>

</table>
</#escape>