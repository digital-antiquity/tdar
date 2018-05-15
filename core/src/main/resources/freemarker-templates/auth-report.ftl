<#import "email-macro.ftl" as mail /> 
<@mail.content>
User ${log.userDisplayName} performed a dedupe operation.<br />

<p>
Authority: ${log.authority} [id:${log.authority.id?c}]<br />
Record Type: ${className}<br />
Records Updated: ${numUpdated}<br />
Dedupe Mode: ${log.dupeMode}<br />
 </p>
 
 <p>
 <hr />
    Duplicates
 <hr />
    <ul>
        <#list log.dupes as dup>
        <li>${dup.id?c}: ${dup}</li>
        </#list>
    </ul>
</p>

<p>
<hr />
 Records Merged
<hr />
<ul>
<#list referrers as referrer>
 <li> ${referrer}</li>
 <li> ${referrer.key.id?c} : ${referrer.key} [${referrer.value}] </li>
</#list>
</ul>
</p>
</@mail.content>