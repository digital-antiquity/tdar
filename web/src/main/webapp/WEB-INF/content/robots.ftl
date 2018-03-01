User-agent: *
<#if production && !hostName?contains("alpha") >
Disallow: /search/*
Disallow: /search/
Disallow: /search/results
Disallow: /login
Disallow: /login*
Disallow: /logout
Disallow: /logout*
Disallow: /api
Disallow: /account/new
Disallow: /search/rss
Disallow: /api/search/rss
Disallow: /search/download
Disallow: /filestore/
Disallow: /filestore/download/
Disallow: /filestore/download/*
Disallow: /resource/request/*
Disallow: /resource/request/
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
