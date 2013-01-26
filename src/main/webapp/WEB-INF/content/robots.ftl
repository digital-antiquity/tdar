User-agent: *
<#if production>
Disallow: /search/rss
Disallow: /search/download
Disallow: /filestore/
Disallow: /search/advanced
<#else>
Disallow: *
</#if>
Crawl-Delay: 10