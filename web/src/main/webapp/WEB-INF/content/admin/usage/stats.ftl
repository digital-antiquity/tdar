<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "../admin-common.ftl" as admin>
<head>
    <title>Administrator Dashboard: Recent Activity</title>
    <meta name="lastModifiedDate" content="$Date$"/>
</head>
    <@admin.header/>

<h1>stats for (${dateStart!"last week"} - ${dateEnd!.now} );  Minimum Views: ${minCount} </h1>


<h2>Usage Stats</h2>
<table class="table table-sm table-striped"" id="tblUsageStats">
      <thead class="thead-dark">

    <tr>
        <th>resource</th>
        <th>views</th>
        <th>day</th>
    </tr>
    </thead>
    <tbody>
        <#list usageStats as stats>
        <tr>
            <td>
            <a href="<@s.url value="/${stats.resource.resourceType.urlNamespace}/${stats.resource.id?c}" />">${stats.resource.title}</a>
                (${stats.resource.id?c})
            </td>
            <td>${stats.count}</td>
            <td>${stats.aggregateDate}</td>
        </tr>
        </#list>
    </tbody>
</table>

<script>
    $(function () {
        $(".tableFormat").dataTable({"bFilter": false, "bInfo": false, "bPaginate": false});
    });
</script>

</#escape>
