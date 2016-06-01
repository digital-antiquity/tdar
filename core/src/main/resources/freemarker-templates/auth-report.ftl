User ${log.userDisplayName} performed a dedupe operation.

Authority: ${log.authority} [id:${log.authority.id?c}]
Record Type: ${className}
Records Updated: ${numUpdated}
Dedupe Mode: ${log.dupeMode}
 
========================================================
 Duplicates
========================================================
<#list log.dupes as dup>
 - ${dup.id?c}: ${dup}
</#list>

========================================================
 Records Merged
========================================================
<#list referrers as referrer>
 - ${referrer}
 - ${referrer.key.id?c} : ${referrer.key} [${referrer.value}]
</#list>
