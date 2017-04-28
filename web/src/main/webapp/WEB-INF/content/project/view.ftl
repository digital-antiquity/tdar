<#escape _untrusted as _untrusted?html>

    <#global includeRssAndSearchLinks=true>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>

    <#macro head>
    <style>
        i.search-list-checkbox-grey {
            background-image: none !important;
        }

        li.media {
            display: inline-block
        }
    </style>
    </#macro>




    <#macro footer>
    <div id="divSearchContext" parse="true">
        <input id="cbctxid" type="checkbox" name="projectId" value="${id?c}">
        <label for="cbctxid">Search within this project</label>
    </div>


        <#if (totalRecords > 0)>
			<@search.partFacet selectedResourceTypes paginationHelper "Project" "h3" />

            <#if ( results?has_content )>
                <@rlist.listResources resourcelist=results listTag="ol" headerTag="h4" titleTag="h5" itemsPerRow=4
                sortfield=sortField  orientation=resource.orientation mapPosition="top" mapHeight="400" />
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
