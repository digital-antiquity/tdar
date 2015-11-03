<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>

<title>Usage Information for ${resource.title}</title>

<h1>Usage Information for <span>${resource.title}</span></h1>

<#if graphJson?has_content>
<h2>Usage Stats</h2>
    <div class="lineGraph" id="statusChart"  data-source="#graphJson" style="height:200px" data-x="date" data-values="${categoryKeys}" >
    </div>

<#noescape>
<script id="graphJson">
${graphJson!'[]'}
</script>
</#noescape>
</#if>

<@statTable allByYear yearLabels "Year" />

<@statTable allByMonth monthLabels "Last Month" />

<@statTable allByDay dayLabels "Last Week" />


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


<#macro statTable hash labelList lbl>
<#if labelList?size == 0>
<#return/>
</#if>
<h4>${lbl}</h4>
<table class="table tableFormat">
    <thead>
    <tr>
        <th>Type</th>
        <th>File</th>
        <#list labelList as label>
            <th>
                <#if label?is_number>
                    ${label?c}
                <#else>
                    ${label}
                </#if>
            </th>
        </#list>
    </tr>
    </thead>
    <tbody>
    <#list hash?keys?sort?reverse as key>
        <tr>
            <#if key?? && key?contains("Views")>
                <td colspan="2"><b>Views</b></td>
             <#else>
                <td>Download:</td><td><b> ${key}</b></td>
            </#if>
            <#list hash[key] as val>
            <td>${val}</td>
            </#list>
        </tr>
    </#list>
    </tbody>
</table>
<br/>
</#macro>

</#escape>