<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
<head>
    <title>Edit Table Metadata for ${dataset.title}</title>
</head>
<body>
    <@edit.sidebar/>

<h1>Dataset Resource Mapping for ${dataset.title}</h1>

<h3>Table ${dataTable.displayName}, ${dataTable.dataTableColumns?size } columns</h3>



    <#if dataTable.dataTableColumns?has_content>
    <div id='subnavbar2' class="subnavbar" data-offset-top="250" data-spy="affix">
        <div class="navbar">
        <#-- <select name="chooseColumn" onChange="goToColumn(this)"> -->
            <div class="navbar-inner">
                <ul class="nav">
                    <li>
                        <a href="#top"><b>top</b></a>
                    </li>
                    <li>
<span style="display:inline-block">
                        <b style="margin-top: 10px !important;display: inline-block;margin-left: 10px;">Jump to a column</b>
<!--                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                        <b class="caret"></b> -->
                    <form class="inline" style="display:inline">
                        <select name="chooseColumn" id="chooseColumn" style="display: inline-block;margin-bottom: -1px;">
                            <#list dataTableColumns?sort_by("sequenceNumber") as column>
                                <option value="columnDiv_${column_index}">${column.displayName}</option>
                            </#list>
                        </select>
                    </form>
                    </span>
                    </li>
                </ul>
                <div id="fakeSubmitDiv" class="pull-right">
                    <button type=button class="button btn btn-primary submitButton" id="fakeSubmitButton">Save</button>
                    <img alt="progress indicator" title="progress indicator" src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="display:none"/>
                </div>
            </div>
        </div>
    </div>

    </#if>


    <@s.form method='post' id="edit-metadata-form" cssClass="form-horizontal"  action='save-column-mapping'>
        <@common.jsErrorLog />
        <@s.token name='struts.csrf.token' />
        <@s.hidden name='id' id="resource_id" value='${resource.id?c}'/>
        <@s.hidden name='dataTableId' value='${dataTable.id?c}'/>

        <div class="mappingDetail well">
            <p>${siteAcronym} can associate groups of documents and images with this dataset as long as they're all part of the same
                project.
                If this column has filenames in it, ${siteAcronym} will associate the filename with the filename of the image or document
                and load the row data as additional fields in the ${siteAcronym} record.
            </p>
		</div>

        <#if ( dataset.dataTables?size > 1 )>
        <h2>Column Description and Mapping: ${dataTable.displayName}</h2>
		
        <div class="">
            <p>
                There are multiple tables in this dataset. To switch between, click
                on one of the links below. Please remember to save any changes before switching tables.
            </p>

            <label for="table_select">Choose Table:</label>
            <select id="table_select" name="table_select">
                <#assign count=0>
                <@s.iterator value='%{dataset.dataTables}' var='table' status="status1">
                    <#assign count=count+1 />
                    <option value="${table.id?c}" <#if table?? && table.id == dataTable.id>selected</#if>>${count}. ${table.displayName}</option>
                </@s.iterator>
            </select>

        </div>
        </#if>


        <#if dataTable.dataTableColumns??>
        <div id="datatablecolumns">
            <#list dataTableColumns?sort_by("sequenceNumber") as column>
                <#if column_index != 0>
                    <hr/></#if>

                <div class="datatablecolumn" id="columnDiv_${column_index}" data-tooltipcontent="#mappingToolTip" data-tiplabel="Mapping ${siteAcronym} Resources">
                    <h3>
                        <span class="displayName">${column.displayName}</span>

                    <@s.hidden name="dataTableColumns[${column_index}].id" value="${column.id?c}" />

                        <@s.checkbox name="dataTableColumns[${column_index}].mappingColumn"
                    label="Use column values to map table rows to resources?"
                    id="mapping_${column_index}" cssClass="mappingValue" />

                        <@s.textfield name="dataTableColumns[${column_index}].delimiterValue" value="${column.delimiterValue!''}" placeholder="e.g. ; , |" label="Delimiter" labelposition="left" maxLength="1"/>
                        <#assign ignoreExt = "false" />
                        <#if column.ignoreFileExtension??>
   <#assign ignoreExt = column.ignoreFileExtension /></#if>
                        <@s.checkbox
                        name="dataTableColumns[${column_index}].ignoreFileExtension"
                        label="ignore file extension"
                        id="dataTableColumns[${column_index}].ignoreFileExtension" />
                </div>
            </#list>

            <div class="hidden" style="visibility:hidden;display:none">
    <span class="hidden" id="generalToolTip">
         Each "column" subform shown on the table metadata page represents a column in the dataset, and provides fields to describe the data in that column. This is important documentation for researchers that wish to use the dataset, and where relevant the form links to coding sheets and ${siteAcronym}
        ontologies to faciliate research.
    </span>
    <span class="hidden" id="columnTypeToolTip">
        Select the option that best describes the data in this column. The form will display fields relevant to your selection. <br/>
        <b>Note:</b> measurement and count cannot be selected for fields that have any non-numerical data.
    </span>
    <span class="hidden" id="displayNameToolTip">
        If needed, edit the name displayed for the column to help users understand the column's contents.
    </span>
    <span class="hidden" id="categoryVariableToolTip"> 
        Select the category and subcategory that best describes the data in this column.
    </span>
    <span class="hidden" id="descriptionToolTip">
        Add any notes that would help a researcher understand the data in the column. 
    </span>
    <span class="hidden" id="codingSheetToolTip">
        If the data in this column is coded and the right coding sheet has been added to ${siteAcronym}, please select a coding sheet that translates and explains the codes. 
    </span>
    <span class="hidden" id="ontologyToolTip">
        If you would like to link this column to a ${siteAcronym} ontology, make that selection here. This is important if you (or other researchers) intend to integrate this dataset with other datasets using the ${siteAcronym}
        data integration tool.
    </span>
    <span class="hidden" id="mappingToolTip">
        Use column values to map table rows to resources? - Select this check box if the data in the column links to a specific ${siteAcronym} resource, usually an image or document.
    </span>
            </div>

        </div>
        </#if>

        <@edit.submit "Save" false />

    </@s.form>


<script type='text/javascript'>

    $(function () {
        TDAR.datasetMetadata.init("#edit-metadata-form");

        $("#edit-metadata-form").FormNavigate("clean");
    });

</script>

</body>



</#escape>

