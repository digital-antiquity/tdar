User-agent: *
<#if production>
Disallow: /search/*
Disallow: /search/
Disallow: /login
Disallow: /logout
Disallow: /search/rss
Disallow: /search/download
Disallow: /filestore/
Disallow: /cart/review
Disallow: /search/advanced
Disallow: /browse/creators/135028

<#else>
Disallow: *
</#if>
Sitemap: ${protocol}//${hostName}/sitemap/${sitemapFile}