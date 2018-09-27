<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "common-collection.ftl" as commonCollection>

<head>
    <@commonCollection.head />
</head>
<body>

    <@commonCollection.header />
        <@commonCollection.sidebar />

<div class="row">
<div class="col-12">
        <h1>${resourceCollection.name!"untitled collection"}     <@view.pageStatusCallout /></h1>
    </div>
    <#if !visible>
    This collection is not accessible
    
    <#else>
        <@commonCollection.toolbar />

        <@commonCollection.descriptionSection/>

        <@commonCollection.keywordSection />

        <@commonCollection.resultsSection/>

        <@commonCollection.adminSection/>
    </#if>

    <@commonCollection.javascript />
</div>
</body>

</#escape>