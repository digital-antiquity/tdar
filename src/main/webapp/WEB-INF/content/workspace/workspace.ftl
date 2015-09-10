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
        <a href="<@s.url value="/workspace/integrate"/>">Start Now</a>

</div>


<div class="glide">

    <div class="row">
        <div class="span8">
            <#if (workflows?size > 0) >
            <h3>Saved Integrations</h3>
            <table class="table table-bordered table-hover">
                <thead>
                    <tr>
                        <th>Name / Description</th>
                        <th>Date</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <#list workflows as workflow>
                        <tr>
                            <td style="width:60%">
                                <a href="<@s.url value="/workspace/integrate/${workflow.id?c}"/>">${workflow.title!"untitled"} </a>
                                <p class="">${workflow.description!""}</p>
                            </td>
                            <td>${workflow.dateCreated?string.short}</td>
                            <td>
                                <a class="btn btn-mini" href="<@s.url value="/workspace/delete?id=${workflow.id?c}"/>">delete</a>
                            </td>   
                        </tr>
                    </#list>
                </tbody>
            </table>

            <#else>
                <@learn />
            </#if>
        </div>
        <div class="span4">
        <#if workflows?size != 0>
            <@learn />
        </#if>
            <img src="<@s.url value="/images/r4/data_integration.png"/>" class="responsive-image" alt="integrate" title="Integrate" />
        </div>
    </div>
</body>

<#macro learn>
    <h3>About Data Integration</h3>

    <p>${siteAcronym}'s data integration tool is allows users to combine disparate data sets into a single, new
        data set. Results can be downloaded and fed into SASS, SPSS, or R for analysis.</p>

</#macro>
</#escape>
