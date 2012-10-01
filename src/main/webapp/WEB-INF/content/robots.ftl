User-agent: *
<#if production>
Disallow: /search/rss
Disallow: /search/download
Disallow: /filestore/
<#else>
Disallow: *
</#if>
Crawl-Delay: 10