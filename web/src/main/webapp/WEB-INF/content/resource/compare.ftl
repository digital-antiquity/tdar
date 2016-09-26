<#escape _untrusted as _untrusted?html >
<style>
th {border-right:1px solid #DDD}
</style>
<h3>Comparing ${resources?size?c} Resources</h3>

<#-- for trying to do things as a columns: ${resource['resourceType']} -->
<#assign properties=['id','resourceType','title','description','primaryCreators','activeIndividualAndInstitutionalCredit','resourceProviderInstitution','project','sharedResourceCollections','activeInformationResourceFiles','activeInvestigationTypes','activeMaterialTypes','activeSiteNameKeywords','activeSiteTypeKeywords','activeTemporalKeywords','activeOtherKeywords','activeGeographicKeywords','activeCultureKeywords'] />

<table class="table">
    <tr>
        <th>Id:</th>
        <#list resources as resource>
        <td>${resource['id']?c}</td>
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
            <#list resource.activeInvestigationTypes as it>
                <#if investigationTypes?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep>, </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Material Keywords:</th>
        <#list resources as resource>
    <td>
            <#list resource.activeMaterialKeywords as it>
                <#if material?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep>, </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Culture Keywords:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeCultureKeywords as it>
                <#if cultures?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep>, </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Site Name Keywords:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeSiteNameKeywords as it>
                <#if siteNames?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep>, </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Site Type Keywords:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeSiteTypeKeywords as it>
                <#if siteTypes?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep>, </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Geographic Keywords:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeGeographicKeywords as it>
                <#if geographic?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep>, </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Temporal Keywords:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeTemporalKeywords as it>
                <#if temporal?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep>, </#sep>
            </#list>
        </td>
        </#list>
    </tr>

</table>
</#escape>