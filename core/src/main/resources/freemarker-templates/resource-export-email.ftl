Dear ${authenticatedUser.firstName},
  We have completed your resource export request.  You can download the requested files (listed below) here: ${url}.  This link will be valid for 24 hours.

<#list resources as resource>
 - ${resource.title} (${resource.id})
</#list>