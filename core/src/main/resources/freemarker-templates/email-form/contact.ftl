<#import "../email-macro.ftl" as mail /> 

<@mail.content>
Dear ${to.properName},
<p>
${from.properName} discovered <a href="${baseUrl}${resource.detailUrl}">${resource.title}</a> (${resource.id?c})  in ${siteAcronym}, 
and is interested in getting in touch with you.  They have sent the following 
message:
</p>

<blockquote>
    ${message}
</blockquote>

<p>
    You may to correspond with ${from.properName} via ${from.email}. 
</p>

<p>
To view the record in ${siteAcronym} visit: 
<a href="${baseUrl}${resource.detailUrl}">${baseUrl}${resource.detailUrl}</a>
</p>

<p>
    Kind regards,
</p>

<p>
Staff at ${serviceProvider}<br />
<br />
<hr/>
<br />
Note: please do not reply to this automated email
</p>

</@mail.content>