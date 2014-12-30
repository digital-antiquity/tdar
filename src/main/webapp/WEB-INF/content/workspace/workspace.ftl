<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#macro listDatasets datasetList>
    <table class="tableFormat table">
        <thead>
        <tr>
            <th></th>
            <th>Active Dataset</th>
            <th>Shared Ontologies</th>
            <th>Table</th>
        <tr>
        </thead>
        <#list datasetList as dataset>
            <#if dataset.dataTables?has_content>
                <tbody class="integrate_dataset" id="dataset_${dataset.id?c}">
                    <@listDataTables dataset />
                </tbody>
            </#if>
        </#list>
    </table>
    </#macro>

    <#macro listDataTables dataset>
        <#if dataset?? && (!dataset.viewable?has_content || dataset.viewable)>
            <#list dataset.dataTables as table>
            <tr>
                <td>
                    <@s.checkbox id="datatable_checkbox_${table.id?c}" name="tableIds" fieldValue="${table.id?c}" disabled="${((table.columnsWithOntologyMappings?size!0) == 0)?c }"/>
                </td>
                <td><label class="datatableListItem" for="datatable_checkbox_${table.id?c}">${dataset.title} - ${table.displayName}</label>
                    (Table from <a href="/${dataset.resourceType.urlNamespace}/${dataset.id?c}">${dataset.title} (${dataset.id?c})</a>)
                </td>
                <td>
                    <#local comma = false />
                    <#if sharedOntologies??>
                    <#list sharedOntologies as ontology>
                        <#local seen  = false>
                        <#list table.dataTableColumns as column>
                            <#local ontId = (column.mappedOntology.id)!-1 />
                            <#if ontId != -1>
                                <#if ontology.id == ontId>
                                    <#local seen = true />
                                </#if>
                            </#if>
                        </#list>
                        <#if seen>
                            <#if !comma><#local comma = true><#else>,</#if>
                        ${ontology.name}
                        </#if>
                    </#list>
                    </#if>
                </td>
                <td>
                    &nbsp;(<a href="#" class="column_toggle"> show / hide columns</a>)
                    <div class="datatable_columns" id="datatable_columns_${table.id?c}" style="display:none;">
                        <@listDataTableColumns table.getDataTableColumns() />
                    </div>
                </td>
            </tr>
            </#list>
        </#if>
    </#macro>

    <#macro listDataTableColumns tableColumnList>
    <table border="1" cellspacing="0" cellpadding="2" class="table table-striped">
        <thead>
        <tr>
            <th align="center"> Column name</th>
            <th align="center"> Column data type</th>
            <th align="center"> Attached ontology</th>
        </tr>
        </thead>
        <tbody>
            <#list tableColumnList as column>
            <tr>
                <td>${column.name}</td>
                <td>${column.columnDataType}</td>
                <td>
                    <#if column.defaultOntology??>
                    ${column.defaultOntology.title}
                    <#else>
                        None
                    </#if>
                </td>
            </tr>
            </#list>
        </tbody>
    </table>
    </#macro>

<head>
    <title>Data Integration: Select Tables</title>
    <script type='text/javascript'>
        $(function () {
            $('.column_toggle').click(function () {
                $(this).next().toggle();
                return false;
            });
        });
    </script>
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
	<li><a href="<@s.url value="/workspace/integrate/${workflow.id?c}"/>">${workflow.title!"untitled"} - ${workflow.dateCreated?string.short}</a><br>${workflow.description!""}</li>
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
