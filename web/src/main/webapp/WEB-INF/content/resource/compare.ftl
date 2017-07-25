<#escape _untrusted as _untrusted?html >
<head>
<style>
    th {border-right:1px solid #DDD}
    </style>
    <title>Comparing ${resources?size?c} Resources</title>
</head>
<h3>Comparing ${resources?size?c} Resources</h3>

<#-- for trying to do things as a columns: ${resource['resourceType']} -->
<#assign properties=['id','resourceType','title','description','primaryCreators','activeIndividualAndInstitutionalCredit','resourceProviderInstitution','project','sharedResourceCollections','activeInformationResourceFiles','activeInvestigationTypes','activeMaterialTypes','activeSiteNameKeywords','activeSiteTypeKeywords','activeTemporalKeywords','activeOtherKeywords','activeGeographicKeywords','activeCultureKeywords'] />

<table class="table">
    <tr>
        <th>Id:</th>
        <#list resources as resource>
        <td><a href="${resource.detailUrl}">${resource['id']?c}</a></td>
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
        <th>Status:</th>
        <#list resources as resource>
            <td>${resource.status}</td>
        </#list>
    </tr>
    <tr>
        <th>Project:</th>
        <#list resources as resource>
        <td><#if resource.project??>
            <a href="${resource.project.detailUrl}">${resource.project.title}</a>
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
        <td>
            <#list resource.primaryCreators as it>
                <#if creators?seq_contains(it) >
                <a href="${it.creator.detailUrl}">${it.creator.properName} ${it.role}</a>
                <#else>
                    <b><a href="${it.creator.detailUrl}">${it.creator.properName} ${it.role}</a></b>
                </#if>
                <#sep> <b>&bull;</b> </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Contributors/Credit:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeIndividualAndInstitutionalCredit as it>
                <#if individualRoles?seq_contains(it) >
	                <a href="${it.creator.detailUrl}">${it.creator.properName} ${it.role}</a>
                <#else>
                    <b><a href="${it.creator.detailUrl}">${it.creator.properName} ${it.role}</a></b>
                </#if>
                <#sep> <b>&bull;</b> </#sep>
	        </#list>
		</td>
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
            <#list resource.sharedResourceCollections as it>
                <#if collections?seq_contains(it) >
                    <a href="${it.detailUrl}">${it.name}</a>
                <#else>
                    <b><a href="${it.detailUrl}">${it.name}</a></b>
                </#if>
                <#sep> <b>&bull;</b> </#sep>
            </#list>

        </td>
        </#list>
    </tr>
    <tr>
        <th>Files:</th>
        <#list resources as resource>
        <td>
            <#if resource.informationResourceFiles??>
            <#list resource.informationResourceFiles as file>
				${file.filename} (${file.restriction})
            <#sep> <b>&bull;</b> </#sep>
            </#list>
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
                <#sep> <b>&bull;</b> </#sep>
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
                <#sep> <b>&bull;</b> </#sep>
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
                <#sep> <b>&bull;</b> </#sep>
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
                <#sep> <b>&bull;</b> </#sep>
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
                <#sep> <b>&bull;</b> </#sep>
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
                <#sep> <b>&bull;</b> </#sep>
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
                <#sep> <b>&bull;</b> </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Notes:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeResourceNotes as it>
                <#if notes?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep> <b>&bull;</b> </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Coverage Dates:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeCoverageDates as it>
                <#if coverage?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep> <b>&bull;</b> </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Latitude Longitude Boxes:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeLatitudeLongitudeBoxes as it>
                <#if latitudeLongitude?seq_contains(it) >
                    ${it}
                <#else>
                    <b>${it}</b>
                </#if>
                <#sep> <b>&bull;</b> </#sep>
            </#list>
        </td>
        </#list>
    </tr>
    <tr>
        <th>Resource Annotations:</th>
        <#list resources as resource>
        <td>
            <#list resource.activeResourceAnnotations as it>
                <#if annotations?seq_contains(it) >
                    ${it.resourceAnnotationKey.key}: ${it.value}
                <#else>
                    <b>${it.resourceAnnotationKey.key}: ${it.value}</b>
                </#if>
                <#sep> <b>&bull;</b> </#sep>
            </#list>
        </td>
        </#list>
    </tr>

</table>
</#escape>