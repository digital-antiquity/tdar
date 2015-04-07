<#macro layout_header>

<#include "/${themeDir}/header.dec" />
<#if (authenticatedUser??) >

<p id="welcome-menu" class="welcome  screen ">
    <@s.text name="menu.welcome_back"/> <a href="">${authenticatedUser.properName}
    <i class="caret drop-down"></i>
</a>
</p>

<div class="welcome-drop  screen ">

    <p>${authenticatedUser.properName}</p>

    <ul>
        <li><a href="<@s.url value="/contribute"/>"><@s.text name="menu.create_a_resource"/></a></li>
        <li><a href="<@s.url value="/project/add"/>"><@s.text name="menu.create_a_project"/></a></li>
        <li><a href="<@s.url value="/collection/add"/>"><@s.text name="menu.create_a_collection"/></a></li>
        <li><a href="<@s.url value="/dashboard"/>"><@s.text name="menu.dashboard"/></a></li>
        <li><a href="<@s.url value="/dashboard#bookmarks"/>"><@s.text name="menu.bookmarks"/></a></li>
    </ul>

    <ul>
        <li><a href="<@s.url value='/entity/user/myprofile'/>"><@s.text name="menu.my_profile"/></a></li>
        <li><a href="${commentUrlEscaped}?subject=tDAR%20comments"><@s.text name="menu.contact"/></a></li>
        <li><a href="<@s.url value="/logout"/>"><@s.text name="menu.logout"/></a></li>
    </ul>

    <#if administrator>
        <ul>
            <li><@s.text name="menu.admin_header"/></li>
            <li><a href="<@s.url value='/admin'/>"><@s.text name="menu.admin_main"/></a></li>
            <li><a href="<@s.url value='/admin/system/activity'/>"><@s.text name="menu.admin_activity"/></a></li>
            <li><a href="<@s.url value='/admin/searchindex/build'/>"><@s.text name="menu.admin_reindex"/></a></li>
        </ul>
    </#if>

</div>
</#if>

<nav>
    <ul class="hidden-phone-portrait">
    <#include "/${themeDir}/nav-items.dec" />
        <li class="button hidden-phone"><a href="<@s.url value="/search/results"/>">BROWSE</a></li>
    <#if ((authenticatedUser.contributor)!true)>
        <li class="button hidden-phone"><a href="<@s.url value="/contribute"/>">UPLOAD</a></li></#if>
        <li>
            <form name="searchheader" action="<@s.url value="/search/results"/>" class="inlineform hidden-phone hidden-tablet  screen">
                <input type="text" name="query" class="searchbox" placeholder="Search ${siteAcronym} &hellip; ">
            <#--<input type="hidden" name="_tdar.searchType" value="simple">-->
            ${(page.properties["div.divSearchContext"])!""}
            </form>
        </li>
    </ul>

</nav>
</#macro>
