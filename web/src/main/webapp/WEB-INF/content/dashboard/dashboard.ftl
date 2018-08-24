<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "common-dashboard.ftl" as dash >
<#import "/WEB-INF/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
        <h1>Dashboard &raquo; <span class="red">My Resources</span></h1>
        <@dash.headerNotifications />
</div>

<div class="row">
    <div class="col-2">
        <@dash.sidebar current="dashboard" />
    </div>

    <div class="col-10">
        <h2>Welcome <#if authenticated.penultimateLogin?has_content>back,</#if> ${authenticatedUser.firstName}!</h2>
        <#if contributor>
            <div class="row">
                <div class="col-lg-8 col-xl-10">
                    <h4>Upload Resources</h4>
                    <div class="row">
                        <div class="col-xl-3 col-lg-6 pl-1 pr-1">
                            <div class="card">
                              <div class="card-body">
                            <svg class="svgicon svg-small black pull-left" style="margin-left:-5px;"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_document"></use></svg>
                            Use <b>Documents</b> to archive reports, books, PDFs, and other textual materials.
                              <p class="center"><a href="/document/add" class="button tdarButton">Upload</a></p>
                              </div>
                              </div>
                        </div>
                        <div class="col-xl-3 col-lg-6 pl-1 pr-1">
                            <div class="card">
                              <div class="card-body">
                            <svg class="svgicon svg-small black  pull-left" style="margin-left:-5px;"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_dataset"></use></svg>
                            Use <b>Datasets</b> for data tables, Excel, Access and other tabular data files.
                              <p class="center"><a href="/dataset/add" class="button tdarButton">Upload</a></p>
                        </div>
                        </div>
                        </div>
                        <div class="col-xl-3 col-lg-6 pl-1 pr-1">
                            <div class="card">
                              <div class="card-body">
                            <svg class="svgicon svg-small black  pull-left"  style="margin-left:-5px;"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_image"></use></svg>
                            Use <b>Images</b> for drawings, photographs, JPEG, TIFF, and other image files.
                             <p class="center"><a href="/image/add" class="button tdarButton">Upload</a></p>
                        </div>
                        </div>
                        </div>
                        <div class="col-xl-3 col-lg-6 pl-1 pr-1">
                            <div class="card">
                              <div class="card-body">
                            <svg class="svgicon svg-small black  pull-left" style="margin-left:-5px;"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_project"></use></svg>
                            Have a <b>3D Scan</b>, <b>GIS File</b>, or want to create a <b>Coding Sheet</b>, <b>Ontology</b>, or <b>Project</b>?
                             <p class="center"><a href="/contribute" class="button tdarButton">Upload</a></p>
                        </div>
                        </div>
                    </div>
                </div>
                </div>
                <div class="col-lg-4 col-xl-2 pl-1 pr-1">
                    <h4>Organize</h4>
                <div class="row">
                <div class="col">
                    <div class="card">
                              <div class="card-body">
                            <svg class="svgicon svg-small black  pull-left" style="margin-right: 5px;"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_collection"></use></svg>
                            Use <b>Collections</b> to organize your resources and manage permissions.
                             <p class="center"><a href="/collection/add" class="button tdarButton">Create</a></p>
                        </div>
                    </div>
                </div>
        </div>
                </div>
                </div>
        <br>
            </#if>
        <#if (false )>
            <div class="row">
                <div class="col-8">
                    <div class="row">
                    <h4 style="text-align:center">At a glance</h4>
                    </div>
                    <div class="row">
                        <div class="col-4">
                
                            <div class="pieChart" id="statusChart" data-columns="#statusTypeData" style="height:200px" data-click="dashboardStatusPieChartClick"  data-legend-position="right">
                            </div>
                            
                            <#noescape>
                            <script id='statusTypeData'>
                            ${statusData}
                            </script>
                            </#noescape>
                
                        </div>
                        <div class="col-4">
                            <div class="pieChart" id="resourceTypeChart" data-columns="#resourceTypeData" style="height:200px" data-click="dashboardResourcePieChartClick" data-legend-position="right">
                            </div>
                            
                            <#noescape>
                            <script id='resourceTypeData'>
                            ${resourceTypeData}
                            </script>
                            </#noescape>
                        </div>
                    </div>
                </div>
            </div>
    
    
        <div class="row">
            <div class="col-10">
                <hr/>
            </div>
        </div>
        </#if>

        <div class="">
            <#if contributor>
                <#if (activeResourceCount == 0)>
                    <@gettingStarted />
                    <hr/>
                <#else>
                    <@recentlyUpdatedSection />
                </#if>

                <#--  <@emptyProjectsSection /> -->
                <@browseResourceSection />
            
            <#else>
                <@searchSection />
                <#if featuredResources?has_content  >
                    <hr/>
                    <div class="row">
                        <@view.featured colspan="9" header="Featured and Recent Content"/>
                    </div>
                </#if>
            </#if>
        </div>
    </div>
</div>





<div id="sidebar-right" parse="true">
    <div>
        <div id="myProfile">
            <hr/>
        </div>
        <#if contributor>
            <#if (activeResourceCount != 0)>
                <@resourcePieChart />
                <hr/>
            </#if>
        <#else>
            <div id="myCarousel" class="carousel slide" data-interval="5000" data-pause="hover">
                <#assign showBuy = (!accounts?has_content) />
              <!-- Carousel items -->
              <div class="carousel-inner">
                <div class="active item">
                    <a href="${config.documentationUrl}">
                        <img class="" src="<@s.url value="/images/dashboard/learn.png"/>" width=120 height=150 alt="Read the Manual"/>
                            Read the Manual
                    </a>
                </div>
                    <#if (showBuy)>
                    <div class="item">
                        <a href="<@s.url value="/cart/add"/>">
                            <img class="" src="<@s.url value="/images/dashboard/upload.png"/>" width=120 height=150 alt="Purchase Space"/>
                                Buy tDAR now
                        </a>
                    </div>
                    </#if>
                <div class="item">
                        <a href="<@s.url value="/browse/explore"/>">
                            <img class="" src="<@s.url value="/images/dashboard/explore.png"/>" width=120 height=150 alt="Explore"/>
                                Explore Content now
                        </a>
                </div>
              </div>
              <div class="clearfix centered">
              <ol class="carousel-indicators" >
                <li data-target="#myCarousel" data-slide-to="0" class="active"></li>
                <#if showBuy><li data-target="#myCarousel" data-slide-to="1"></li></#if>
                <li data-target="#myCarousel" data-slide-to="2"></li>
              </ol>
                <!-- Carousel nav 
  <a class="carousel-control left" href="#myCarousel" data-slide="prev">&lsaquo;</a>
  <a class="carousel-control right" href="#myCarousel" data-slide="next">&rsaquo;</a>
                -->
              </div>

        </div>
        </#if>
    </div>
</div>



<hr/>
<div>
<ul class="inline center">
            <li><a href="<@s.url value="/browse/creators/${authenticatedUser.id?c}"/>">${authenticatedUser.properName}</a></li>
            <#if authenticatedUser.institution??>
                <li><a href="<@s.url value="/browse/creators/${authenticatedUser.institution.id?c}"/>">${authenticatedUser.institution.properName}</a></li>
            </#if>
            <#if authenticatedUser.penultimateLogin??>
                <li><strong>Last Login: </strong>${authenticatedUser.penultimateLogin?datetime}</li>
            </#if>
</ul>
</div>
    <#-- <@accountSection /> -->
<hr/>


<#macro searchSection>
    <div class="row">
        <div class="col-10">
            <form name="searchheader" action="<@s.url value="/search/results"/>">
                <input type="text" name="query" class="searchbox" placeholder="Search ${siteAcronym} &hellip; ">
                <@s.checkboxlist id="includedResourceTypes" numColumns=4 spanClass="col-2" name='resourceTypes' list='resourceTypes'  listValue='label' label="Resource Type"/>
                <@s.submit value="Search" cssClass="btn btn-primary" />
            </form>
        </div>    
    </div>
</#macro>


<#macro gettingStarted>
    <div class="row">
        <div class="col-10">
            <h3>Getting Started</h3>
            <ol style='list-style-position:inside'>
                <li><a href="<@s.url value="/project/add"/>">Start a new Project</a></li>
                <li><a href="<@s.url value="/resource/add"/>">Add a new Resource</a></li>
            </ol>
        </div>
    </div>
</#macro>

<#macro resourcePieChart>
    <div>
        <h3>At a glance</h3>
    </div>
</#macro>

<#macro recentlyUpdatedSection>
    <div class="row">
        <div class="col-12">
            <h3><@s.text name="dashboard.recently_updated"/></h3>
            <ol id='recentlyEditedResources'>
                <#list recentlyEditedResources as res>
                    <li id="li-recent-resource-${res.id?c}">
                            <@commonr.cartouche res true>
                                <a href="<@s.url value='${res.detailUrl}' />"><@common.truncate res.title 120 /></a>
                            </@commonr.cartouche>
                    </li>
                </#list>
            </ol>
        </div>
    </div>
</#macro>

<#macro emptyProjectsSection>
    <#if (emptyProjects?? && !emptyProjects.empty )>
        <div class="row">
            <div class="col-10" id="divEmptyProjects">
                <h3>Empty Projects</h3>
                <ol id="emptyProjects">
                    <#list emptyProjects as res>
                        <li id="li-empty-project-${res.id?c}">
                            
                            <a href="<@s.url value="${res.detailUrl}"/>">
                                <@common.truncate res.title 60 />
                            </a>
                            
                            <small>(ID: ${res.id?c})</small>
                   
                                <div class="btn-group inline recent-nav pull-right">
                                    <a class="btn btn-sm" href="<@s.url value='/resource/add?projectId=${res.id?c}'><@s.param name="id" value="${res.id?c}"/></@s.url>"
                                       title="add a resource">add resource</a>
                                    <a class="btn btn-sm" href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.edit" /></a>
                                    <a class="btn btn-sm"  href="<@s.url value='/resource/delete?'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.delete" /></a>
                                </div>
                        </li>
                    </#list>
                </ol>
            </div>
        </div>
        <hr/>
    </#if>
</#macro>


<#macro browseResourceSection>
    <@search.reindexingNote />
    <div class="row" id="project-list">
        <div class="col-12">
            <h3>Browse Resources</h3>
            <div>   
                <@edit.resourceDataTable span="col-12" />
            </div>
        </div>
    </div>
</#macro>

<#macro repeat num val>
    <#if (num > 0)>
        <@repeat (num-1) val /><#noescape>${val}</#noescape>
    </#if>
</#macro>

<script>
    $(document).ready(function () {
        TDAR.notifications.init();
        TDAR.common.collectionTreeview();
        $("#myCarousel").carousel('cycle');
        if ($("#project-selector").val() != '' || $("#collection-selector").val() != '' || $("#statuses").val() != '' || $("#resourceTypes").val() != '') {
        console.log("show advanved filters");
        	$("#divAdvancedFilters").toggleClass("collapse");
        }
    });
</script>
</#escape>
