<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>

<#macro listDatasets datasetList> 
            <table class="tableFormat table">
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
<h1>Data Integration</h1>

<div class="row">
<#if !bookmarkedDataTables.empty >
<div class="span12">
    <h3>Step 1: Select Datasets to Integrate or Display</h3>
    <@s.form name='selectDTForm' method='post' action='select-columns'>
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
<p>tDAR's revolutionary data integration tool is designed to simplify the process to combine and reconcile two or more disparate data sets into a single, new data set, for computational analysis. The resulting data set can be easily downloaded and fed into SASS, SPSS, or R for additional flexibility.</p>
<h3>Getting Started</h3>
<p>To get started, you must first upload or find data sets and bookmark them.  Each data set must have at least one column mapped to the same shared ontology within ${siteAcronym}. The ontology enables different values such as <em>Chair</em> and <em>Seat</em> to be reconciled as similar, but also <em>Chair</em> and <em>Furniture</em> to be placed within a hierarchical relationship.</p>
<p>Once you have two or more data sets loaded, you may select columns to include within the final data set and then filter the results based on your ontologies.  You can read more about data integration <a href="https://dev.tdar.org/confluence/display/TDAR/Data+Integration">here</a>.
<ul>
	<li><a href="<@s.url value="/search/results.action?integratableOptions=YES&startRecord=0"/>">Find Data Sets</a></li>
	<li><a href="https://dev.tdar.org/confluence/display/TDAR/Bookmarking+a+Dataset+for+Data+Integration">Bookmark Data Sets</a></li>
</ul>
</div>
<div class="span6">
<img src="/images/r4/data_integration.png" class="responsive-image" />
</div>

</div>
</body>

</#escape>