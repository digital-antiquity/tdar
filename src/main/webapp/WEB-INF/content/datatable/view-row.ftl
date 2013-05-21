<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<h2>Row Level Page</h2>
<#if authenticatedUser??>
  <#if dataTableRowAsMap??>
  <!-- replace with reference to dataset -->
    <p><strong>Parent Dataset:</strong> ${datasetName}</p>
    <p><strong>Parent Description:</strong> ${datasetDescription}</p>
    <table class="table table-striped">
        <thead>  
          <tr>  
            <th>Field</th>  
            <th>Value</th>  
          </tr>  
        </thead>   
        <tbody>  
    <#list dataTableRowAsMap.entrySet() as entry>
    <!-- remove java methods and use freemarker conventions below -- entry.key.visible -->
        <#if entry.key.isVisible()>
          <tr>
            <td>${entry.key.getDisplayName()} </td><td>${entry.value} </td>
          </tr>
        </#if>
    </#list>
        </tbody>
    </table>
  </#if>

</#if>
