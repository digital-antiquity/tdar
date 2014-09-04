<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/${themeDir}/settings.ftl" as settings>

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
    <div class="span6">
        <@common.resourceBarGraph />
    </div>
    <div class="span6 map">
        <!-- fixme styling -->
        <h3 style="">${siteAcronym} Worldwide</h3>
        <@common.renderWorldMap />
    </div>
</div>

<h2>Browse By Decade</h2>
<script>
    var timelineData = [];
        <#list timelineData as cache>
            <#if (cache.count > 1 && cache.key != 0 && cache.key < 2500 )>
                <#noescape>
                timelineData.push(["${cache.key?c}",${(cache.count!0)?string("##0")}, "${cache.key?c}",${(cache.count!0)?string("##0")}]);
                </#noescape>
            </#if>
        </#list>

    var timelineConfig = {
        seriesDefaults: {
            rendererOptions: {
                varyBarColor: false
            }
        }
    };
</script>
    <@common.barGraph data="timelineData" graphLabel="" graphHeight=354 searchKey="groups[0].creationDecades" id="browseByDecade" config="timelineConfig" rotate=-30  yaxis="log"/>


<div class="row">
    <div class="span6">
        <h2>Most Popular in the Past Week</h2>
        <ul>
            <#list featuredResources as resource>
                <li><a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></li>
            </#list>
        </ul>
    </div>
    <div class="span6">
        <h2>Recently Added Resources</h2>
        <ul>
            <#list recentResources as resource>
                <#if resource??>
                    <li><a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></li>
                </#if>
            </#list>
        </ul>
    </div>
</div>


<h2>Browse by Investigation Type</h2>
<ul class="inline">
    <#list investigationTypes?sort as keyword>
        <@_searchFor keyword />
<#--         <@searchFor "groups[0].investigationTypeIdLists[0]" investigationType.id investigationType.label "li" investigationType.occurrence /> -->
     </#list>
</ul>

<h2>Browse by Site Type</h2>
<ul class="inline">
    <#list siteTypeKeywords?sort as keyword>
        <@_searchFor keyword />
<#--         <@searchFor "groups[0].approvedSiteTypeIdLists[0]" keyword.id keyword.label "li" keyword.occurrence /> -->
     </#list>
</ul>

<h2>Browse by ${culturalTermsLabel!"Culture"}</h2>
<ul class="inline">
    <#list cultureKeywords?sort as keyword>
        <@_searchFor keyword />
<#--         <@searchFor "groups[0].approvedCultureKeywordIdLists[0]" keyword.id keyword.label "li" keyword.occurrence /> -->
     </#list>
</ul>


<h2>Browse by Material Type</h2>
<ul class="inline">
    <#list materialTypes?sort as keyword>
        <@_searchFor keyword />
<#--          <@searchFor "groups[0].materialKeywordIdLists[0]" keyword.id keyword.label "li" keyword.occurrence /> -->
     </#list>
</ul>

    <#macro _searchFor keyword>
        <li class="bullet">
            <a href="<@s.url value="/${keyword.urlNamespace}/${keyword.id?c}" />">${keyword.label}
            <#if keyword.occurrence?has_content && keyword.occurrence != 0 >(${keyword.occurrence?c})</#if>
            </a>
        </li>
    </#macro>

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

