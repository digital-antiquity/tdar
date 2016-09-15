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

        <h1>${resourceCollection.name!"untitled collection"}
			<#if (logoAvailable && (resourceCollection.description!'')?length < 150)>
		        <img class="pull-right collection-logo" src="/files/collection/lg/${id?c}/logo" alt="logo" title="logo" />
		    </#if>
		</h1>
		<#if ( logoAvailable && (resourceCollection.description!'')?length > 150)>
	        <img class="pull-right collection-logo" src="/files/collection/lg/${id?c}/logo" alt="logo" title="logo" />
        </#if>

    <#if !visible>
    This collection is not accessible
    <#else>

        <@commonCollection.sidebar minimal=true />

        <@commonCollection.descriptionSection/>

        <@commonCollection.resultsSection header="Resources in this Share"/>

        <@commonCollection.adminSection/>
    </#if>

    <@commonCollection.javascript />

</body>

</#escape>