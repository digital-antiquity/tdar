<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<@search.initResultPagination/>
<@view.htmlHeader resourceType="project">
<meta name="lastModifiedDate" content="$Date$"/>
<@view.googleScholar />
</@view.htmlHeader>

<@view.toolbar "${resource.urlNamespace}" "view" />

<@view.basicInformation />
<@view.sharedViewComponents project />


<#if (totalRecords > 0)>

<br/>
<h3>There are ${project.informationResources.size()} Resources within this Project</h3>

    <#if ( results?? && results.size() > 0 )>
                      <@rlist.listResources results 'RESOURCE_TYPE' false true true "ol" "h4" />
    </#if>

    <#if ( results?? && numPages > 1)>
<div style="border-top:1px solid #999;padding:2px 2px 2px 2px">
         <@search.pagination ""/>
</div>
    </#if>

</div>
<#else>
    No resources have been associated with this project.
</#if>