<#macro layout_header>

<nav class="navbar navbar-expand-md navbar-light bg-white">
      <a href="/" class="navbar-brand">
                <img src="${staticHost}/images/r4/bg-logo.png" title="tDAR - the Digital Archaeological Record" usemap="#tdarmap">
            </a>

      <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#tdarNavMenu">
                <span class="navbar-toggler-icon"></span>
            </button>

      <div class="d-flex flex-column ml-auto">
        <div class="mr-0 d-flex align-items-top justify-content-end">
            <#if (authenticatedUser??) >
                <p id="welcome-menu" class="welcome  screen ">
                    <@s.text name="menu.welcome_back"/> 
                        <a href="">${authenticatedUser.properName}
                        <i class="caret drop-down"></i>
                    </a>
                </p>
            <#else>
                <p><a href="">Log In</a> or <a href="">Sign Up</a></p>
            </#if>  
        </div>
        <div class="collapse navbar-collapse d-flex align-items-end" id="tdarNavMenu">
          <ul class="navbar-nav">
            <li class="nav-item mr-sm-3">
              <button class="btn btn-sm btn-outline-secondary" type="button">Browse</button>
            </li>
            <li class="nav-item mr-sm-3">
              <button class="btn btn-sm btn-outline-secondary" type="button">Upload</button>
            </li>
            <li class="nav-item">
              <form class="form-inline">
                <input class="form-control form-control-sm" type="search" placeholder="Search..." aria-label="Search">
                <div class="input-group-append">
                  <button class="btn btn-sm" type="button"><i class="fas fa-search"></i></button>
                </div>
              </form>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <nav class="navbar navbar-expand-md navbar-light bg-white">
      <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#tdarNavMenu2">
              <span class="navbar-toggler-icon"></span>
          </button>

      <div class="collapse navbar-collapse" id="tdarNavMenu2">
        <ul class="navbar-nav">
          <li class="nav-item mr-sm-3">
            <a href="" class="nav-link">SAA</a>
          </li>
          <li class="nav-item mr-sm-3">
            <a href="" class="nav-link">News</a>
          </li>
          <li class="nav-item mr-sm-3">
            <div class="dropdown">
              <button class="btn btn-light dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                About
              </button>
              <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                <h5 class = "ml-4">About</h5>
                <div class="d-flex flex-row">
                  <div class="d-flex flex-column">
                    <a class="dropdown-item" href="#">Our Team</a>
                    <a class="dropdown-item" href="#">Organization</a>
                    <a class="dropdown-item" href="#">Publications</a>
                    <a class="dropdown-item" href="#">History</a>
                    <a class="dropdown-item" href="#">Contact Us</a>
                    <a class="dropdown-item" href="#">Current Versions</a>
                  </div>
                  <div class="d-flex flex-column">
                    <a class="dropdown-item" href="#"><strong>Search</strong></a>
                    <a class="dropdown-item" href="#"><strong>Explore</strong></a>
                  </div>
                </div>
              </div>
            </div>
          </li>
          <li class="nav-item mr-sm-3">
            <div class="dropdown">
              <button class="btn btn-light dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                Why tDAR?
              </button>
              <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                <h5 class = "ml-4">Why tDAR?</h5>
                <div class="d-flex flex-row">
                  <div class="d-flex flex-column">
                    <a class="dropdown-item" href="#">Our Team</a>
                    <a class="dropdown-item" href="#">Organization</a>
                    <a class="dropdown-item" href="#">Publications</a>
                    <a class="dropdown-item" href="#">History</a>
                    <a class="dropdown-item" href="#">Contact Us</a>
                    <a class="dropdown-item" href="#">Current Versions</a>
                  </div>
                  <div class="d-flex flex-column">
                    <a class="dropdown-item" href="#"><strong>Search</strong></a>
                    <a class="dropdown-item" href="#"><strong>Explore</strong></a>
                  </div>
                </div>
              </div>
            </div>
          </li>
          <li class="nav-item mr-sm-3">
            <div class="dropdown">
              <button class="btn btn-light dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                Using tDAR
              </button>
              <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                <h5 class = "ml-4">Using tDAR</h5>
                <div class="d-flex flex-row">
                  <div class="d-flex flex-column">
                    <a class="dropdown-item" href="#">Our Team</a>
                    <a class="dropdown-item" href="#">Organization</a>
                    <a class="dropdown-item" href="#">Publications</a>
                    <a class="dropdown-item" href="#">History</a>
                    <a class="dropdown-item" href="#">Contact Us</a>
                    <a class="dropdown-item" href="#">Current Versions</a>
                  </div>
                  <div class="d-flex flex-column">
                    <a class="dropdown-item" href="#"><strong>Search</strong></a>
                    <a class="dropdown-item" href="#"><strong>Explore</strong></a>
                  </div>
                </div>
              </div>
            </div>
          </li>
        </ul>
        <div class="dropdown ml-auto">
          <button class="btn btn-light dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            My Account
          </button>
          <div class="dropdown-menu dropdown-menu-right" aria-labelledby="dropdownMenuButton">
            <#if (authenticatedUser??) >
                <h5 class = "ml-4">${authenticatedUser.properName}</h5>
                <div class="d-flex flex-row">
                  <div class="d-flex flex-column">
                    <a class="dropdown-item" href="<@s.url value="/contribute"/>"><@s.text name="menu.create_a_resource"/></a>
                    <a class="dropdown-item" href="<@s.url value="/project/add"/>"><@s.text name="menu.create_a_project"/></a>
                    <a class="dropdown-item" href="<@s.url value="/collection/add"/>"><@s.text name="menu.create_a_collection"/></a>
                    <a class="dropdown-item" href="<@s.url value="/dashboard#bookmarks"/>"><@s.text name="menu.bookmarks"/></a>
                    <br>
                    <a class="dropdown-item" href="<@s.url value='/entity/user/myprofile'/>"><@s.text name="menu.my_profile"/></a>
                    <a class="dropdown-item" href="${commentUrlEscaped}?subject=tDAR%20comments"><@s.text name="menu.contact"/></a>
                    <a class="dropdown-item" href="#">Sign Out</a>
                  </div>
                  <div class="d-flex flex-column">
                    <a class="dropdown-item" href="#">Dashboard</a>
                    <a class="dropdown-item" href="#">Explore</a>
                    <a class="dropdown-item" href="#">Search</a>
                    <a class="dropdown-item" href="#">Integrate</a>
                  </div>
                </div>
            <#else>
                <a class="dropdown-item" href="<@s.url value="/account/new" />" class="button" rel="nofollow">Sign Up</a>
            </#if>
          </div>
        </div>
      </div>
    </nav>


    
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
             <form class="form-complete-inline seleniumIgnoreForm" id="frmMenuLogout" name="logoutFormMenu" method="post" action="/logout">
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

    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#tdarNavMenu">
        <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="tdarNavMenu">
        <ul class="navbar-nav ml-auto">
            <li class="nav-item"><a class="nav-link" href="https://www.tdar.org/saa/">SAA</a></li>
            <li class="nav-item"><a class="nav-link" href="https://www.tdar.org/news/">News</a></li>
            <li class="nav-item"><a class="nav-link" href="https://www.tdar.org/about">About</a></li>
            <li class="nav-item"><a class="nav-link" href="https://www.tdar.org/using-tdar">Using ${siteAcronym}</a></li>

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
    </div>
</nav>


</#macro>

<#macro homepageHeader>
    <div class="row">
        <div class="hero">
<h2>What can you dig up?</h2>

<p><strong>The Digital Archaeological Record (tDAR)</strong> is your online archive <br/>for archaeological information.</p>

<form name="searchheader" action="<@s.url value="/search/results"/>" class="searchheader">
    <input type="text" name="query" placeholder="Find archaeological data..." accesskey="s" class="searchbox input-xxlarge">
    <a href="<@s.url value="/search"/>">advanced</a>
    <input type="hidden" name="_tdar.searchType" value="simple">
</form>

        <@auth.loginMenu true/>
        </div>
        <ul class="inline-menu hidden-desktop"><@auth.loginMenu false/></ul>
    </div>


<div class="row">
    <div class="span3 bucket">
        <img src="${staticHost}/images/r4/icn-data.png" alt="Access / Use" title="Access / Use" />

        <h3><a href="http://www.tdar.org/why-tdar/data-access/">Access &amp; Use</a></h3>

        <p style="min-height:4em">Broadening the access to archaeological data through simple search and browse functionality.</p>

        <p>
            <a href="http://www.tdar.org/why-tdar/data-access/" class="button">Learn More</a>
        </p>
    </div>
    <div class="span3 bucket">
        <img src="${staticHost}/images/r4/icn-pres.png" alt="Preservation" title="Preservation" />

        <h3><a href="http://www.tdar.org/why-tdar/preservation/">Preservation</a></h3>

        <p style="min-height:4em">Dedicated to ensuring long-term preservation of digital archaeological data.</p>

        <p>
            <a href="http://www.tdar.org/why-tdar/preservation/" class="button">Learn More</a>
        </p>
    </div>
    <div class="span3 bucket">
        <img src="${staticHost}/images/r4/icn-stew.png" alt="Stewardship" title="Stewardship"/>

        <h3><a href="http://www.tdar.org/why-tdar/contribute/">Upload Resources</a></h3>

        <p style="min-height:4em">Contribute documents, data sets , images, and other critical archaeological materials.</p>

        <p>
            <a href="http://www.tdar.org/why-tdar/contribute/" class="button">Learn More</a>
        </p>
    </div>
    <div class="span3 bucket">
        <img src="${staticHost}/images/r4/icn-uses.png" alt="Use" title="Use" />

        <h3><a href="http://www.tdar.org/using-tdar/">Who Uses tDAR</a></h3>

        <p style="min-height:4em">Researchers like you. Uncover knowledge of the past, and preserve and protect archaeological resources.</p>

        <p>
            <a href="http://www.tdar.org/using-tdar/" class="button">Learn More</a>
        </p>
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