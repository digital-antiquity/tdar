<#escape _untrusted as _untrusted?html>

<#global includeRssAndSearchLinks=true>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>

<#macro head>
<style>
i.search-list-checkbox-grey {background-image:none!important;}
li.media { display:inline-block}
</style>
</#macro>


<#macro footer>
        <#if selectedResourceTypes.empty>
        <@search.facetBy facetlist=resourceTypeFacets currentValues=selectedResourceTypes label="Browse by Resource Type(s)" facetParam="selectedResourceTypes" />
		</#if>
<#if (totalRecords > 0)>

<br/>
<h3>There are ${paginationHelper.totalNumberOfItems?c}


 <#if selectedResourceTypes?has_content>
${resourceTypeFacets[0].plural}
 <#else>Resources</#if> within this Project <#if selectedResourceTypes?has_content>                <sup><a style="text-decoration: " href="<@s.url includeParams="all">
            <@s.param name="selectedResourceTypes"value="" />
            <@s.param name="startRecord" value=""/>
</@s.url>">[remove this filter]</a></sup>
 </#if>
 </h3>

    <#if ( results?has_content )>
              <@rlist.listResources resourcelist=results listTag="ol" headerTag="h4" titleTag="h5" 
              sortfield=sortField  orientation=project.orientation mapPosition="left" />
    </#if>

    <#if ( paginationHelper.pageCount > 1)>
        <div class="search" style="border-top:1px solid #999;padding:2px 2px 2px 2px">
             <@search.pagination ""/>
        </div>
    </#if>

<#else>
    No resources have been associated with this project.
</#if>
</#macro>
</#escape>
