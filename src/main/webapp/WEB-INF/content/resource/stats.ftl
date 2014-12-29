<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>

<h1>Usage Information for <span>${resource.title}</span></h1>

<h2>Usage Stats</h2>
<div class="row">
    <div class="span9">
        <div id="chart1" style="width:100%; height:400px" title="Views & Downloads"></div>
    </div>
</div>
<#-- The '&' is being escaped, hence no need for '&amp;' -->
<#--<@common.barGraph data="data" graphLabel="views & downloads" xaxis="date" graphHeight=200/>-->
<table class="tableFormat table">
    <tr>
        <th>views</th>
        <th>day</th>
    </tr>
	<#assign total = 0/>
    <#list usageStatsForResources as stats>
        <tr>
            <td>${stats.count}</td>
            <td>${stats.aggregateDate?date}</td>
        </tr>
        	<#assign total = total + stats.count />
    </#list>
</table>
<#if (total > 0)>
    <#noescape>
    <script>
        $(function () {
            var usageStats = (${jsonStats});
            
            TDAR.charts.adminUsageStats({
                rawData: usageStats,
                label: "Views & Downloads"
            });
        });
    </script>
    </#noescape>
<#else>
	<style>
	#chart1 {display:none;visibile:hidden}
	</style>
        <p><b>None</b></p>
</#if>

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
                        <td>${stats.aggregateDate?date}</td>
                    </tr>
                </#list>
            </table>
            </#if>
        </#list>
        <#if !contents>
        <p><b>None</b></p>
        </#if>
    </#if>

</#escape>