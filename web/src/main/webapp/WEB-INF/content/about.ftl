<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "/WEB-INF/settings.ftl" as settings>

<head>
    <title>Welcome to ${siteName}</title>
    <script type="application/ld+json">
{
   "@context": "http://schema.org",
   "@type": "WebSite",
   "url": "<@s.url value="/"/>",
   "potentialAction": {
     "@type": "SearchAction",
     "target": "${baseUrl}/search/results?query={search_term_string}",
     "query-input": "required name=search_term_string"
   }
}
</script>
</head>
<body>
<#escape _untrusted as _untrusted?html >
<#-- <div class="row">
    <div class="pricing">
        <div class="col-lg-1 col-md-1 col-sm-1 center">
        
            <a href="<@s.url value="/document"/>">
                <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_document"></use></svg>
            </a>
        </div>
        <div class="col-lg-1 col-md-1 col-sm-1 center">
            <a href="<@s.url value="/dataset"/>">
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_dataset"></use></svg>
            </a>
        </div>
        <div class="col-lg-1 col-md-1 col-sm-1 center">
            <a href="<@s.url value="/image"/>">
                <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_image"></use></svg>
            </a>
        </div>
        <div class="col-lg-1 col-md-1 col-sm-1 center">
            <a href="<@s.url value="/sensory-data"/>">
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_sensory_data"></use></svg>
            </a>
        </div>
        <div class="col-lg-4 col-md-4 col-sm-4 center" style="margin-top: 10px;">
            <#if config.payPerIngestEnabled >
                <a href="<@s.url value="/cart/add"/>" class="button">Archive a file for as low as $5</a>
            </#if>
        </div>
        <div class="col-lg-1 col-md-1 col-sm-1 center">
            <a href="<@s.url value="/ontology"/>">
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_ontology"></use></svg>
            </a>
        </div>
        <div class="col-lg-1 col-md-1 col-sm-1 center">
            <a href="<@s.url value="/geospatial"/>">
                <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_geospatial"></use></svg>
            </a>
        </div>
        <div class="col-lg-1 col-md-1 col-sm-1 center">
            <a href="<@s.url value="/project"/>">
                <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_project"></use></svg>
            </a>
        </div>
        <div class="col-lg-1 col-md-1 col-sm-1 center">
            <a href="<@s.url value="/collection"/>">
                <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_collection"></use></svg>
            </a>
        </div>
    </div>
</div> -->

<section class = "" id="pricing">
    <div class="container mb-4 d-none d-lg-block">
        <div class="row d-none d-lg-flex text-center justify-content-center">
            <div class="col-lg-1">
                <a href="<@s.url value="/document"/>">
                    <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_document"></use></svg>
                </a>
            </div>
            <div class="col-lg-1 text-center">
                <a href="<@s.url value="/dataset"/>">
                    <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_dataset"></use></svg>
                </a>
            </div>
            <div class="col-lg-1 text-center">
                <a href="<@s.url value="/image"/>">
                    <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_image"></use></svg>
                </a>
            </div>
            <div class="col-lg-1 text-center">
                <a href="<@s.url value="/sensory-data"/>">
                    <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_sensory_data"></use></svg>
                </a>
            </div>
            <#if config.payPerIngestEnabled >
                <div class="col-lg-4 text-center">
                    <a href="<@s.url value="/cart/add"/>" class="btn btn-secondary btn-block">tDAR Pricing Information</a>
                </div>
            </#if>
            <div class="col-lg-1 text-center">
                <a href="<@s.url value="/ontology"/>">
                    <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_ontology"></use></svg>
                </a>
            </div>
            <div class="col-lg-1 text-center">
                <a href="<@s.url value="/geospatial"/>">
                    <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_geospatial"></use></svg>
                </a>
            </div>
            <div class="col-lg-1 text-center">
                <a href="<@s.url value="/project"/>">
                    <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_project"></use></svg>
                </a>
            </div>
            <div class="col-lg-1 text-center">
                <a href="<@s.url value="/collection"/>">
                    <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_collection"></use></svg>
                </a>
            </div>
        </div>
        <hr>
    </div>

    <div class="container d-block d-lg-none">
        <div class="row">
            <div class="col-3 text-center">
                <a href="<@s.url value="/document"/>">
                    <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_document"></use></svg>
                </a>
            </div>
            <div class="col-3 text-center">
                <a href="<@s.url value="/dataset"/>">
                    <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_dataset"></use></svg>
                </a>
            </div>
            <div class="col-3 text-center">
                <a href="<@s.url value="/image"/>">
                    <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_image"></use></svg>
                </a>
            </div>
            <div class="col-3 text-center">
                <a href="<@s.url value="/sensory-data"/>">
                    <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_sensory_data"></use></svg>
                </a>
            </div>
        </div>
        <#if config.payPerIngestEnabled >
            <div class="col-lg-4 text-center my-4">
                <a href="<@s.url value="/cart/add"/>" class="btn btn-secondary btn-block">tDAR Pricing Information</a>
            </div>
        </#if>
        <div class="row">
            <div class="col-3 text-center">
                <a href="<@s.url value="/ontology"/>">
                    <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_ontology"></use></svg>
                </a>
            </div>
            <div class="col-3 text-center">
                <a href="<@s.url value="/geospatial"/>">
                    <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_geospatial"></use></svg>
                </a>
            </div>
            <div class="col-3 text-center">
                <a href="<@s.url value="/project"/>">
                    <svg class="svgicon svg-small red"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_project"></use></svg>
                </a>
            </div>
            <div class="col-3 text-center">
                <a href="<@s.url value="/collection"/>">
                    <svg class="svgicon svg-small black"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_collection"></use></svg>
                </a>
            </div>
        </div>
        <hr>
    </div>
</section>

<div class="row">
    <#include "featured.ftl" />
</div>
<hr>

<#-- FAIMS want this row disabled as they are unhappy with the map quality 
     and the RSS feed is broken on their side. I'm sure that its just a temporary state of affairs
     until they find someone to bring it up to their standards, and they fix their RSS feed. -->
<#if !config.archiveFileEnabled>
    
    <div class="row">
            <@commonr.renderWorldMap />
        <div class="col-12 col-md-6 news">
    
            <h3><a href="${config.newsUrl}">What's New at ${siteAcronym}?</a></h3>
    
            <#if rssEntries?has_content>
                <ul>
                    <#assign maxEntries =6 />
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
                <p class="pull-right"><a href="${config.newsUrl}">Read More &raquo;</a></p>
            </#if>
        </div>
    </div>
</#if>

<hr />
<div class="row">
    <div class="col-12 col-md-6 mt-2">
		<#if featuredCollection?has_content>
            <@commonr.featuredCollection featuredCollection />
		<#else>
        <h3>Getting Started with ${siteAcronym}</h3>
        <ul class = "list-group list-group-flush">
            <li class = "list-group-item" ><a href="${config.documentationUrl}">a tutorial that can help you get started</a>.</li>
            <li class = "list-group-item" ><a href="<@s.url value="/search/results?query=&resourceTypes=PROJECT"/>">browse</a> all projects</li>
            <li class = "list-group-item" ><a href="<@s.url value="/browse/collections"/>">browse</a> all collections</li>
            <li class = "list-group-item" ><a href="<@s.url value="/browse/explore"/>">explore</a> ${siteAcronym} by keyword</li>
        </ul>
        <!--    <a href="<@s.url value="/register"/>" class="button">Register</a> or <a href="<@s.url value="/login"/>" class="button">Log in</a> -->
		</#if>
    </div>
    <div class="col-md-6">
        <@commonr.resourceBarGraph />
    </div>
</div>
</#escape>
</body>
