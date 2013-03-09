User ${user.properName} performed a dedupe operation.

Authority: ${authority}
Record Type: ${className}
Records Updated: ${numUpdated}

========================================================
records merged
========================================================

<#list referrers as referrer>
 - ${referrer}
</#list>