<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/decorators/decorator-macros.ftl" as decorator>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "common-collection.ftl" as commonCollection>
    <#import "/WEB-INF/macros/vue-search-macros.ftl" as vue>

<#if (whiteLabelCollection)>
<#--This is just an alias to help illustrate when we are using fields exclusive to whitelabel collections -->
    <#assign whitelabelCollection = resourceCollection >
</#if>

<head>
    <@commonCollection.head />
    
<#if (whiteLabelCollection)>
        <style>
        
        #hero-wide + .container {
        	margin-top:3rem;

		}
    <#noescape>${whitelabelCollection.properties.css!''}</#noescape>

    <#-- todo: move these to .css and use collection-specific css classes -->
        .whitelabelImageClass {
    <#if searchHeaderLogoAvailable>
            background-image: url("${hostedContentBaseUrl}/search-header.jpg");
			background-position: center;
			background-repeat: no-repeat;
	}
    <#elseif searchHeaderEnabled>
            background:url(/images/r4/bg-home.jpg) no-repeat center;
    </#if>
          }
          
  #hero-wide {padding-bottom:2rem}
    </style>
</#if>    
</head>
<body>
    <@commonCollection.header />
    <@commonCollection.sidebar />

<div class="row">
<div class="col-12">
    <#if !searchHeaderEnabled>
        <h1>${resourceCollection.name!"untitled collection"}     <@view.pageStatusCallout /></h1>
    </#if>
    </div>
    <#if !visible>
    This collection is not accessible
    
    <#else>
        <@commonCollection.toolbar />

        <@commonCollection.descriptionSection/>

        <#if whitelabelCollection?has_content && whitelabelCollection.properties.featuredResourcesEnabled>
            <@view.featured colspan="12" resourceList=whitelabelCollection.properties.featuredResources />
            <div class="col-12">
                <div class="row">
                </div>
            </div>
        </#if>


        <div class="col-12">
        <div class="row">
        <@commonCollection.keywordSection />
        </div>
        </div>


        <#if whitelabelCollection?has_content && whitelabelCollection.properties.subCollectionsEnabled>
            <div class="col-12">
                <#list collections>
                <h2>Collections</h2>
                <#items as childCollection>
                    <p>
                        <@s.a href="/collection/${childCollection.id?c}/${childCollection.slug}" cssClass="title">${childCollection.name}</@s.a>
                        <#assign  descr = childCollection.description />
                        <#if (childCollection.description?trim?index_of("\n") > 0)>
                            <#assign  descr = childCollection.description?trim?keep_before("\n") />
                        </#if>
                        ${common.fnTruncate(descr!'', 500)}
                    </p>
                </#items>
                </#list>
            </div>

        </#if>
        
        <@commonCollection.resultsSection/>

        <@commonCollection.adminSection/>
    </#if>

    <@commonCollection.javascript />
<script id="datasetinfo" data-dataset-id="<#if resourceCollection.dataset?has_content>${resourceCollection.dataset.id?c}</#if>"></script>
<#noescape>
    <#if datasetMapped>
    <script id="searchinfo" type="application/json">${jsonSearchInfo}</script>
    <script id="refinesearchinfo" type="application/json">${jsonRefineSearchInfo}</script>
    </#if>

</#noescape>
</div>
<script>
    //FIXME: this hack is necessary because the vuejs package names don't follow the TDAR.pkg.main convention
    $(function() {
        TDAR.vuejs.advancedSearch.main();
    });
</script>
</body>
</#escape>