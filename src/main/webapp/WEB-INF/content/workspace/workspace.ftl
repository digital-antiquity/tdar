<#escape _untrusted as _untrusted?html>
<head>
    <title>Data Integration: Select Tables</title>
</head>

<body>
<h1>Data Integration</h1>
<div class="well">
    <p>
        Please click the link below to start your dataset integration.
    </p>
        <a href="integrate">Start Now</a>

</div>


<div class="glide">

    <div class="row">
        <div class="span6">

<#if (workflows?size > 0) >
<h3>Saved Integrations</h3>
<ul>
<#list workflows as workflow>
    <li><a href="<@s.url value="/workspace/integrate/${workflow.id?c}"/>">${workflow.title!"untitled"} - ${workflow.dateCreated?string.short}</a><br>${workflow.description!""}
        [<a href="<@s.url value="/workspace/delete?id=${workflow.id?c}"/>">delete</a>]</li>
</#list>
</ul>

<#else>
    <@learn />
</#if>
        </div>
        <div class="span6">
        <#if workflows?size != 0>
            <@learn />
        </#if>
            <img src="/images/r4/data_integration.png" class="responsive-image" alt="integrate" title="Integrate" />
        </div>
    </div>
    <div class="row">
		<div class="span12">
        <p><a href="/workspace/select-tables">Legacy Integration tool</a></p>
        </div>
    </div>
</body>

<#macro learn>
    <h3>About Data Integration</h3>

    <p>${siteAcronym}'s data integration tool is allows users to combine disparate data sets into a single, new
        data set. Results can be downloaded and fed into SASS, SPSS, or R for analysis.</p>

</#macro>
</#escape>
