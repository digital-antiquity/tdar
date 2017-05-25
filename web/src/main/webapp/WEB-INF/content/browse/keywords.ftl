<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    
<head>
    
<title>${keyword.label}</title>

    <@view.canonical keyword />
<#--    <#assign rssUrl = "/api/search/rss?groups[0].fieldTypes[0]=COLLECTION&groups[0].collections[0].id=${resourceCollection.id?c}&groups[0].collections[0].name=${(resourceCollection.name!'untitled')?url}">
    <@search.rssUrlTag url=rssUrl /> -->
    <@search.headerLinks includeRss=false />

    <link rel="alternate" href="/api/lod/keyword/${id?c}?type=${keywordType}" type="application/ld+json" />    

</head>
<div id="sidebar-right" parse="true">
    <div class="sidebar-spacer">
       <#if geoJson?has_content>
           <@common.renderWorldMap mode="mini" extra="#localGeoJson">
        <script type="application/json" data-mapdata>
            <#noescape>{"geographic.ISO,resourceType":[]}</#noescape>
        </script>
           </@common.renderWorldMap>
      </#if>

        <#list keyword.assertions>
        <h4>External Relationships</h4>
        <ul>
            <#items as item>
                <li><a href="${item.relation}">${item.label!item.relation}</a> (${item.relationType.term}<#if item.relationHost?has_content> - ${item.relationHost!''}</#if>)</li>
            </#items>        
        </ul>
        <#else>
        </#list>

        <@search.facetBy facetlist=facetWrapper.facetResults['resourceType']![]  currentValues=resourceTypes label="Resource Type(s)" facetParam="resourceTypes" liCssClass="" ulClass="unstyled" pictoralIcon=true />
        
    </div>
</div>
        <@nav.keywordToolbar "view" />

<div class="glide">
    <h1>${keyword.label} <span class="xsmall red">(<@s.text name="${keywordType.localeKey}"/>)</span></h1>
    <#if keyword.synonyms?has_content>
    <p><#list keyword.synonyms![] as synonym><#if synonym_index !=0>, </#if>${synonym.label} </#list></p>
    </#if>
    <#if keyword.parent?has_content>
    <p><b>Parent:</b><@common.searchFor keyword.parent false /></p>
    </#if>
    
    <p>${keyword.definition!''}</p>
</div>

    <#if ( results?? && results?size > 0) >
        <div id="divResultsSortControl">
            <div class="row">
                <div class="span4">
                    <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Record"  />
                </div>
            </div>
        </div>
        <div class="tdarresults">
            <@list.listResources resourcelist=results  listTag="span" itemTag="span" titleTag="h3" orientation=orientation mapPosition="top" mapHeight="450"/>
        </div>
    
    </#if>

        <@search.basicPagination "Results"/>

<#if geoJson?has_content><#noescape>
<script id="localGeoJson"  type="application/json">
${geoJson}
</script>
</#noescape></#if>

<script type='text/javascript'>
    $(document).ready(function () {
        TDAR.common.initializeView();
    });
</script>

</#escape>