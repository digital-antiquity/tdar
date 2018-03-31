<#import "email-macro.ftl" as mail /> 
<@mail.content>
Hello ${user.firstName},<br />
<br />
You have successfully signed up to use ${config.siteAcronym}<br />
<br />
We are very happy to have you join our community.<br />
<br/>
Your user name is: <b>${user.username}</b><br/>
<br/>
You can:
<ul>
    <li> <a href="${config.documentationUrl}">Read the documentation</a></li>
    <li> <a href="${config.newsRssFeed}">Use the RSS feed</a></li>
    <li> <a href="${config.bugReportUrl}">Report defects </a></li>
</ul>

<br/>

If you need more help, there are dedicated sections on
<ul>
    <li><a href="${config.culturalTermsHelpURL}">Cultural terms</a> </li>
    <li><a href="${config.investigationTypesHelpURL}">Investigation types</a></li>
    <li><a href="${config.materialTypesHelpURL}">Material types</a></li>
    <li><a href="${config.siteTypesHelpURL}">And site types</a></li>
</ul>

If you'd like to contact us, simple send an email message to: ${config.contactEmail}<br/>
<br/>
Thanks for signing up!<br/>
<br/>
The team at ${config.siteName}
</@mail.content>