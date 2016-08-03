<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>


<#macro head>
    <@search.headerLinks includeRss=false />
    <title>${resourceCollection.name!"untitled collection"}</title>
    <@view.canonical resourceCollection />
    <#assign rssUrl = "/search/rss?groups[0].fieldTypes[0]=COLLECTION&groups[0].collections[0].id=${resourceCollection.id?c}&groups[0].collections[0].name=${(resourceCollection.name!'untitled')?url}">
    <@search.rssUrlTag url=rssUrl />
    <link rel="alternate" href="/api/lod/collection/${id?c}" type="application/ld+json" />    

</#macro>

<#macro sidebar>

    <!-- Don't show header if header doesn't exist -->
    <div id="sidebar-right" parse="true">
        <#if results?has_content>
        <br/><br/>
        <hr class="light"/>
        <@common.renderWorldMap mode="mini" />
        <hr class="light"/>
<!--            <h3>Contents</h3> -->
            <@search.facetBy facetlist=resourceTypeFacets label="" facetParam="selectedResourceTypes" link=false liCssClass="" ulClass="unstyled" pictoralIcon=true />
<i class="icon-document-red"></i>
        </#if>
        <#if collections?has_content && !collections.empty > 
            <h3>Child Collections</h3>
            <@common.listCollections collections=collections showOnlyVisible=true />
        </#if>
		<@list.displayWidget />
    </div>
</#macro>

<#macro header>
    <#if editable>
        <@nav.collectionToolbar "collection" "view">
            <@nav.makeLink
            namespace="collection"
            action="add?parentId=${id?c}"
            label="create child collection"
            name="columns"
            current=current
            includeResourceId=false
            disabled=disabled
            extraClass="hidden-tablet hidden-phone"/>
        </@nav.collectionToolbar>
    </#if>

    <@view.pageStatusCallout />

</#macro>

    <#macro _keywordSection label keywordList searchParam>
        <#list keywordList>
        <p>
            <strong>${label}</strong><br>
            <#items as item>
                <a href="${item.detailUrl}">${item.label}</a><#sep> &bull;</#sep>
            </#items>
        </p>
        </#list>
    </#macro>

<#macro descriptionSection>
    <#if editor>
    <div data-spy="affix" class="affix  screen adminbox rotate-90"><a href="<@s.url value="/collection/admin/${id?c}"/>">ADMIN</a></div>
    </#if>
        <#if resourceCollection.parent?? || resourceCollection.description??  || resourceCollection.formattedDescription?? || collections??>
        <div>
            <#if resourceCollection.parent??><p><b>Part of:</b>
                <#if resourceCollection.parent.hidden && !authenticated >
                    ${resourceCollection.parent.name!"(n/a)"}
                <#else>
                 <a
                    href="${resourceCollection.parent.detailUrl}">${resourceCollection.parent.name!"(n/a)"}</a>
                </#if>
            </p></#if>

            <div class="viewpage-section">
                <#-- TODO: move this logic to logoAvailable() -->
                <#nested/>
                <@common.description resourceCollection.description />

                <#if resourceCollection.formattedDescription??>
                    <p>
                        <#noescape>
                        ${resourceCollection.formattedDescription}
                    </#noescape>
                    </p>
                </#if>
            </div>
        </div>
        </#if>
</#macro>

<#macro keywordSection>
            <#if keywordSectionVisible>
            <!-- <h5>Common Keywords found within this Collection</h5> -->
            <div class="row">
                <div class="span4">
                <@_keywordSection "Site Name Keywords" facetWrapper.facetResults['activeSiteNameKeywords']![] "query" />
                <@_keywordSection "Site Type Keywords" facetWrapper.facetResults['activeSiteTypeKeywords']![] "query" />
                <@_keywordSection "Other Keywords" facetWrapper.facetResults['activeOtherKeywords']![] "query" />
                <@_keywordSection "Culture Keywords" facetWrapper.facetResults['activeCultureKeywords']![] "query" />
                </div>

                <div class="span4">
                <@_keywordSection "Investigation Types" facetWrapper.facetResults['activeInvestigationTypes']![] "query" />
                <@_keywordSection "Material Types" facetWrapper.facetResults['activeMaterialKeywords']![] "query" />
                <@_keywordSection "Temporal Keywords" facetWrapper.facetResults['activeTemporalKeywords']![] "query" />
                <@_keywordSection "Geographic Keywords" facetWrapper.facetResults['activeGeographicKeywords']![] "query" />
                </div>
            </div>
            <hr/>
            </#if>
</#macro>

<#macro resultsSection>

        <#if results?has_content>
        <div id="divResultsSortControl">
            <h2>Resources Inside This Collection</h2>
            <div class="row">
                <div class="span4">
                    <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Record"/>
                </div>
                <div class="span5"></div>
            </div>
        </div>
        
        
        
        <div class="collection-facets">
            <#assign mapSize="450" />
<#-- for when map orientiation is 'left' or 'right'  
            <#if (totalRecords > 10)>
                <#assign mapSize="700" />
            </#if>
            <#if (totalRecords > 18)>
                <#assign mapSize="1000" />
            </#if>
-->
            <#if selectedResourceTypes.empty>
                <@search.facetBy facetlist=resourceTypeFacets currentValues=selectedResourceTypes label="" facetParam="selectedResourceTypes" />
            <#else>
            <h4>
                There <#if paginationHelper.totalNumberOfItems == 1>is<#else>are</#if> ${paginationHelper.totalNumberOfItems?c}


            <#if selectedResourceTypes?has_content>
                <#if paginationHelper.totalNumberOfItems == 1>
                    <@s.text name="${selectedResourceTypes[0].localeKey}" />
                <#else>
                    <@s.text name="${selectedResourceTypes[0].pluralLocaleKey}" />
                </#if> 
            <#else>
                    <#if paginationHelper.totalNumberOfItems == 1>Resource<#else>Resources</#if>
            </#if>
                 within this Collection <#if selectedResourceTypes?has_content>                <sup><a style="text-decoration: "
                                                                                                             href="<@s.url includeParams="all">
                        <@s.param name="selectedResourceTypes"value="" />
                        <@s.param name="startRecord" value=""/>
            </@s.url>">[remove this filter]</a></sup>
            </#if>
            </h4>
            </#if>
        </div>

        <div class="tdarresults">
            <#assign itemsPerRow = 5 />
        <#if ((rightSidebar!false) || (leftSidebar!false)) >
            <#assign itemsPerRow = 4 />
		</#if>	



            <#nested />
            <@list.listResources resourcelist=results sortfield=sortField titleTag="h5" listTag="ul" itemTag="li" itemsPerRow=itemsPerRow
                    orientation=orientation    mapPosition="top" mapHeight=mapSize />
        </div>
            <@search.basicPagination "Records" />
        <#else>
        This collection is either empty or you do not currently have permissions to view the contents.
        </#if>
</#macro>

<#macro adminSection type="">
        <#if editable>
        <h3>Administrative Information</h3>

        <div class="row">
            <div class="span4">
                <@view.kvp key="Collection Type" val="${resourceCollection.type.label} ${type}" />
            </div>
            <div class="span4">
                <@view.kvp key="Hidden" val=resourceCollection.hidden?string />
            </div>
        </div>
        <div class="row">
            <div class="span4">
                <@view.kvp key="Sort By" val=resourceCollection.sortBy.label />
            </div>
            <div class="span4">
                <#assign viewed>${viewCount} times</#assign>
                <@view.kvp key="Viewed" val=viewed />
            </div>
        </div>
        <div class="row">
            <div class="span4">
                <@view.kvp key="Created By" nested=true><a
                        href="<@s.url value="${resourceCollection.owner.detailUrl}"/>">${resourceCollection.owner.properName}</a>
                    on ${resourceCollection.dateCreated?datetime}</@view.kvp>
            </div>
            <div class="span4">
                <@view.kvp key="Updated By" nested=true><a
                        href="<@s.url value="${resourceCollection.updater.detailUrl}"/>">${resourceCollection.updater.properName}</a>
                    on ${resourceCollection.dateUpdated?datetime}</@view.kvp>
            </div>
        </div>

            <@_authorizedUsers resourceCollection />
        </#if>

</#macro>

    <#macro javascript>
<script type='text/javascript'>
    $(document).ready(function () {
        TDAR.common.initializeView();
        TDAR.common.collectionTreeview();
    });
</script>
</#macro>


    <#macro _authorizedUsers collection >
        <@common.resourceCollectionsRights collections=collection.hierarchicalResourceCollections />
    </#macro>

</#escape>