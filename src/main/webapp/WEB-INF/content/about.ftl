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
<div class='post'>

<#if !sessionData?? || !sessionData.authenticated>
<div style="position:relative;display:block;clear:both;overflow:visible;height:250px">
</#if>
    <div class="glide       <#if !sessionData?? || !sessionData.authenticated>col66</#if>">
    <h3>About</h3>
    <#include "/${themeDir}notice.ftl">
    </div>

<#if !sessionData?? || !sessionData.authenticated>
    <div class="col33 glide">
    <h3>Login</h3>
     <@nav.loginForm />
    </div>
</div>
</#if>

<div class="clear"></div>

<div class="glide">
<h3>Search</h3>
<@s.form action="search/results" method="GET" id='searchForm' >
	<@search.queryField "Keyword"/>
</@s.form>
</div>
<div class="glide" style="position:relative;">
<h3>Explore</h3>
<@common.barGraph resourceCacheObjects=homepageResourceCountCache graphLabel="${siteAcronym} by the Numbers" graphHeight=354 />
<@common.worldMap />

</div>
<div class="glide">
	<div class="col50">
		<h3>Getting Started with ${siteAcronym}</h3>
		<ul>
		<li><a href="${documentationUrl}">a tutorial that can help you get started</a>.</li>
		<li> <a href="<@s.url value="/search/results?query=&resourceTypes=PROJECT"/>">browse</a> all projects</li>
		<li> <a href="<@s.url value="/browse/collections"/>">browse</a> all collections</li>
		<li> <a href="<@s.url value="/browse/explore"/>">explore</a> ${siteAcronym} by keyword</li>
		</ul>
	</div>
	<div class="col50">
		<#if featuredResource?? >
		<h3>Featured ${featuredResource.resourceType.label}</h3>
			<@view.tdarCitation featuredResource false />
		<#else>
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		<br/>
		</#if>
		<br/>
	</div>
</div>

</div>
</body>
