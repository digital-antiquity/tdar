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

    <#if logoAvailable>
        <#if ((resourceCollection.description!'')?length > 150)>
        <h1>${resourceCollection.name!"untitled collection"}</h1>
        <img class="pull-right collection-logo" src="/files/collection/lg/${id?c}/logo"
        alt="logo" title="logo" />
        <#else>
        <h1>${resourceCollection.name!"untitled collection"}
        <img class="pull-right collection-logo" src="/files/collection/lg/${id?c}/logo"
             alt="logo" title="logo" />
        </h1>

        </#if>
    </#if>

    <#if !visible>
    This collection is not accessible
    <#else>

        <@commonCollection.sidebar />

        <@commonCollection.descriptionSection/>

        <@commonCollection.keywordSection />

        <@commonCollection.resultsSection/>

        <@commonCollection.adminSection/>
    </#if>

    <@commonCollection.javascript />

</body>

</#escape>