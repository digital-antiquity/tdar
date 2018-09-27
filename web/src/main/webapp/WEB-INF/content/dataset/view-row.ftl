<#if dataset?? && dataset.viewable >
<h1 class="view-page-title">Row number ${rowId} of ${dataset.name}</h1>
<#-- At the moment no sidebar: we await further refinement of the users needs. -->
    <#if dataTableRowAsMap??>
    <p><strong>Dataset:</strong> <a href="<@s.url value="${dataset.detailUrl}"/>">${dataset.name}</a></p>
    <p><strong>Description:</strong> ${dataset.description}</p>
    <table class="table table-striped">
          <thead class="thead-dark">

        <tr>
            <th>Field</th>
            <th>Value</th>
        </tr>
        </thead>
        <tbody>
            <#list dataTableRowAsMap?keys as key>
                <#if key.visible>
                <tr>
                    <td>${key.displayName}</td>
                    <#-- dataTableRowAsMap(key) also works, for some reason -->
                    <td>${dataTableRowAsMap.get(key)} <#if (key.defaultOntology.id)?has_content>(<a
                            href="<@s.url value="/ontology/${key.defaultOntology.id?c}"/>">${key.defaultOntology.title}</a>)</#if></td>
                </tr>
                </#if>
            </#list>
        </tbody>
    </table>
    </#if>
<#else>
<#-- text of following is used for testing DatasetWebITCase#viewRowPageRender-->
<p class="alert">The view dataset row feature does not appear to be enabled.</p>
</#if>