User ${user} performed a dedupe operation.

Authority: ${authority} ${authority.id?c}
Record Type: ${className}
Records Updated: ${numUpdated}

========================================================
records merged
========================================================

<#list referrers?values as referrer>
 - ${referrer}
</#list>