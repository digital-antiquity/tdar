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
<#escape _untrusted as _untrusted?html >
<hr>
<div class="row">
    <div class="pricing">
        <div class="span1 center">
            <a href="<@s.url value="/search/results?_tdar.searchType=simple&resourceTypes=DOCUMENT"/>"><h3 class="document-mid-black red"></h3></a>
        </div>
        <div class="span1 center">
            <a href="<@s.url value="/search/results?_tdar.searchType=simple&resourceTypes=DATASET"/>"><h3 class="dataset-mid-red red"></h3></a>
        </div>
        <div class="span1 center">
            <a href="<@s.url value="/search/results?_tdar.searchType=simple&resourceTypes=IMAGE"/>"><h3 class="image-mid-black red"></h3></a>
        </div>
        <div class="span1 center">
            <a href="<@s.url value="/search/results?_tdar.searchType=simple&resourceTypes=SENSORY_DATA"/>"><h3 class="sensory_data-mid-red red"></h3></a>
        </div>
        <div class="span4 center" style="margin-top: 10px;">
            <#if payPerIngestEnabled >
                <a href="http://www.tdar.org/about/pricing/" class="button">tDAR Pricing Information</a>
            </#if>
        </div>
        <div class="span1 center">
            <a href="<@s.url value="/search/results?_tdar.searchType=simple&resourceTypes=ONTOLOGY"/>"><h3 class="ontology-mid-red red"></h3></a>
        </div>
        <div class="span1 center">
            <a href="<@s.url value="/search/results?_tdar.searchType=simple&resourceTypes=GEOSPATIAL"/>"><h3 class="geospatial-mid-black red"></h3></a>
        </div>
        <div class="span1 center">
            <a href="<@s.url value="/search/results?_tdar.searchType=simple&resourceTypes=PROJECT"/>"><h3 class="project-mid-red red"></h3></a>
        </div>
        <div class="span1 center">
            <a href="<@s.url value="/search/collections"/>"><h3 class="collection-mid-black red"></h3></a>
        </div>
    </div>
</div>
<hr>

<div class="row">
    <#include "featured.ftl" />
</div>
<hr>

<#-- FAIMS want this row disabled as they are unhappy with the map quality 
     and the RSS feed is broken on their side. I'm sure that its just a temporary state of affairs
     until they find someone to bring it up to their standards, and they fix their RSS feed. -->
<#-- But beware: archiveFileEnabled is in danger of becoming "is FAIMS" -->
<#if !archiveFileEnabled>
    
    <div class="row">
        <div class="span6 map">
            <h3>${siteAcronym} Worldwide</h3>
            <@common.renderWorldMap />
        </div>
        <div class="span6 news">
    
            <h3>What&rsquo;s New at ${siteAcronym}?</h3>
    
            <#if rssEntries?has_content>
                <ul>
                    <#assign maxEntries =5 />
                    <#list rssEntries as entry>
                        <#assign maxEntries = maxEntries -1 />
                        <#if maxEntries == 0>
                            <#break>
                        </#if>
                        <li>
                            <span>${entry.publishedDate?string("MMM")?upper_case}<em>${entry.publishedDate?string("dd")}</em></span>
                            <a href="${entry.link}" class="title">${entry.title}</a>
                            Posted by ${entry.author}
                        </li>
                    </#list>
                </ul>
                <p class="pull-right"><a href="${newsUrl}">Older News &raquo;</a></p>
            </#if>
        </div>
    </div>
</#if>
<br/>

<div class="row">
    <div class="span6">
        <#include "/${themeDir}/homepage-bottom-left.dec" />
    </div>
    <div class="span6">
        <@common.resourceBarGraph />
    </div>
</div>
</#escape>
</body>
