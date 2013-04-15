User ${user} performed a dedupe operation.

Authority: ${authority} [id:${authority.id?c}]
Record Type: ${className}
Records Updated: ${numUpdated}
 
========================================================
 Duplicates
========================================================
<#list dups as dup>
 - ${dup.id?c}: ${dup}
</#list>

========================================================
 Records Merged
========================================================
<#list referrers as referrer>
 - ${referrer}
 - ${referrer.key.id?c} : ${referrer.key} [${referrer.value}]
</#list>
