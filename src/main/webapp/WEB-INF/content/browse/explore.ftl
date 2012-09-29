<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/${themeDir}/settings.ftl" as settings>

<title>Explore tDAR</title>
 
<#escape _untrusted as _untrusted?html >

 <h2>Browse Resources by Title</h2>
 	<ul>
 <#list alphabet as letter>
 	<@searchFor "groups[0].startingLetter" letter letter "span"/>
 </#list>
	</ul>

<h2>Browse By Decade</h2>
<@common.flotBarGraph resourceCacheObjects=timelineData graphWidth=900 graphHeight=300 graphLabel="tDAR by decade" rotateColors=false labelRotation=-90 minWidth=10 searchKey="groups[0].creationDecades" explore=true max=2100 min=1400 />

<h2>Browse by Investigation Type</h2>
<ul>
	 <#list investigationTypes as investigationType>
	 	<@searchFor "groups[0].investigationTypeIdLists[0]" investigationType.id investigationType.label "span"/>
	 </#list>
</ul>

<h2>Browse by Site Type</h2>
<ul>
	 <#list siteTypeKeywords as keyword>
	 	<@searchFor "groups[0].approvedSiteTypeIdLists[0]" keyword.id keyword.label "span"/>
	 </#list>
</ul>

<h2>Browse by Culture</h2>
<ul>
	 <#list cultureKeywords as keyword>
	 	<@searchFor "groups[0].approvedCultureKeywordIdLists[0]" keyword.id keyword.label "span"/>
	 </#list>
</ul>


<h2>Browse by Material Type</h2>
<ul>
	 <#list materialTypes as keyword>
	 	<@searchFor "groups[0].materialKeywordIdLists[0]" keyword.id keyword.label "span"/>
	 </#list>
</ul>
 

<#macro searchFor queryParam term displayTerm wrappingTag>
 	<${wrappingTag} class="bullet"><a href="<@s.url value="/search/results?${queryParam}=${term}&explore=true"/>">${displayTerm}</a></${wrappingTag}>
</#macro>
</#escape>

