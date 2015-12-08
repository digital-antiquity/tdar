The following resources were added to ${siteAcronym} in the last week (${date?date?string.short}):

<#list resources as resource><#if resource?has_content>
 - ${resource.title!'no title'} (${resource.resourceType!'unknown'}) - ${siteUrl}${resource.detailUrl!''}
     <#if ((resource.description)?length > 76)>${resource.description?substring(76)}<#else>${resource.description!'[no description]'}</#if>
</#if></#list>

These resources have been added to the following collection: ${collectionUrl}