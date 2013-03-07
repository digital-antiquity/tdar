<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/${themeDir}/settings.ftl" as settings>

<h1>Administrative info for <span>${resource.title}</span></h1>

<h2>Usage Stats</h2>
<#noescape>
<script>
$(function() {

var d0 = {};
var data = [];


	<#list usageStatsForResources as stats>
            data.push(["${stats.aggregateDate?string("yyyy-MM-dd")}", ${stats.count?c}]);
        </#list>
        d0.label = "Views";
        d0.color = "${settings.barColors[ 0 % settings.barColors?size ]}";

    $.plot($("#graphviewstats"), [ {label: d0.label, data: data ,color: d0.color }],{
          bars: {
            show: true,
            barWidth: 5,
            align: "center"
          },
          xaxis: {
            mode:"time",
            minTickSize: [1, "day"],
	        timeformat: "%y-%m-%d",
	        min: (new Date(${resource.dateRegistered?string("yyyy-MM-dd")})),
            max: (new Date())
        },
        legend : {
            show:true,
            position:"nw"
        }
    });
});
</script>
<div id="graphviewstats" style="height:120px"></div>
</#noescape>
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



<@view.accessRights />

<@view.adminFileActions />
</#escape>