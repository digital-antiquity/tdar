<#import "email-macro.ftl" as mail /> 

<@mail.content>
The following resources were added to ${siteAcronym} in the last week (${date?date?string.short}):

<ul>
<#list resources as resource><#if resource?has_content>
 <li> <a href="${siteUrl}${resource.detailUrl}">${resource.title!'no title'}</a> (${resource.resourceType!'unknown'}) 
     <#if ((resource.description)?length > 76)>${resource.description?substring(0,76)}...<#else>${resource.description!'[no description]'}</#if></li>


</#if></#list>
</ul>
These resources have been added to the following collection: ${collectionUrl}
</@mail.content>
