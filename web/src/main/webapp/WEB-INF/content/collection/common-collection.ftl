<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/common-rights.ftl" as rights>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search-macros.ftl" as search>


<#macro head>
    <@search.headerLinks includeRss=false />
    <title>${resourceCollection.name!"untitled collection"}</title>
    <@view.canonical resourceCollection />
    <#assign rssUrl = "/api/search/rss?groups[0].fieldTypes[0]=COLLECTION&groups[0].collections[0].id=${resourceCollection.id?c}&groups[0].collections[0].name=${(resourceCollection.name!'untitled')?url}">
    <@search.rssUrlTag url=rssUrl />
    <link rel="alternate" href="/api/lod/collection/${id?c}" type="application/ld+json" />    

</#macro>

<#macro sidebar minimal=false>

    <!-- Don't show header if header doesn't exist -->
    <div id="sidebar-right" parse="true" class="row pl-3">
    	<div class="col-12">
        <#if !minimal>
            <#if (logoAvailable && ((resourceCollection.properties.whitelabel)!false || ((resourceCollection.properties.customHeaderEnabled)!false) == false)) >
                <img class="collection-logo" src="/files/collection/lg/${id?c}/logo" alt="logo" title="logo" />
            </#if>
            <#if results?has_content>
            <@commonr.renderWorldMap mode="mini" />
            <hr class="light"/>
                <@search.facetBy facetlist=resourceTypeFacets label="" facetParam="selectedResourceTypes" link=false liCssClass="" ulClass="list-unstyled" pictoralIcon=true />
    <i class="icon-document-red"></i>
            </#if>
		<#else>
		    <div class="beige white-border-bottom">
		        <div class="iconbox">
		            <svg class="svgicon white svg-dynamic"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_collection"></use></svg>
		        </div>
		    </div>
        </#if>
        <#if collections?has_content && !collections.empty  && !(resourceCollection.properties.hideCollectionSidebar)!false> 
            <h3>ChildCollections</h3>
            <@commonr.listCollections collections=collections showOnlyVisible=true />
        </#if>
		<@list.displayWidget />

            <hr/>
        <ul class="media-list ml-0 pl-0">
        <#if false >
            <li class="media"><i class="icon-envelope pull-left"></i>
            <div class="media-body">
                    <a id="requestAccess" href="/collection/request/${id?c}">Request Access, Submit Correction, Comment
            </a>
            </div>
            </li>
        </#if>
        <@nav.shareSection id=resourceCollection.id title=resourceCollection.name citation=resourceCollection.name />

        </ul>
            <hr/>

            <ul class="list-unstyled">
            <li>
                <strong>Submitter</strong><br>
                    <a href="<@s.url value="${resourceCollection.owner.detailUrl}"/>">${resourceCollection.owner.properName}</a>
            </li>
        <li>
            <strong>tDAR ID</strong><br>${id?c}
        </li>
    </ul>

    </div>
    </div>
</#macro>

<#macro header>

    <div id="divSearchContext" parse="true">
        <input id="cbctxid" type="checkbox" name="collectionId" value="${id?c}">
        <label for="cbctxid">Search within this collection</label>
    </div>
    



</#macro>

    <#macro _keywordSection label keywordList searchParam>
        <#list keywordList>
        <p class="break-word">
            <strong>${label}</strong><br>
            <#items as item>
                <a href="${item.detailUrl}">${item.label}</a><#sep> &bull;</#sep>
            </#items>
        </p>
        </#list>
    </#macro>

<#macro formatCollectionLink collection>
    <#if collection.hidden && !authenticated >
        ${collection.name!"(n/a)"}
    <#else>
     <a
        href="${collection.detailUrl}">${collection.name!"(n/a)"}</a>
    </#if>
</#macro>

<#macro descriptionSection>
        <#if resourceCollection.parent?? || resourceCollection.description??  || resourceCollection.formattedDescription?? || collections??>
        <div class="col-12">
            <#if resourceCollection.parent??><p><b>Part of:</b>
                    <@formatCollectionLink resourceCollection.parent />
                <#if resourceCollection.alternateParent?has_content>
                    <#assign alternate = resourceCollection.alternateParent>,
                    <@formatCollectionLink alternate />
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
            <!-- <div class="col-12">
            <div class="row"> -->
                <div class="col">
                <@_keywordSection "Site Name Keywords" facetWrapper.facetResults['activeSiteNameKeywords']![] "query" />
                <@_keywordSection "Site Type Keywords" facetWrapper.facetResults['activeSiteTypeKeywords']![] "query" />
                <@_keywordSection "Other Keywords" facetWrapper.facetResults['activeOtherKeywords']![] "query" />
                <@_keywordSection "Culture Keywords" facetWrapper.facetResults['activeCultureKeywords']![] "query" />
                </div>

                <div class="col">
                <@_keywordSection "Investigation Types" facetWrapper.facetResults['activeInvestigationTypes']![] "query" />
                <@_keywordSection "Material Types" facetWrapper.facetResults['activeMaterialKeywords']![] "query" />
                <@_keywordSection "Temporal Keywords" facetWrapper.facetResults['activeTemporalKeywords']![] "query" />
                <@_keywordSection "Geographic Keywords" facetWrapper.facetResults['activeGeographicKeywords']![] "query" />
                </div>
<!--            </div>
            </div> -->
            <hr/>
            </#if>
</#macro>
<#macro toolbar>
<div class="col-12">
    <#if editable>
    <#local path="${resourceCollection.urlNamespace}"/>
        <@nav.collectionToolbar "collection" "view">
            <@nav.makeLink2
            namespace="${path}"
            link="add?parentId=${id?c}"
            label="add child collection"
            disabled=disabled
            icon="add" />

            <@nav.makeLink2
                namespace="${path}"
                link="${id?c}/rights"
                label="permissions"
                icon="permissions"
                disabled=disabled/>

        <#if editor && ((resourceCollection.managedResources![])?size > 0) >
            <@nav.makeLink2
            namespace="${path}"
            link="admin/batch/${id?c}"
            label="batch title (beta)"
            icon="batch"
            disabled=disabled />
        </#if>
        </@nav.collectionToolbar>
    </#if>
</div>
</#macro>

<#macro resultsSection header="Inside This Collection">
<div class="col-12">
		<div class="">
	        <#if results?has_content>
    		    <div id="divResultsSortControl" class="">
                    <@search.totalRecordsSection tag="h2" helper=paginationHelper header=header/>
		        </div>
        
        <div class="collection-facets col-12">
            <#assign mapSize="450" />
			<@search.partFacet selectedResourceTypes paginationHelper "Collection" "h4" 'horizontal' />
        </div>

        <div class="tdarresults">
            <#assign itemsPerRow = 5 />
            <#if ((rightSidebar!false) || (leftSidebar!false)) >
                <#assign itemsPerRow = 4 />
            </#if>



            <#nested />
            <@list.listResources resourcelist=results sortfield=sortField titleTag="b"  headerTag="b" listTag="ul" itemTag="li" itemsPerRow=itemsPerRow
                    orientation=orientation    mapPosition="top" mapHeight=mapSize />
            </div>
            <@search.basicPagination "Records" />
        <#else>
        <div class="col-12">
        <hr>
        This collection is either empty or you do not currently have permissions to view the contents.
        <hr>
        </div> 
        </#if>
        </div> 
        </div> 
</#macro>

<#macro adminSection type="">
        <#if editable>
        <div class="col-12">
        <h3>Administrative Information</h3>
        <div class="row">
            <div class="col">
                <#local _type="Collection"/>
                <#if resourceCollection.properties.whitelabel>
                   <#local _type="Whitelabel"/>
                </#if>

                <@view.kvp key="Collection Type" val="${type} ${resourceCollection.systemManaged!false?string(' (System)', _type)}" />
            </div>
            <div class="col">
                <@view.kvp key="Hidden" val=resourceCollection.hidden?string />
            </div>
        </div>
        <div class="row">
            <div class="col">
                <@view.kvp key="Sort By" val=resourceCollection.sortBy.label />
            </div>
            <div class="col">
            </div>
        </div>
        <div class="row">
            <div class="col">
                <@view.kvp key="Created By" nested=true><a
                        href="<@s.url value="${resourceCollection.owner.detailUrl}"/>">${resourceCollection.owner.properName}</a>
                    on ${resourceCollection.dateCreated?datetime}</@view.kvp>
            </div>
            <div class="col">
                <@view.kvp key="Updated By" nested=true><a
                        href="<@s.url value="${resourceCollection.updater.detailUrl}"/>">${resourceCollection.updater.properName}</a>
                    on ${resourceCollection.dateUpdated?datetime}</@view.kvp>
            </div>
        </div>
        <div class="row">
            <@_authorizedUsers resourceCollection />
        </div>
        </div>
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
        <div class="col-12">
        <@rights.resourceCollectionsRights collections=collection.hierarchicalResourceCollections />
        </div>
    </#macro>

</#escape>