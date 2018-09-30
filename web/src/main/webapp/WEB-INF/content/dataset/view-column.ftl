<#if dataset?? && dataset.viewable >
<h1 class="view-page-title">Column information: ${dataTableColumn.displayName} of ${dataset.name}</h1>
<p><b>Table:</b> ${dataTableColumn.dataTable.displayName}<br>
<p><b>Dataset:</b> ${dataset.name}<br>

<#list dataTableColumn.values![]>
    <ul>
    <#items as value>
        <li>${value}</li>
    </#items>
    </ul>
</#list>

<#list dataTableColumn.intValues![]>
    <ul>
    <#items as value>
        <li>${value?c}</li>
    </#items>
    </ul>
</#list>

<#list dataTableColumn.floatValues![]>
    <ul>
    <#items as value>
        <li>${value?c}</li>
    </#items>
    </ul>
</#list>

</p>

</#if>