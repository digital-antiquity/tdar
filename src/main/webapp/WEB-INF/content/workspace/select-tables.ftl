<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>

<#macro listDatasets datasetList> 
            <table class="tableFormat ">
                <thead>
                    <tr>
                        <th></th>
                        <th>Active Dataset</th>
                        <th>Table</th>
                    <tr>
                </thead>
    <#list datasetList as dataset>
        <#if ! dataset.dataTables.isEmpty()>
            <tbody class="integrate_dataset" id="dataset_${dataset.id?c}">
                    <@listDataTables dataset />
            </tbody> 
        </#if>
    </#list>
            </table>
</#macro>

<#macro listDataTables dataset>
    <#if dataset?? && (!dataset.viewable?has_content || dataset.viewable)>
        <#list dataset.getDataTables() as table>
        <tr>
            <td>
                <@s.checkbox id="datatable_checkbox_${table.id?c}" name="tableIds" fieldValue="${table.id?c}"/>
            </td>
            <td><label class="datatableListItem" for="datatable_checkbox_${table.id?c}">${dataset.title}</label></td>
            <td>${table.displayName}
             &nbsp;(<a href="#" class="column_toggle" > show/hide columns</a>)            
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
                <th align="center"> Column name </th>
                <th align="center"> Column data type </th>
                <th align="center"> Attached ontology </th>
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
<meta name="lastModifiedDate" content="$Date$"/>
<script type='text/javascript'>
$(function() {
  $('.column_toggle').click(function() {
      $(this).next().toggle();
      return false;
  });
});
</script>
</head>

<body>
<div class="glide">

<#if bookmarkedDataTables.empty >
    <p>
    No tables have been bookmarked.  You can bookmark a table through <a
    href='<@s.url value="/search/basic"/>'>search</a> or through your <a
    href='<@s.url value="/dashboard"/>'>project list</a>.
    </p>
<#else>
    <h3>Step 1: Select Datasets to Integrate or Display</h3>
    <@s.form name='selectDTForm' method='post' action='select-columns'>
        <@listDatasets bookmarkedDatasets />

<div class="form-actions">
    <@s.submit value='Next: select columns' cssClass="btn btn-primary submitButton"/>
</div>
    </@s.form>
</#if>
</div>
</body>

</#escape>