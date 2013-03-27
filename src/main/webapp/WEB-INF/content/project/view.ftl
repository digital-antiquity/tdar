<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<@view.htmlHeader resourceType="project">
<meta name="lastModifiedDate" content="$Date$"/>
<@view.googleScholar />
</@view.htmlHeader>
 <@search.headerLinks includeRss=false />

<@view.toolbar "${resource.urlNamespace}" "view" />

<@view.basicInformation />
<@view.sharedViewComponents project />


<#if (totalRecords > 0)>

<br/>
<h3>There are ${paginationHelper.totalNumberOfItems?c} Resources within this Project</h3>

    <#if ( results?has_content )>
              <@rlist.listResources resourcelist=results expanded=true listTag="ol" headerTag="h4" titleTag="h5" 
              sortfield=project.sortBy  orientation=project.orientation mapPosition="left" />
    </#if>

    <#if ( paginationHelper.pageCount > 1)>
        <div class="search" style="border-top:1px solid #999;padding:2px 2px 2px 2px">
             <@search.pagination ""/>
        </div>
    </#if>

<#else>
    No resources have been associated with this project.
</#if>

