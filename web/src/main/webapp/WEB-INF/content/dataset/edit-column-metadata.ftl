<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<head>
    <title>Edit Table Metadata for ${dataset.title}</title>
</head>
<body>
    <@edit.sidebar/>

<h1>Edit Table Metadata for ${dataset.title}</h1>

<h3>Table ${dataTable.displayName}, ${dataTable.dataTableColumns?size } columns</h3>


    <#if dataTable.dataTableColumns?has_content>

    <!--TODO: .container sets content width, it should be outside of grid layout  (or a grid-layout parent) -->
    <!-- we break this rule so that navbar will be correct with when it is .affix'd, for all responsive profiles -->
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

    <#if (dataset.dataTables?size > 1) >
        <div class="card">
            <div class="card-body">
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
        </div>
    </#if>

    <@s.form method='post' id="edit-metadata-form" cssClass="form-horizontal tdarvalidate"  dynamicAttributes={"data-validate-method":"initBasicForm"}  action='save-column-metadata'>
        <@common.jsErrorLog />
        <@s.token name='struts.csrf.token' />
        <@s.hidden name='id' id="resource_id" value='${resource.id?c}'/>
        <@s.hidden name='dataTableId' value='${dataTable.id?c}'/>
        <@s.hidden name="startRecord" value="${(startRecord!0)?c}" />
        <@s.hidden name="recordsPerPage" value="${(recordsPerPage!10)?c}" />
        <@edit.hiddenStartTime />
        <#if ( dataset.dataTables?size > 1 )>
        <h2>Description: ${dataTable.displayName}</h2>

        <@s.textarea name="tableDescription" label="Table Description" cssClass="resizable input-xxlarge" />

        <h2>Column Description &amp; Mapping</h2>

        </#if>
        <@pagination "1"/>

        <#macro pagination prefix>
            <#if (paginationHelper.pageCount > 1)>
            <div class="pagination">
                <b>Showing ${recordsPerPage} columns, jump to another page? (save first)</b>
                <#assign path="/">
                <#if (paginationHelper.totalNumberOfItems >0)>
                    <table class="pagin">
                        <tr>
                            <#if paginationHelper.hasPrevious()>
                                <td class="prev">
                                    <@paginationLink startRecord=paginationHelper.previousPageStartRecord path=path linkText="Previous" />
                                </td>
                            </#if>
                            <td class="page">
                                <ul>
                                    <#if (0 < paginationHelper.minimumPageNumber) >
                                        <li>
                                            <@paginationLink startRecord=0 path=path linkText="First" />
                                        </li>
                                        <li>...</li>
                                    </#if>
                                    <#list paginationHelper.minimumPageNumber..paginationHelper.maximumPageNumber as i>
                                        <li>
                                            <#if i == paginationHelper.currentPage>
                                                <span class="currentResultPage">${i + 1}</span>
                                            <#else>
                                                <@paginationLink startRecord=(i * paginationHelper.itemsPerPage) path=path linkText=(i + 1) />
                                            </#if>
                                        </li>
                                    </#list>
                                    <#if (paginationHelper.maximumPageNumber < (paginationHelper.pageCount - 1))>
                                        <li>...</li>
                                        <li>
                                            <@paginationLink startRecord=paginationHelper.lastPage path=path linkText="Last" />
                                        </li>
                                    </#if>
                                </ul>
                            </td>
                            <#if (paginationHelper.hasNext()) >
                                <td class="next">
                                    <@paginationLink startRecord=paginationHelper.nextPageStartRecord path=path linkText="Next" />
                                </td>
                            </#if>
                            <td>
                                <label>Records Per Page
                                    <@s.select  theme="simple" id="recordsPerPage${prefix}" cssClass="input-small" name="recordsPerPage${prefix}"
                                    list={"10":"10", "25":"25", "50":"50"} listKey="key" listValue="value" />
                                </label>
                                <script type='text/javascript'>
                                $(function () {
                                    TDAR.datasetMetadata.initPagination("${prefix}");
                                });
                                </script>
                            </td>
                        </tr>
                    </table>
                </#if>
            </div>

            </#if>
        </#macro>

        <#macro paginationLink startRecord path linkText>
        <span class="paginationLink">
    	<a href="<@s.url includeParams="none" value="${actionName}?startRecord=${startRecord?c}&recordsPerPage=${recordsPerPage}&id=${id?c}"/><#if dataTableId?has_content>&dataTableId=${dataTableId?c}</#if>">${linkText}</a>
    </span>
        </#macro>

        <#if dataTable.dataTableColumns??>
        <div id="datatablecolumns" class="row">
            <#list dataTableColumns?sort_by("sequenceNumber") as column>
                <#if column_index != 0>
                    <hr/></#if>

                <div class="datatablecolumn col-12" id="columnDiv_${column_index}">
                        <span id="columnDiv_${column_index}lgnd" data-tooltipcontent="#generalToolTip" data-tiplabel="Column Mapping Instructions"
                              class="columnSquare"><span>&nbsp;</span>
                    <h3 class="displayName">${column.displayName}</h3>

    <span data-tooltipcontent="#columnTypeToolTip" data-tiplabel="Column Type">
        <@s.radio name='dataTableColumns[${column_index}].columnEncodingType' label="Column Type:"
        cssClass="columnEncoding" target="#columnDiv_${column_index}" labelposition="left"
        listValue='label' emptyOption='false' list='%{allColumnEncodingTypes}'/>
    </span>
                    <@s.hidden name="dataTableColumns[${column_index}].id" value="${column.id?c}" />
                    <@s.hidden name="dataTableColumns[${column_index}].columnDataType" value="${column.columnDataType}" cssClass="dataType" />
                    <@s.hidden name="dataTableColumns[${column_index}].name" value="${column.name}" />
                    <span data-tooltipcontent="#displayNameToolTip" data-tiplabel="Display Name">
                        <@s.textfield name="dataTableColumns[${column_index}].displayName" value="${column.displayName}" label="Display Name:" cssClass="input-xxlarge" labelposition="left"/>
                    </span>

                    <div class="measurementInfo" style='display:none;'>
                        <@s.select name='dataTableColumns[${column_index}].measurementUnit' cssClass="measurementUnit" labelposition="left"
                        label="Meas. Unit:" listValue='fullName' emptyOption='true' list='%{allMeasurementUnits}'/>
                    </div>
                    <div data-tooltipcontent="#categoryVariableToolTip" data-tiplabel="Category Variable" class="form-group row">
                        <label class="col-form-label col-2">Category:</label>
                        <#assign subCategoryId="" />
                        <#assign categoryId="" />
                        <#if column.categoryVariable??>
                            <#assign subCategoryId="${column.categoryVariable.id?c}" />
                            <#if column.categoryVariable.parent??>
                                <#assign categoryId="${column.categoryVariable.parent.id?c}" />
                            <#else>
                                <#assign categoryId="${column.categoryVariable.id?c}" />
                            </#if>
                        </#if>
                        <div class="controls col-10">
                        <div class="row">
                            <@s.select id='categoryVariableId_${column_index}'
                            name='dataTableColumns[${column_index}].categoryVariable.id'
                            onchange='TDAR.common.changeSubcategory("#categoryVariableId_${column_index}","#subcategoryId_${column_index}")'
                            headerKey="-1"
                            headerValue=""
                            cssClass="categorySelect col-6"
                            listKey='id'
                            listValue='name'
                            list='%{allDomainCategories}'
                            autocompleteName="sortCategoryId"
                            value="${categoryId}"
                            />

                            <span id="subcategoryDivId_${column_index}">
                                <#if subCategoryId != "">
                                    <@s.select  target="#columnDiv_${column_index}"
                                    id='subcategoryId_${column_index}'
                                    cssClass="subcategorySelect col-6"
                                    name='dataTableColumns[${column_index}].tempSubCategoryVariable.id'
                                    list='%{subcategories[${column_index}]}'
                                    headerKey="-1"
                                    headerValue=""
                                    listKey='id'
                                    listValue='name'
                                    emptyOption='false'
                                    label="Subcategory:"
                                    autocompleteName="subCategoryId"
                                    value="${subCategoryId}"
                                    />
                                <#else>
                                    <select id='subcategoryId_${column_index}' name='dataTableColumns[${column_index}].tempSubCategoryVariable.id'
                                            class="col-6 form-control" autocompleteName="subCategoryId">
                                        <option value='-1'>N/A</option>
                                    </select>
                                </#if>
                            </span>
                            <img alt="progress indicator" title="progress indicator" src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="visibility:hidden"/>
                        </div>
                        </div>
                    </div>
    <span data-tooltipcontent="#descriptionToolTip" data-tiplabel="Column Description">
        <@s.textarea label="Column Description" name='dataTableColumns[${column_index}].description' rows='2' cols='12' cssClass="resizable input-xxlarge" />

    </span>

                    <div id='divCodingSheet-${column_index}' class="codingInfo" data-tooltipcontent="#codingSheetToolTip" data-tiplabel="Coding Sheet">
                        <#assign codingId="" />
            <#if column.defaultCodingSheet?? && column.defaultCodingSheet.id??>
                        <#assign codingId=column.defaultCodingSheet.id?c />
                    </#if>
            <@s.hidden id="${column_index}_cid" name="dataTableColumns[${column_index}].defaultCodingSheet.id" cssClass="codingsheetidfield" value="${codingId}" />
            <@commonr.combobox name="dataTableColumns[${column_index}].defaultCodingSheet.title"  target="#columnDiv_${column_index}"
                    label="Translate your data using a Coding Sheet:"
                    autocompleteParentElement="#divCodingSheet-${column_index}"
                    autocompleteIdElement="#${column_index}_cid"
                    placeholder="Enter the name of a Coding Sheet"
                    addNewLink="/coding-sheet/add?returnToResourceMappingId=${resource.id?c}"
                    cssClass="input-xxlarge-combo codingsheetfield" />
                    </div>
                    <div id='divOntology-${column_index}' class="ontologyInfo " data-tooltipcontent="#ontologyToolTip" data-tiplabel="Ontology">
                        <#assign ontologyId="" />
            <#if column.defaultOntology??  && column.defaultOntology.id?? && column.columnEncodingType != "CODED_VALUE" >
                        <#assign ontologyId=column.defaultOntology.id?c />
                    </#if>
            <@s.hidden name="dataTableColumns[${column_index}].transientOntology.id" value="${ontologyId}" id="${column_index}_oid" />
            <@commonr.combobox name="dataTableColumns[${column_index}].transientOntology.title" target="#columnDiv_${column_index}"
                    label="Map it to an Ontology:"
                    placeholder="Enter the name of an Ontology"
                    autocompleteParentElement="#divOntology-${column_index}"
                    autocompleteIdElement="#${column_index}_oid"
                    addNewLink="/ontology/add?returnToResourceMappingId=${resource.id?c}"
                    cssClass="col-10 ontologyfield" />
                    </div>
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
            </div>

        </div>
        </#if>
    <h2>Summary</h2>
    <table id="summaryTable" class="table tableFormat">
        <tr>
            <th></th>
            <th></th>
        </tr>
        <tr>
            <td><span class="columnSquare invalid">&nbsp;</span><span class="error_label"></span></td>
            <td>columns with errors</td>
        </tr>
        <tr>
            <td><span class="columnSquare measurement">&nbsp;</span><span class="measurement_label"></span></td>
            <td>measurment columns</td>
        </tr>
        <tr>
            <td><span class="columnSquare count">&nbsp;</span><span class="count_label"></span></td>
            <td>count columns</td>
        </tr>
        <tr>
            <td><span class="columnSquare coded">&nbsp;</span><span class="coded_label"></span></td>
            <td>coded columns</td>
        </tr>
        <tr>
            <td><span class="columnSquare uncoded">&nbsp;</span><span class="uncoded_label"></span></td>
            <td>uncoded columns</td>
        </tr>
        <tr>
            <td><span class="columnSquare integration">&nbsp;</span><span class="integration_label"></span></td>
            <td>integration columns</td>
        </tr>
    </table>

        <@pagination "2" />


        <@edit.submit "Save" false>
        <p>
            <@s.radio name="postSaveAction" listValue="label" emptyOption="false" list="%{allSaveActions}" numColumns=1
            cssClass="inline radio" theme="simple" />
            <br/>
            <br/>
        </p>
        </@edit.submit>

    </@s.form>


<script type='text/javascript'>

    $(function () {
        TDAR.datasetMetadata.init("#edit-metadata-form");

        $("#edit-metadata-form").FormNavigate("clean");
    });

</script>

</body>



</#escape>

