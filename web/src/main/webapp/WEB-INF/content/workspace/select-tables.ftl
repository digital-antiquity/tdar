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
                    <#if column.mappedOntology??>
                    ${column.mappedOntology.title}
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
<div class="glide">
    <h1>Data Integration</h1>

    <div class="row">
        <#if (bookmarkedDataTables)?? && !bookmarkedDataTables.empty >
            <div class="span12">
                <h3>Step 1: Select Datasets to Integrate or Display</h3>
                <@s.form name='selectDTForm' method='post' action='select-columns'>
                    <@s.token name='struts.csrf.token' />
                    <@listDatasets bookmarkedDatasets />

                    <div class="form-actions">
                        <@s.submit value='Next: select columns' cssClass="btn btn-primary submitButton"/>
                    </div>
                </@s.form>
            </div>
        </#if>
    </div>

    <div class="row">
        <div class="span6">
            <h3>Learn About Data Integration</h3>

            <p>${siteAcronym}'s revolutionary data integration tool is a simple interface to help you combine two or more disparate data sets into a single, new
                data set. The resulting data set can be easily downloaded and fed into SASS, SPSS, or R for analysis.</p>

            <h3>Getting Started</h3>

            <p>To get started, you either upload a data set to ${siteAcronym} or find data sets and bookmark them. You then ensure that at least one column from
                each data set is mapped to the same ontology. In ${siteAcronym},
                the key to integrating columns from different data sets is the ontology. It is a classification tool that helps reconcile different variable
                states from separate data sets. For example, an ontology might reconcile the values "chair" and "seat" as similar values, or it could even
                associate both values under the broader category "Furniture." </p>

            <p>After you have mapped data sets to ontologies, you can then filter the results to create the integrated data set. You simply select the
                columns you want to appear in the final set. You can read more about data integration <a
                        href="https://docs.tdar.org/display/TDAR/Data+Integration">here</a>.
            <ul>
                <li><a href="<@s.url value="/search/results?integratableOptions=YES&startRecord=0"/>">Find Data Sets</a></li>
                <li><a href="https://docs.tdar.org/display/TDAR/Bookmarking+a+Dataset+for+Data+Integration">How to Bookmark Data Sets</a></li>
            </ul>
        </div>
        <div class="span6">
            <img src="/images/r4/data_integration.png" class="responsive-image"/>
        </div>

    </div>
</body>

</#escape>
