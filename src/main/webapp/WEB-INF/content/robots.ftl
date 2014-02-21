User-agent: *
<#if production>
Disallow: /search/*
Disallow: /search/
Disallow: /search/rss
Disallow: /search/download
Disallow: /filestore/
Disallow: /search/advanced
<#else>
Disallow: *
</#if>
Sitemap: ${protocol}//${hostName}/sitemap/${sitemapFile}