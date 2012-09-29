<@s.set name="theme" value="'bootstrap'" scope="request" />
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/${themeDir}/settings.ftl" as settings>

<head>
<title>Welcome to ${siteName}</title>
<meta name="lastModifiedDate" content="$Date: 2009-02-13 09:05:44 -0700 (Fri, 13 Feb 2009)$"/>
</head>
<body>
<div class="container">
<div class="row">
    <div class="<#if !sessionData?? || !sessionData.authenticated>span6</#if>">
    <h3>About</h3>
    <#include "/${themeDir}notice.ftl">
    </div>

    <#if !sessionData?? || !sessionData.authenticated>
        <div class="span3">
        <h3>Login</h3>
         <@nav.loginForm />
        </div>
    </#if>
</div>

<div class="row">
    <h3>Search</h3>
    <div class="span9">
    <@s.form action="search/results" method="GET" id='searchForm' cssClass="form-inline">
        <@search.queryField "Keyword"/>
    </@s.form>
    </div>
</div>

<div class="row">
    <h3>Explore</h3>
    <div class="span6">
        <@common.worldMap />
    </div>
    <div class="span3">
        <@common.barGraph resourceCacheObjects=homepageResourceCountCache graphLabel="${siteAcronym} by the Numbers" graphHeight=354 />
    </div>
</div>

<div class="row">
    <div class="span5">
        <#if featuredResource?? >
        <h3>Featured ${featuredResource.resourceType.label}</h3>
            <@view.tdarCitation featuredResource false />
        </#if>
    </div>
    <div class="span4">
        <h3>Getting Started with ${siteAcronym}</h3>
        <ul>
        <li><a href="${documentationUrl}">a tutorial that can help you get started</a>.</li>
        <li> <a href="<@s.url value="/search/results?query=&resourceTypes=PROJECT"/>">browse</a> all projects</li>
        <li> <a href="<@s.url value="/browse/collections"/>">browse</a> all collections</li>
        <li> <a href="<@s.url value="/browse/explore"/>">explore</a> ${siteAcronym} by keyword</li>
        </ul>
    </div>
</div>

</div>
</body>
