<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "common-collection.ftl" as commonCollection>
    <#import "/WEB-INF/macros/whitelabel-macros.ftl" as whitelabel>

<#--This is just an alias to help illustrate when we are using fields exclusive to whitelabel collections -->
    <#assign whitelabelCollection = resourceCollection >
<head>
    <@commonCollection.head />
    <style>
    <#noescape>${whitelabelCollection.properties.css!''}</#noescape>

	<#-- todo: move these to .css and use collection-specific css classes -->
    <#if searchHeaderLogoAvailable>
        div.searchheader {
            background-image: url("${hostedContentBaseUrl}/search-header.jpg");
              background-position: center 0;
        }
    <#elseif searchHeaderEnabled>
        div.searchheader {
		    background: url(/images/r4/bg-home.jpg);
			background-repeat: no-repeat;
            background-position: center 0;
          }
    </#if>
    </style>
    <!-- search header url: "${hostedContentBaseUrl}/search-header.jpg" -->

</head>
<body>
    <@commonCollection.header />

    <#if !searchHeaderEnabled><h1>${resourceCollection.name!"untitled collection"}</h1></#if>

<#-- FIXME: have the controller handle isVisible via separate result name -->
    <#if !visible>
    This collection is not accessible
    <#else>


        <@commonCollection.sidebar />

        <@commonCollection.descriptionSection />
                <#-- TODO: move this logic to logoAvailable() -->

        <#if whitelabelCollection.properties.featuredResourcesEnabled>
            <div class="viewpage-section">
                <div class="row">
                    <@view.featured colspan="9" resourceList=whitelabelCollection.properties.featuredResources />
                </div>
            </div>
        </#if>

        <@commonCollection.keywordSection />

        <#if whitelabelCollection.properties.subCollectionsEnabled>
            <div class="viewpage-section">
                <h2>Collections</h2>
                <#list collections as childCollection>
                    <p>
                        <@s.a href="/collection/${childCollection.id?c}/${childCollection.slug}" cssClass="title">${childCollection.name}</@s.a>
						<#assign  descr = childCollection.description />
						<#if (childCollection.description?trim?index_of("\n") > 0)>
							<#assign  descr = childCollection.description?trim?keep_before("\n") />
						</#if>
                        ${common.fnTruncate(descr!'', 500)}
                    </p>
                </#list>
            </div>

        </#if>


        <@commonCollection.resultsSection>
            <#assign itemsPerRow = 4/>
        <#assign mapPosition="top"/>
        <#if collections.empty>
            <#assign itemsPerRow = 5 />
            <#assign mapPosition="left"/>
        </#if>
        </@commonCollection.resultsSection>


        <@commonCollection.adminSection type="(white label)"/>
    </#if>

    <@commonCollection.javascript />


</body>


</#escape>