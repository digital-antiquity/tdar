<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/${config.themeDir}/settings.ftl" as settings>

<title>Explore ${siteAcronym}</title>

<#escape _untrusted as _untrusted?html >
<h1>Explore ${siteAcronym}</h1>

<h2>Browse Resources by Title</h2>
<ul class="inline">
    <#list alphabet as letter>
	     <@searchFor "groups[0].startingLetter" letter letter "li"/>
	 </#list>
</ul>
<br/>
<br/>

<h2>Resources by ${siteAcronym} Year</h2>

<div>
    <ul class="inline">
        <#list scholarData?sort_by("key") as key>
            <#assign tdarYear = key.key?substring(3) />
            <li class="bullet"><a href="<@s.url value="/scholar/scholar?year=${key.key?c}"/>">${key.key?c}</a></li>
        </#list>

    </ul>
    <br/>
</div>
<br/>

<div class="row">    
        <@commonr.renderWorldMap mode='vertical'/>
</div>

<div class="row">
    <div class="span12">
         <@commonr.resourceBarGraph /> 
    </div>
</div>

<h2>Browse By Decade</h2>

<div id="timelineGraph" style="height:400px" data-source="#timelineData" class="areaChart" data-x="label" data-values="count" data-yAxis="log">

</div>


<div class="row">
    <div class="span6">
        <h2>Most Popular in the Past Week</h2>
        <ul>
            <#list featuredResources as resource>
                <li><a href="<@s.url value="${resource.detailUrl}"/>">${resource.title}</a></li>
            </#list>
        </ul>
    </div>
    <div class="span6">
        <h2>Recently Added Resources</h2>
        <ul>
            <#list recentResources as resource>
                <#if resource??>
                    <li><a href="<@s.url value="${resource.detailUrl}"/>">${resource.title}</a></li>
                </#if>
            </#list>
        </ul>
    </div>
</div>


<h2>Browse by Investigation Type</h2>
<ul class="inline">
    <#list investigationTypes?sort as keyword>
        <@search.searchFor keyword=keyword asList=true showOccurrence=true />
     </#list>
</ul>

<h2>Browse by Site Type</h2>
<ul class="inline">
    <#list siteTypeKeywords?sort as keyword>
        <@search.searchFor keyword=keyword asList=true showOccurrence=true />
     </#list>
</ul>

<h2>Browse by ${config.culturalTermsLabel!"Culture"}</h2>
<ul class="inline">
    <#list cultureKeywords?sort as keyword>
        <@search.searchFor keyword=keyword asList=true showOccurrence=true />
     </#list>
</ul>


<h2>Browse by Material Type</h2>
<ul class="inline">
    <#list materialTypes?sort as keyword>
        <@search.searchFor keyword=keyword asList=true showOccurrence=true />
     </#list>
</ul>

    <#macro searchFor queryParam term displayTerm wrappingTag="span" occurrence=0>
        <#local term_ = term />
        <#if term?is_number>
            <#local term_ = term?c/>
        </#if>
    <${wrappingTag} class="bullet"><a href="<@s.url value="/search/results?${queryParam}=${term_}&explore=true"/>">${displayTerm}
        <#if occurrence?has_content && occurrence != 0 >(${occurrence?c})</#if>
    </a></${wrappingTag}>
    </#macro>
</#escape>

<script id="timelineData">
${timelineData}
</script>
