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
<#else>
Disallow: *
</#if>
Sitemap: ${protocol}//${hostName}/sitemap/${sitemapFile}