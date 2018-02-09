<#import "../email-macro.ftl" as mail /> 

<@mail.content>
Dear ${to.properName},
<p>
${from.properName} wants to suggest an edit or correction to the resource:
 ${resource.title} (${resource.id?c}) that you have administrative rights 
 to in tDAR.
</p>
---
<p>
${message}
</p>
---
<p>
You may correspond with ${from.properName} via ${from.email}.  To make 
edits to your ${siteAcronym} resource, log in to ${siteAcronym} and visit 
<a href="${baseUrl}/${resource.urlNamespace}/${resource.id?c}">
${baseUrl}/${resource.urlNamespace}/${resource.id?c}</a>.  
Select the edit tab at the top of the page, make any changes, and press save. 
</p>
<p>
Kind regards,<br />
<br />
Staff at ${serviceProvider}
</p>
<p>
---------<br />
Note: please do not reply to this automated email
</p>
</@mail.content>