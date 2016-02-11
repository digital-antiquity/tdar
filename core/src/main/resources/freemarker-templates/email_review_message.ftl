The following ${totalEmails} user-generated emails need to be reviewed before they are sent.  Review online ( ${siteUrl}/admin/email ) here to review and change status in ${siteAcronym}.

<#list emails as email>
 - ${email.date?string.short} ${email.subject} from:${email.from} to: ${email.to}
</#list>