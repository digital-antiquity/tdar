User-agent: *
<#if production>
Disallow: /search/*
Disallow: /search/
Disallow: /search/results
Disallow: /login
Disallow: /logout
Disallow: /search/rss
Disallow: /search/download
Disallow: /filestore/
Disallow: /cart/review
Disallow: /search/advanced
Disallow: /resource/request/
Disallow: /resource/request/*
Disallow: /browse/creators/135028/
Disallow: /browse/creators/12729/

<#else>
Disallow: *
</#if>
Sitemap: ${protocol}//${hostName}/sitemap/${sitemapFile}

User-agent: msnbot 
Crawl-delay: 1
User-agent: bingbot 
Crawl-delay: 1

