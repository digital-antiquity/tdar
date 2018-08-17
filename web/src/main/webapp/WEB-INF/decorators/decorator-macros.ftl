<#macro layout_header>

    <div id="mdScreenNav">
        <nav class="navbar navbar-expand-md navbar-light">
            <div class = "container">
                <a href="/" class="navbar-brand">
                    <img src="${staticHost}/images/r4/bg-logo-transparent.png" title="tDAR - the Digital Archaeological Record" alt="tDAR - the Digital Archaeological Record"  usemap="#tdarmap">
                </a>

                <div class="d-flex flex-column d-md-none">
                    <div class = "ml-auto">
                        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#tdarNavMenu">
                            <span class="navbar-toggler-icon"></span>
                        </button>
                    </div>

                    <form   name="searchheader" class="form-inline mt-2 seleniumIgnoreForm" action="/search/results" method="GET" >
                        <div class="input-group">
                            <input class="form-control form-control-sm border-right-0 border" type="search" placeholder="Search...">
                            <span class="input-group-append">
                                        <div class="input-group-text bg-transparent"><i class="fa fa-search fa-sm-1x"></i></div>
                                    </span>
                        </div>
                    </form>
                </div>


                <div class="collapse navbar-collapse d-none d-md-flex align-items-end" id="tdarNavMenu">
                    <div class="d-flex flex-column ml-auto">
                        <div class="mr-0 d-flex align-items-top justify-content-end">
                            <#if (authenticatedUser??) >
                            <p id="welcome-menu" class="welcome  screen ">
                                <@s.text name="menu.welcome_back"/>
                                ${authenticatedUser.properName}
                            </p>
                            <#else>
                            <p class = "logIn"><a class = "tdarLink" href="/login">Log In</a> or <a class = "tdarLink" href="/account/new">Sign Up</a></p>
                        </#if>
                    </div>
                    <ul class="navbar-nav">
                        <li class="nav-item">
                            <form  name="searchheader"  class="form-inline seleniumIgnoreForm" action="/search/results" method="GET">
                                <div class="input-group">
                                    <input class="form-control form-control-sm border-right-0 border" type="search" placeholder="Search...">
                                    <span class="input-group-append">
                                        <div class="input-group-text bg-transparent"><i class="fa fa-search fa-sm-1x"></i></div>
                                    </span>
                                </div>
                            </form>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>

        <nav class="navbar navbar-expand-md navbar-light tdarNav py-0">
            <div class = "container">
                <div class="collapse navbar-collapse" id="tdarNavMenu">
                    <ul class="navbar-nav">
                        <li class="nav-item mr-sm-3">
                            <div class="dropdown tdarDropdown">
                                <button class="btn btn-link align-middle tdarNavItem" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">About 
                                <i class="fas fa-chevron-down"></i></button>
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
                            <a href="" class="nav-link align-middle tdarNavItem">News</a>
                        </li>
                        <li class="nav-item mr-sm-3">
                            <div class="dropdown tdarDropdown">
                                <button class="btn btn-link align-middle tdarNavItem" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Use <svg class="svgicon svg-icons_chevron nav-chevron">
                                    <use xlink:href="/images/svg/symbol-defs.svg#svg-icons_chevron"></use></svg></button>
                                <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                                    <h5 class = "ml-4">Use</h5>
                                    <div class="d-flex flex-row">
                                        <div class="d-flex flex-column">
                                        </div>
                                        <div class="d-flex flex-column">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li class="nav-item mr-sm-3">
                            <div class="dropdown tdarDropdown">
                                <button class="btn btn-link align-middle tdarNavItem" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Archive <svg class="svgicon svg-icons_chevron nav-chevron">
                                    <use xlink:href="/images/svg/symbol-defs.svg#svg-icons_chevron"></use></svg></button>
                                <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                                    <h5 class = "ml-4">Archive</h5>
                                    <div class="d-flex flex-row">
                                        <div class="d-flex flex-column">
                                        </div>
                                        <div class="d-flex flex-column">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li class="nav-item mr-sm-3">
                            <a href="/contribute" class="nav-link align-middle tdarNavItem">Upload</a>
                        </li>

                    </ul>
                    <div class="dropdown ml-auto tdarDropdown" id = "myAccountNav">
                        <div class="d-none d-md-block">
                            <button class="btn btn-link align-middle tdarNavItem" type="button" id="myAccountMenu" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">My Account <svg class="svgicon svg-icons_chevron nav-chevron"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_chevron"></use></svg></button>
                            <div class="dropdown-menu dropdown-menu-right" aria-labelledby="dropdownMenuButton">
                                <@dec.myAccountMenu />
                            </div>
                        </div>
                        <div class="d-md-none">
                            <button class="btn btn-link align-middle tdarNavItem" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">My Account <svg class="svgicon svg-icons_chevron nav-chevron"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_chevron"></use></svg></button>
                            <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                                <@dec.myAccountMenu />
                            </div>
                        </div>
                    </div>
            </div>
        </nav>
    </div>



</#macro>

<#macro myAccountMenu>
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
            <form class="seleniumIgnoreForm" id="frmMenuLogout" name="logoutFormMenu" method="post" action="/logout">
                <button type="submit" class="dropdown-item btn-link" name="logout" id="logout-button" value="Logout">Sign Out</button>
            </form>
        </div>
        <div class="d-flex flex-column">
            <a class="dropdown-item" href="/dashboard">Dashboard</a>
            <a class="dropdown-item" href="/browse/explore">Explore</a>
            <a class="dropdown-item" href="/search">Search</a>
            <a class="dropdown-item" href="/workspace/list">Integrate</a>
        </div>
    </div>
    <#else>
        <a class="dropdown-item" href="<@s.url value="/account/new" />" rel="nofollow">Sign Up</a>
        <a class="dropdown-item" href="<@s.url value="/login" />" rel="nofollow">Log In</a>
    </#if>
</#macro>

<#macro homepageHeader>

<section id="hero">
  <div class = "container">  
    <div class="px-5 pb-5 pt-3">
        <div class="container">
            <h2 >What can you dig up?</h2>
            <p class=""><strong>The Digital Archaeological Record (tDAR)</strong> is your online archive<br/> for archaeological information.</p>
            <form class="d-flex" name="searchheader"  action="<@s.url value="/search/results"/>">
                <div class="input-group col-md-9 noleftmargin">
                    <input class="form-control border-right-0 border"  type="text" name="query" accesskey="s" aria-label="Search Archaeological Data">
                    <span class="input-group-append" name="_tdar.searchType" value="simple">
                        <div class="input-group-text bg-white border rounded-right"><i class="fa fa-search noborder"></i></div>
                    </span>
                    <a class="ml-3 align-self-center" href="<@s.url value="/search"/>">advanced</a>
                    <input type="hidden" name="_tdar.searchType" value="simple">
                </div>
            </form>
        </div>
    </div>
  </div>
</section>

<#--     <div class="row">
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
    </div> -->

<section id="learnMore">
  <div class="container px-5 py-3">
      <div class="row">
          <div class="col-lg-3 col-md-6 col-sm-12 mb-3">
              <div class="card text-center h-100 border border-0">
                  <div class="card-body">
                      <img class = "img-fluid mb-3" src="${staticHost}/images/r4/icn-data.png" alt="Access / Use" title="Access / Use" />
                      <h5 class="card-title"><a href="http://www.tdar.org/why-tdar/data-access/">Access &amp; Use</a></h5>
                      <p class="card-text">Broadening the access to archaeological data through simple search and browse functionality.</p>
                      <a href="http://www.tdar.org/why-tdar/data-access/" class="btn tdarButton">Learn More</a>
                  </div>
              </div>
          </div>
          <div class="col-lg-3 col-md-6 col-sm-12 mb-3">
              <div class="card text-center h-100 border border-0">
                  <div class="card-body">
                      <img class = "img-fluid mb-3" src="${staticHost}/images/r4/icn-stew.png" alt="Stewardship" title="Stewardship"/>
                      <h5 class="card-title"><a href="http://www.tdar.org/why-tdar/contribute/">Upload Resources</a></h5>
                      <p class="card-text">Contribute documents, datasets, images, and other critical archaeological materials.</p>
                      <a href="#" class="btn tdarButton">Learn More</a>
                  </div>
              </div>
          </div>
          <div class="col-lg-3 col-md-6 col-sm-12 mb-3">
              <div class="card text-center h-100 border border-0">
                  <div class="card-body">
                      <img class="img-fluid mb-3" src="${staticHost}/images/r4/icn-pres.png" alt="Preservation" title="Preservation" />
                      <h5 class="card-title"><a href="http://www.tdar.org/why-tdar/preservation/">Preservation</a></h5>
                      <p class="card-text">Dedicated to ensuring long-term preservation of digital archaeological data.</p>
                      <a href="#" class="btn tdarButton">Learn More</a>
                  </div>
              </div>
          </div>
          <div class="col-lg-3 col-md-6 col-sm-12 mb-3">
              <div class="card text-center h-100 border border-0">
                  <div class="card-body">
                      <img class="img-fluid mb-3" src="${staticHost}/images/r4/icn-uses.png" alt="Use" title="Use" />
                      <h5 class="card-title"><a href="http://www.tdar.org/using-tdar/">Who Uses tDAR</a></h5>
                      <p class="card-text">Researchers like you. Uncover knowledge of the past, and preserve and protect resources.</p>
                      <a href="#" class="btn tdarButton">Learn More</a>
                  </div>
              </div>
          </div>
      </div>
  </div>
</section>



</#macro>

<#macro subnav>
<#if (subnavEnabled!true)>
<div class="subnav-section">
    <div class="container">
        <div class="row">
            <div class="col-12 subnav">
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