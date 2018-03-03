<#import "email-macro.ftl" as mail /> 

<@mail.content>
Hello ${user.firstName},<br />
<br />
You have successfully signed up to use ${config.siteAcronym}<br />
<br />
We are very happy to have you join our community.<br />
<br/>
Your user name is: ${user.username}<br/>
The sites URL is: ${config.baseUrl}<br/>
<br/>
<br/>
You can:
<ul>
<li> Read the documentation at: ${config.documentationUrl}</li>
<li> Use the RSS feed: ${config.newsRssFeed}</li>
<li> And report defects at: ${config.bugReportUrl}</li>
</ul>
<br/>
If you need more help, there are dedicated sections on
<ul>
<li>Cultural terms: ${config.culturalTermsHelpURL}</li>
<li>Investigation types: ${config.investigationTypesHelpURL}</li>
<li>Material types: ${config.materialTypesHelpURL}</li>
<li>And site types: ${config.siteTypesHelpURL}</li>
</ul>

If you'd like to contact us, simple send an email message to: ${config.contactEmail}<br/>
<br/>
Thanks for signing up!<br/>
<br/>
The team at ${config.siteName}
</@mail.content>