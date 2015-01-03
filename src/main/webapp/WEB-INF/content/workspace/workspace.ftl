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

<#list workflows as workflow>
<ul>
	<li><a href="<@s.url value="/workspace/integrate/${workflow.id?c}"/>">${workflow.title!"untitled"} - ${workflow.dateCreated?string.short}</a><br>${workflow.description!""} 
		[<a href="<@s.url value="/workspace/integrate/delete?id=${workflow.id?c}"/>">delete</a>]</li>
</ul>
</#list>


<div class="glide">

    <div class="row">
        <div class="span6">
            <h3>Learn About Data Integration</h3>

            <p>${siteAcronym}'s revolutionary data integration tool is a simple interface to help you combine two or more disparate data sets into a single, new
                data set. The resulting data set can be easily downloaded and fed into SASS, SPSS, or R for analysis.</p>
        </div>
        <div class="span6">
            <img src="/images/r4/data_integration.png" class="responsive-image" alt="integrate" title="Integrate" />
        </div>

    </div>
</body>

</#escape>
