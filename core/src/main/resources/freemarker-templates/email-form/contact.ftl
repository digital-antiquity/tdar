<#import "../email-macro.ftl" as mail /> 

<@mail.content>
Dear ${to.properName},
<p>
${from.properName} discovered <a href="${baseUrl}${resource.detailUrl}">${resource.title} (${resource.id?c})</a>  in ${siteAcronym}, 
and is interested in getting in touch with you.  They have sent the following 
message:
</p>

<p>
    ${message}
</p>

<p>
    You may to correspond with ${from.properName} via ${from.email}. 
</p>

<p>
To view the record in ${siteAcronym} visit: 
<a href="${baseUrl}/${resource.urlNamespace}/${resource.id?c}">
    ${baseUrl}/${resource.urlNamespace}/${resource.id?c}</a>
</p>

<p>
    Kind regards,
</p>

<p>
Staff at ${serviceProvider}<br />
<br />
---------<br />
Note: please do not reply to this automated email
</p>

</@mail.content>