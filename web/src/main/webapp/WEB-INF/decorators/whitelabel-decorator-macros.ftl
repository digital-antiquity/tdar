<#macro searchHeader>
<#if !searchHeaderEnabled><#return></#if>
<#assign subtitle = (resourceCollection.properties.subtitle!(resourceCollection.institution.name)!'')>
<div class="searchheader whitelabel">
    <div class="container">
        <div class="row">
            <div class="hero">
                <h2 class="color-title">${title}</h2>
                <#if subtitle?has_content>
                <p class="color-subtitle">${subtitle}</p>
                <#else><p></p>
                </#if>
                <form name="searchheader" action="<@s.url value="/search/results"/>" class="searchheader">
                    <input type="text" name="query" placeholder="Search within this collection..." class="searchbox input-xxlarge" value="${query!''}">
                    <a href="/search/advanced?collectionId=${resourceCollection.id?c}">advanced search</a>
                    <input type="hidden" name="_tdar.searchType" value="simple">
                    <input type="hidden" name="collectionId" value="${resourceCollection.id}">
                </form>
                <ul class="subnav-rht hidden-phone hidden-tablet"><@common.loginMenu false/></ul>
            </div>
        </div>
    </div>
</div>
</#macro>
