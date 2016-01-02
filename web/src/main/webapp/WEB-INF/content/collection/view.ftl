<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "common-collection.ftl" as commonCollection>

<head>
    <@commonCollection.head />
</head>
<body>

<div id="divSearchContext" parse="true">
    <input id="cbctxid" type="checkbox" name="collectionId" value="${id?c}">
    <label for="cbctxid">Search within this collection</label>
</div>

    <@commonCollection.header />
<h1>
    <#if logoAvailable>
        <img class="pull-right collection-logo" src="/files/collection/sm/${id?c}/logo"
        alt="logo" title="logo" /> 
    </#if>

${resourceCollection.name!"untitled collection"}</h1>
	visible: ${visible?c} hidden:${resourceCollection.hidden?c}  
    <#if !visible>
    This collection is not accessible
    <#else>

        <#if !collections.empty>
        <!-- Don't show header if header doesn't exist -->
        <div id="sidebar-right" parse="true">
            <h3 class="sidebar-spacer">Child Collections</h3>
            <@common.listCollections collections=collections showOnlyVisible=true />
        </div>
        </#if>


        <@commonCollection.descriptionSection/>

        <@commonCollection.resultsSection/>

        <@commonCollection.adminSection/>
    </#if>

    <@commonCollection.javascript />


</body>

</#escape>