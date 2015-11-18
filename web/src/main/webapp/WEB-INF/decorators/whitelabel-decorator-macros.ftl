<#macro searchHeader>
<#if !searchHeaderEnabled><#return></#if>
<#assign subtitle = (resourceCollection.subtitle!(resourceCollection.institution.name)!'')>
<div class="searchheader">
    <div class="hero">
        <div class="container">
            <div class="pull-right whitelabel-login-menu" ><@common.loginMenu false/></div>
            <h2 class="color-title">${title}</h2>
            <#if subtitle?has_content>
            <p class="color-subtitle">${subtitle}</p>
           	<#else><p></p>
            </#if>
            <form name="searchheader" action="<@s.url value="/search/results"/>" class="searchheader">
                <input type="text" name="query" placeholder="Search within this collection..." class="searchbox input-xxlarge">
                <a href="/search/advanced?collectionId=${resourceCollection.id?c}">advanced</a>
                <input type="hidden" name="_tdar.searchType" value="simple">
                <input type="hidden" name="collectionId" value="${resourceCollection.id}">
            </form>

        </div>
    </div>
</div>
</#macro>
