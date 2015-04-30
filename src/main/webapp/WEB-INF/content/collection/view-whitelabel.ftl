<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/whitelabel-macros.ftl" as whitelabel>

<#--This is just an alias to help illustrate when we are using fields exclusive to whitelabel collections -->
    <#assign whitelabelCollection = resourceCollection>
<head>
    <meta name="decorator" content="whitelabel">
    <@search.headerLinks includeRss=false />
    <title>${resourceCollection.name!"untitled collection"}</title>
    <@view.canonical resourceCollection />
    <#assign rssUrl = "/search/rss?groups[0].fieldTypes[0]=COLLECTION&groups[0].collections[0].id=${resourceCollection.id?c}&groups[0].collections[0].name=${(resourceCollection.name!'untitled')?url}">
    <@search.rssUrlTag url=rssUrl />

    <style>
    <#noescape>${whitelabelCollection.css!''}</#noescape>
    </style>

</head>
<body>
<#-- todo: move these to .css and use collection-specific css classes -->
    <#if searchHeaderLogoAvailable>
    <style>
        div.searchheader {
            background-image: url("/hosted/search-header.jpg?id=${resourceCollection.id?c}");
        }
    </style>
    </#if>
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


    <#if !searchHeaderEnabled><h1>${resourceCollection.name!"untitled collection"}</h1></#if>

<#-- FIXME: have the controller handle isVisible via separate result name -->
    <#if !visible>
    This collection is not accessible
    <#else>


        <#if resourceCollection.parent??>
        <@whitelabel.subcollectionSidebar />
        </#if>

        <#if resourceCollection.parent?? || resourceCollection.description??  || resourceCollection.adminDescription?? || collections??>
        <div>
            <h2>Description</h2>
            <#if resourceCollection.parent??><p><b>Part of:</b> <a
                    href="${resourceCollection.parent.detailUrl}">${resourceCollection.parent.name!"(n/a)"}</a></p></#if>

            <div class="viewpage-section">
                <#-- TODO: move this logic to logoAvailable() -->
                <#if (logoAvailable && (resourceCollection.subCollection || !resourceCollection.whiteLabelCollection))>
                    <div class="pull-right"><img class="img-rounded whitelabel-logo" src="/files/collection/lg/${id?c}/logo" alt="logo" title="logo"> </div>
                </#if>
                <@common.description resourceCollection.description />

                <#if resourceCollection.adminDescription??>
                    <p>
                        <#noescape>
                        ${resourceCollection.adminDescription}
                    </#noescape>
                    </p>
                </#if>
            </div>
        </div>
        </#if>

        <#if whitelabelCollection.featuredResourcesEnabled>
            <div class="viewpage-section">

            <@view.featured resourceList=whitelabelCollection.featuredResources />
            </div>
        </#if>

        <#if whitelabelCollection.subCollectionsEnabled>
            <div class="viewpage-section">
                <h2>Collections</h2>
                <#list whitelabelCollection.transientChildren as childCollection>
                    <p>
                        <@s.a href="/collection/${childCollection.id?c}/${childCollection.slug}" cssClass="title"
                            >${childCollection.name}</@s.a>

                        ${common.fnTruncate(childCollection.description, 500)}
                    </p>
                </#list>
            </div>

        </#if>


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
            <#if (totalRecords > 10)>
                <#assign mapSize="700" />
            </#if>
            <#if (totalRecords > 18)>
                <#assign mapSize="1000" />
            </#if>
            <#if selectedResourceTypes.empty>
                <@search.facetBy facetlist=resourceTypeFacets currentValues=selectedResourceTypes label="" facetParam="selectedResourceTypes" />
            <#else>
            <h4>
                There <#if paginationHelper.totalNumberOfItems == 1>is<#else>are</#if> ${paginationHelper.totalNumberOfItems?c}

                <#if selectedResourceTypes?has_content>
                    <#if paginationHelper.totalNumberOfItems == 1>
                        <@s.text name="${resourceTypeFacets[0].key}" />
                    <#else>
                        <@s.text name="${resourceTypeFacets[0].pluralKey}" />
                    </#if>
                <#else>
                    <#if paginationHelper.totalNumberOfItems == 1>Resource<#else>Resources</#if>
                </#if> within this Collection <#if selectedResourceTypes?has_content>                <sup><a style="text-decoration: "
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
            <@list.listResources resourcelist=results sortfield=sortField titleTag="h5" listTag="ul" itemTag="li" itemsPerRow=itemsPerRow
                    orientation=resourceCollection.orientation    mapPosition="right" mapHeight=mapSize />
        </div>
            <@search.basicPagination "Records" />
        <#else>
        <hr/>
        This collection is either empty or you do not currently have permissions to view the contents.
        </#if>
        <#if editable>
        <h3>Administrative Information</h3>

            <@common.resourceUsageInfo />
        <div class="row">
            <div class="span4">
                <@view.kvp key="Collection Type" val="${resourceCollection.type.label} (white label)" />
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
    </#if>

<script type='text/javascript'>
    $(document).ready(function () {
        TDAR.common.initializeView();
        TDAR.common.collectionTreeview();
    });
</script>


</body>

    <#macro _authorizedUsers collection >
        <@common.resourceCollectionsRights collections=collection.hierarchicalResourceCollections />
    </#macro>

</#escape>