<h2>Greetings new user!</h2>
It's nice to have you join ${siteAcronym}.

<p>Now that you've successfully registered an account with ${siteAcronym}. Here are some nice places to start:</p>
<ol>
<#if contributor>
    <#if config.payPerIngestEnabled>
        <li><a href="<@s.url value="/cart/add"/>">Charge your account</a>.</li>
    </#if>
    <li><a href="<@s.url value="/project/add"/>">Create a new project</a>. Projects in ${siteAcronym} are simple, easy ways to organize similar Documents,
        Images, and Datasets which share metadata.
    </li>
    <li><a href="<@s.url value="/resource/add"/>">Create a new resource</a>.</li>
</#if>
    <li><a href="${config.helpUrl}">Review the user's manual</a></li>
    <li><a href="<@s.url value="/dashboard" />">Visit your dashboard</a>.</li>
    <li><a href="<@s.url value="/search" />">Search ${siteAcronym}</a>.</li>
</ol>
