<!--
vim:sts=2:sw=2:filetype=jsp
-->
<head>
<title>Account Information for ${person.properName} with ${siteName}</title>
<meta name="lastModifiedDate" content="$Date$"/>
<script type='text/javascript'>
</script>
</head>
<body>
<div class="hero-unit">
<h1>Welcome, ${person.properName}  </h1>
<p>Now that you've successfully registered an account with ${siteAcronym}.  Here are some nice places to start:</p>
<ol>
<#if contributor>
    <li><a href="<@s.url value="/project/add"/>">Create a new project</a>.  Projects in ${siteAcronym} are simple, easy ways to organize similar Documents, Images, and Datasets which share metadata.</li>
    <li><a href="<@s.url value="/resource/add"/>">Create a new resource</a>.</li>
</#if>
    <li><a href="${helpUrl}">Review the user's manual</a></li>
    <li><a href="<@s.url value="/dashboard" />">Visit your dashboard</a>.</li>
    <li><a href="<@s.url value="/search" />">Search ${siteAcronym}</a>.</li>
</ol>

</div>

<div class="well">
<h2>Account Details</h2>
<dl class="dl-horizontal">
    <dt>Name:</dt>
    <dd>${person.properName}</dd>
    
    <dt>Email:</dt>
    <dd>${person.email!"Not Shown"}</dd>
    
    <dt>Institution:</dt>
    <dd>${person.institution!"Not Provided"}</dd>

    <#if person.contributorReason??>
    <dt>Requested contributor access for the following reason(s):</dt>
    <dd>${person.contributorReason}</dd>
    </#if>
    
    <#if RPAEnabled>
    <dt>RPA?</dt>
    <dd>${person.rpaNumber!""}</dd>
    </#if>
</dl>
<a class="btn btn-primary" href="<@s.url value='/entity/person/edit?id=${sessionData.person.id?c}'/>">Edit your Account Settings</a>
</div>
</body> 
