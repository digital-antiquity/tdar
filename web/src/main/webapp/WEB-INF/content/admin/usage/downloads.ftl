<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "../admin-common.ftl" as admin>
<head>
    <title>Administrator Dashboard: Recent Activity</title>
    <meta name="lastModifiedDate" content="$Date$"/>
</head>
    <@admin.header/>

<h1>Download Stats for (${dateStart!"last week"} - ${dateEnd!.now} ); Minimum Downloads: ${minCount} </h1>

<h2>Download Stats</h2>
<table class="table table-sm table-striped"" id="tblDownloadStats">
      <thead class="thead-dark">

    <tr>
        <th>Resource</th>
        <th>File</th>
        <th>Downloads</th>
        <th>day</th>
    </tr>
    </thead>
    <tbody>
        <#list downloadStats as stats>
        <tr>
            <td>
            
                    <#if stats.file.latestThumbnail?? && !stats.file.deleted>
                        <img src="<@s.url value="/files/sm/${stats.file.latestThumbnail.id?c}"/>"><br/>
                    </#if>
            	<#if (stats.file.informationResource)?has_content>
                <a href="<@s.url value="${stats.file.informationResource.detailUrl}" />">${stats.file.informationResource.title}</a>
                (${stats.file.informationResource.id?c})
                </#if> 
            </td>
            <td>
                <a href="<@s.url value="/filestore/${stats.file.informationResource.id?c}/${stats.file.id?c}" />">${stats.file.filename}</a> (${stats.file.informationResource.id?c})
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
