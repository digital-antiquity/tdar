<#escape _untrusted as _untrusted?html>
<head>
    <title>Data Integration: Select Tables</title>
</head>

<body>
<h1>Data Integration</h1>


    <div class="row">
        <div class="span9">
                <a href="<@s.url value="/workspace/integrate"/>" class="button tdar-button">Start a New Integration</a>
            <#list workflows?sort_by('dateUpdated')?reverse>
                <h3>Saved Integrations</h3>
            <table id="tblWorkflows" class="table table-bordered table-hover">
                <thead>
                    <tr>
                        <th>Name / Description</th>
                        <th>Last Update</th>
                        <th>Owner</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                	<#items as workflow>
                        <tr>
                            <td style="width:60%">
                                <a href="<@s.url value="/workspace/integrate/${workflow.id?c}"/>">${workflow.title!"untitled"} </a>
                                <p class="">${workflow.description!""}</p>
                            </td>
                            <td>${workflow.dateUpdated?string.short}</td>
                            <td>${workflow.submitter.properName}</td>
                            <td>
                            <#if workflow.editable>
                                <a class="btn btn-mini" href="<@s.url value="/workspace/settings/edit?id=${workflow.id?c}"/>">settings</a>
                                <a class="btn btn-mini" href="<@s.url value="/workspace/duplicate-confirm?id=${workflow.id?c}"/>">duplicate</a>
                                <a class="btn btn-mini" href="<@s.url value="/workspace/delete?id=${workflow.id?c}"/>">delete</a>
                            </#if>
                            </td>   
                        </tr>
                     </#items>
                </tbody>
            </table>

            <#else>
                <@learn />
            </#list>
        </div>
        <div class="span3">
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
