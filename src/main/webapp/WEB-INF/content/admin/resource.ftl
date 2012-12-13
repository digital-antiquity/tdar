<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "admin-common.ftl" as admin>
<title>Admin Pages</title>

<@admin.header/>


<@admin.statsTable historicalResourceStats "Resource Statistics" "resourceStats" />

<@admin.statsTable historicalCollectionStats "Collection Statistics" "collectionStats" />


<div class="glide">
    <h3># of Files by extension</h3>
    <@common.pieChart extensionStats "extensions" "" 600 300 />
</div>

<div class="glide">
    <h3>File Usage by extension</h3>
    <table class="tableFormat">
     <tr>
      <th>Extension</th>
      <th>Min</th>
      <th>Max</th>
      <th>Average</th>
     </tr>
     <#list fileAverageStats?keys?sort as stat>
     <tr>
       <td><b>${stat}</b></td>
       <td><@common.convertFileSize fileAverageStats.get(stat)[0] /></td>
       <td><@common.convertFileSize fileAverageStats.get(stat)[1] /></td>
       <td><@common.convertFileSize fileAverageStats.get(stat)[2] /></td>
     </tr>
     </#list>
     </table>
</div>
</#escape>
