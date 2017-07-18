<#import "/WEB-INF/macros/resource/common.ftl" as common>

<#macro searchheader>
<form name="searchheader" action="<@s.url value="/search/results"/>" class="searchheader">
    <input type="text" name="query" placeholder="Find archaeological data..." class="searchbox input-xxlarge">
    <a href="/search">advanced</a>
    <input type="hidden" name="_tdar.searchType" value="simple">
</form>
</#macro>

