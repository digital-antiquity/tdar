<#macro layout_header>

<#include "/${config.themeDir}/header.dec" />
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
        <li>
             <form class="form-complete-inline seleniumIgnoreForm" id="frmMenuLogout" name="logoutFormMenu" method="post" action="/logout" >
                    <button type="submit" class="btn btn-link tdar-btn-link serif" name="logout" value="Logout">Logout</button>
             </form>
         </li>
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
        <#include "/${config.themeDir}/nav-items.dec" />
<!--        <li class="button hidden-phone"><a href="<@s.url value="/search/results"/>">BROWSE</a></li> -->
        <#if ((authenticatedUser.contributor)!true)>
            <li class="button hidden-phone"><a href="<@s.url value="/contribute"/>">UPLOAD</a></li></#if>
        <li>
            <#if navSearchBoxVisible>
                <form name="searchheader" action="<@s.url value="/search/results"/>" class="inlineform seleniumIgnoreForm hidden-phone hidden-tablet  screen">
                <#-- fixme -- boostrap 3/4 should provide a better unstyled way to handle the magnifying glass -->
                    <input type="text" name="query" class="searchbox" accesskey="s" placeholder="Search ${siteAcronym} &hellip; "  value="${(query!'')?html}" maxlength="512">
                    <input type="hidden" name="_tdar.searchType" value="simple">
                ${(page.properties["div.divSearchContext"])!""}
                </form>
            </#if>
        </li>
    </ul>

</nav>


</#macro>

<#macro homepageHeader>
<div id="jumbo">
    <div class="container">
    <div class="row">
        <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
            <div class="jumbotron">
            <ul class="pull-right hidden-phone hidden-tablet nav">
                <li>
                <form class="form-unstyled seleniumIgnoreForm logoutForm" id="frmLogout" name="logoutForm" method="post" action="/logout">
                        <input type="submit" class="tdar-button" name="logout" value="Logout" id="logout-button">
                </form>
                </li>
            </ul>
                
    <h2>What can you dig up?</h2>

    <p><strong>The Digital Archaeological Record (tDAR)</strong> is your online archive <br>for archaeological information.</p>

    <form name="searchheader" action="/search/results" class="searchheader">
        <div class="row">
            <div class="col-lg-8 col-md-8 col-sm-10">
                <div class="input-group jumbo-search">
            <input type="text" name="query" placeholder="Find archaeological data..." accesskey="s" class="form-control">
        
            <input type="hidden" name="_tdar.searchType" value="simple">

                <span class="input-group-btn">
                  <button class="btn btn-default no-left-border" type="button"><i class="glyphicon glyphicon-search"></i></button>
                </span>
            </div>
              </div><!-- /input-group -->

            <div class="col-lg-4 col-md-4 col-sm-2">
                <a href="/search">advanced</a>
            </div>
        </div>
    </form>

            </div>
        </div>
        </div>
    </div>
    </div>

</#macro>

<#macro subnav>
<#if (subnavEnabled!true)>
<div class="subnav-section">
    <div class="container">
        <div class="row">
            <div class="span12 subnav">
                <ul class="subnav-lft">
                    <li><a href="<@s.url value="/search"/>"><@s.text name="menu.search"/></a></li>
                    <li><a href="<@s.url value="/browse/explore"/>"><@s.text name="menu.explore"/></a></li>
                    <#if sessionData?? && sessionData.authenticated>
                        <li><a href="<@s.url value="/dashboard"/>"><@s.text name="menu.dashboard"/></a></li>
<!--
                        <li><a href="<@s.url value="/organize"/>"><@s.text name="menu.organize"/></a></li>
                        <li><a href="<@s.url value="/manage"/>"><@s.text name="menu.manage"/></a></li>
                        <li><a href="<@s.url value="/billing"/>"><@s.text name="menu.billing"/></a></li>

-->                        <li><a href="<@s.url value="/workspace/list"/>"><@s.text name="menu.integrate"/></a></li>
                        <#if editor>
                            <li><a href="<@s.url value="/admin"/>"><@s.text name="menu.admin"/></a></li>
                        </#if>
                    </#if>
                </ul>
                <#if actionName!='login' && actionName!='register' && actionName!='download' && actionName!='review-unauthenticated'>
                    <@auth.loginMenu true />
                </#if>
            </div>
        </div>
    </div>
</div>
</#if>
</#macro>
