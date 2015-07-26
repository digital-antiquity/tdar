<#import "/WEB-INF/macros/resource/common.ftl" as common>

<#macro searchheader>
<form name="searchheader" action="<@s.url value="/search/results"/>" class="searchheader">
    <input type="text" name="query" placeholder="Find archaeological data..." class="searchbox input-xxlarge">
    <a href="/search">advanced</a>
    <input type="hidden" name="_tdar.searchType" value="simple">
</form>
</#macro>


<#escape _untrusted as _untrusted?html>
<#macro subcollectionSidebar>
    <#if !collections.empty>
    <div id="sidebar-right" parse="true">
        <h3 class="sidebar-spacer">Child Collections</h3>
        <@common.listCollections collections=collections showOnlyVisible=true />
    </div>
    </#if>
</#macro>
</#escape>
