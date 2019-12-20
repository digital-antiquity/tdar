<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
    <#import "/WEB-INF/macros/helptext.ftl" as  helptext>
<head>
    <title>Edit Table Metadata for ${dataset.title}</title>
</head>
<body>
    <@edit.sidebar>
    

    <!--TODO: .container sets content width, it should be outside of grid layout  (or a grid-layout parent) -->
    <!-- we break this rule so that navbar will be correct with when it is .affix'd, for all responsive profiles -->
        <#-- <select name="chooseColumn" onChange="goToColumn(this)"> -->
                <ul class="nav">
                    <li class="page-item">
                        <a href="#top"><b>top</b></a>
                    </li>
                    <li class="page-item">
        <#if dataTable.dataTableColumns?has_content>
<span style="display:inline-block">
                        <b style="margin-top: 10px !important;display: inline-block;margin-left: 10px;">Jump to a column</b>
                    <form class="inline" style="display:inline">
                        <select name="chooseColumn" id="chooseColumn" style="display: inline-block;margin-bottom: -1px;">
                            <#list dataTableColumns?sort_by("sequenceNumber") as column>
                                <option value="columnDiv_${column_index}">${column.displayName}</option>
                            </#list>
                        </select>
                    </form>
                    </span>
    </#if>
                    </li>
                    <li class="page-item">
                                                    <label>Records Per Page
                                    <@s.select  theme="simple" id="recordsPerPage1" cssClass="input-small" name="recordsPerPage1"
                                    list={"10":"10", "25":"25", "50":"50"} listKey="key" listValue="value" />
                                </label>
                                <script type='text/javascript'>
                                $(function () {
                                    TDAR.datasetMetadata.initPagination("1");
                                });
                                </script>

                    </li>
                </ul>

        
    
        <div id="fakeSubmitDiv">
            <button type=button class="button btn btn-primary submitButton" id="fakeSubmitButton">Save</button>
            <img alt="progress indicator" title="progress indicator" src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="display:none"/>
        </div>
    </@edit.sidebar>

<h1>Edit Table Metadata for ${dataset.title}</h1>

<h3>Table ${dataTable.displayName}, ${dataTable.dataTableColumns?size } columns</h3>

                    <@pagination "1"/>


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

        <#macro pagination prefix>
            <#if (paginationHelper.pageCount > 1)>
            <div class="">
                <b>Showing ${recordsPerPage} columns, jump to another page? (save first)</b>
                <br/>
                <#assign path="/">
                <#if (paginationHelper.totalNumberOfItems >0)>
                                <ul class="pagination">
                            <#if paginationHelper.hasPrevious()>
                                    <@paginationLink startRecord=paginationHelper.previousPageStartRecord path=path linkText="Previous" />
                            </#if>
                                    <#if (0 < paginationHelper.minimumPageNumber) >
                                            <@paginationLink startRecord=0 path=path linkText="First" />
                                        <li "page-item">...</li>
                                    </#if>
                                    <#list paginationHelper.minimumPageNumber..paginationHelper.maximumPageNumber as i>
                                        <li "page-item">
                                            <#if i == paginationHelper.currentPage>
                                                <li class="page-item active">
												      <a class="page-link currentResultPage" href="#">${i + 1} <span class="sr-only"></span></a>
    											</li>
                                            <#else>
                                                <@paginationLink startRecord=(i * paginationHelper.itemsPerPage) path=path linkText=(i + 1) />
                                            </#if>
                                        </li>
                                    </#list>
                                    <#if (paginationHelper.maximumPageNumber < (paginationHelper.pageCount - 1))>
                                        <li "page-item">...</li>
                                        <@paginationLink startRecord=paginationHelper.lastPage path=path linkText="Last" />
                                    </#if>
                            <#if (paginationHelper.hasNext()) >
                                    <@paginationLink startRecord=paginationHelper.nextPageStartRecord path=path linkText="Next" />
                            </#if>
                                </ul>
                </#if>
            </div>

            </#if>
        </#macro>

        <#macro paginationLink startRecord path linkText>
        <li class="page-item">
    		<a  class="page-link" href="<@s.url includeParams="none" value="${actionName}?startRecord=${startRecord?c}&recordsPerPage=${recordsPerPage}&id=${id?c}"/><#if dataTableId?has_content>&dataTableId=${dataTableId?c}</#if>">${linkText}</a>
   		 </li>
        </#macro>

        <#if dataTable.dataTableColumns??>
        <div id="datatablecolumns" class="row">
            <#list dataTableColumns?sort_by("sequenceNumber") as column>
                <#if column_index != 0>
                    <hr/></#if>

                <div class="datatablecolumn col-12" id="columnDiv_${column_index}">

                    <div class="card mb-3">
                        <div class="card-body">

                    <h3 class="displayName card-title">
                        <span class="columnSquare">&nbsp;</span>
                        ${column.displayName} <@helptext.info title="Column Mapping Instructions" contentDiv="#generalToolTip" /></h3>

                    <div class="form-group row">
                        <label class="col-form-label col-2"><span data-tooltipcontent="#columnTypeToolTip" data-tiplabel="Column Type">Column Type:</span></label>
                        <@s.radio name='dataTableColumns[${column_index}].columnEncodingType'  inline=true
                        cssClass="columnEncoding" target="#columnDiv_${column_index}"
                        listValue='label' emptyOption='false' list='%{allColumnEncodingTypes}' theme=""/>
                    </div>


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
                            cssClass="categorySelect col-4 mr-4"
                            listKey='id'
                            listValue='name'
                            list='%{allDomainCategories}'
                            autocompleteName="sortCategoryId"
                            value="${categoryId}"
                            />

                                <#if subCategoryId != "">
                                    <@s.select  target="#columnDiv_${column_index}"
                                    id='subcategoryId_${column_index}'
                                    cssClass="subcategorySelect col-4 ml-5"
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
                            <img alt="progress indicator" title="progress indicator" src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="visibility:hidden"/>
                        </div>
                        </div>
                    </div>



                    <div class="form-group row" id="divColumnVisibility[${column_index?c}]">
                                <label for="columnVisibility_${column_index}" class="col-form-label col-2">Search Visibility:</label>
                                <@s.select
                                    name='dataTableColumns[${column_index}].visible'
                                    target="#columnDiv_${column_index}"
                                    id='columnVisibility_${column_index}'
                                    cssClass="form-control col-9"
                                    list={"HIDDEN":"Hidden", "CONFIDENTIAL":"Confidential - only seen by authorized users", "VISIBLE":"Visible"}
                                    listKey='key'
                                    listValue='value'
                                    emptyOption='false'
                                    label="Visibility"
                                    value="dataTableColumns[${column_index}].visible"
                                    theme="simple"
                                    />
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

                    </div>
                </div>

            </#list>

	<@helptext.columninfo />

        </div>
        </#if>
    <h2>Summary</h2>
    <table id="summaryTable" class="table table-sm table-striped">
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

