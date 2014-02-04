<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/${themeDir}/settings.ftl" as settings>
<head>
    <script type="text/javascript" src="/js/tdar.charts.js"></script>
</head>

<h1>Administrative info for <span>${resource.title}</span></h1>

<h2>Usage Stats</h2>
<div class="row">
    <div class="span9">
        <div id="chart1" style="width:100%; height:400px" title="Views & Downloads"></div>
    </div>
</div>
<#noescape>
<script>
    $(function() {
        var usageStats = (${jsonStats});
        TDAR.charts.adminUsageStats({
            rawData:usageStats,
            label: "Views & Downloads"
        });
    });
</script>
</#noescape>
    <#-- The '&' is being escaped, hence no need for '&amp;' -->
    <#--<@common.barGraph data="data" graphLabel="views & downloads" xaxis="date" graphHeight=200/>-->
<table class="tableFormat table">
    <tr>
        <th>views</th>
        <th>day</th>
    </tr>
<#list usageStatsForResources as stats>
    <tr>
        <td>${stats.count}</td>
        <td>${stats.aggregateDate?string("yyyy-MM-dd")}</td>
    </tr>
</#list>
</table>

<#if downloadStats?has_content>
<h2>Download Stats</h2>
<#assign contents =false/>
<#list downloadStats?keys as key>
<#if downloadStats.get(key)?has_content>
<h3>${key}</h3>
<#assign contents = true/>
<table class="tableFormat table">
    <tr>
        <th>downloads</th>
        <th>day</th>
    </tr>
<#list (downloadStats.get(key)) as stats>
    <tr>
        <td>${stats.count}</td>
        <td>${stats.aggregateDate?string("yyyy-MM-dd")}</td>
    </tr>
</#list>
</table>
</#if>
</#list>
<#if !contents>
<p><b>None</b></p>
</#if>
</#if>


<h2>Resource Revision History</h2>
<table class="table tableFormat">
    <tr>
        <th>When</th>
        <th>Who</th>
        <th>Event</th>
    </tr>
<#list resourceLogEntries as entry>
    <tr>
        <td>${entry.timestamp}</td>
        <td>${entry.person.properName}</td>
        <td>${entry.logMessage}</td>
    </tr>
</#list>
</table>



<#if (resource.informationResourceFiles?has_content )>
<h2>File History</h2>
<table class="table tableFormat">
    <tr>
        <th colspan="2">Name</th>
        <th>Type</th>
        <th>Version #</th>
        <th>Restriction</th>
        <th>Status</th>
        <th>Size</th>
        <th>Date Uploaded</th>
        <th>MimeType</th>
        <th>Processing Errors?</th>
    </tr>
<#list resource.informationResourceFiles as file>
    <tr>
        <td colspan="2">${file.fileName!"unnamed file"}
        <#if file.latestThumbnail?has_content>
 	       <br><img src="<@s.url value="/filestore/${file.latestThumbnail.id?c}/thumbnail"/>"/>
        </#if>
        </td>
        <td>${file.informationResourceFileType}</td>
        <td>${file.latestVersion}</td>
        <td>${file.restriction}</td>
        <td><#if file.status?has_content>${file.status!""}</#if></td>
           <#assign orig = file.latestUploadedVersion />
           <td></td>
           <td></td>
           <td></td>
        <td><#if !file.processed && file.errorMessage?has_content>${file.errorMessage}</#if></td>
    </tr>
    <#list file.informationResourceFileVersions as vers>
        <#if vers.uploaded >
        <tr>
            <td></td>
            <td>
          <a href="<@s.url value='/filestore/${vers.id?c}/get'/>?coverPageIncluded=false" onClick="TDAR.common.registerDownload('<@s.url value='/filestore/${vers.id?c}/get'/>', '${id?c}')" >${vers.filename}</a></td>
            <td>${vers.fileVersionType} </td>
            <td>${vers.version}</td>
            <td></td>
            <td></td>
               <#assign orig = file.latestUploadedVersion />
               <td>${vers.fileLength}</td>
               <td>${vers.dateCreated}</td>
               <td>${vers.mimeType}</td>
            <td></td>
        </tr>
        </#if>
    </#list>
</#list>
</table>
</#if>

<@view.accessRights />

<@view.adminFileActions />
</#escape>