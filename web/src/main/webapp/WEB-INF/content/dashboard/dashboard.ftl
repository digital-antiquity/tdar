<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "common-dashboard.ftl" as dash >
<#import "/${config.themeDir}/settings.ftl" as settings>

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
    <div class="span2">
        <@dash.sidebar current="dashboard" />
    </div>

    <div class="span10">
        <div class="row">
            <div class="span10">
                Welcome <#if authenticated.penultimateLogin?has_content>back,</#if> ${authenticatedUser.firstName}!
                
                <#if contributor>
                    The resources you can access are listed below. To create a <a href="<@s.url value="/resource/add"/>">new resource</a> or
                    <a href="<@s.url value="/project/add"/>">project</a>, or <a href="<@s.url value="/collection/add"/>">collection</a>, click on the "upload" button
                    above.
                <hr/>
                </#if>
            </div>
        </div>
    
        <#if (activeResourceCount > 0 )>
            <div class="row">
                <div class="span8">
                    <div class="row">
                    <h4 style="text-align:center">At a glance</h4>
                    </div>
                    <div class="row">
                        <div class="span4">
                
                            <div class="pieChart" id="statusChart" data-columns="#statusTypeData" style="height:200px" data-click="dashboardStatusPieChartClick"  data-legend-position="right">
                            </div>
                            
                            <#noescape>
                            <script id='statusTypeData'>
                            ${statusData}
                            </script>
                            </#noescape>
                
                        </div>
                        <div class="span4">
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
        </#if>
    
    
        <div class="row">
            <div class="span10">
                <hr/>
            </div>
        </div>

        <div class="">
            <#if contributor>
                <#if (activeResourceCount == 0)>
                    <@gettingStarted />
                    <hr/>
                <#else>
                    <@recentlyUpdatedSection />
                </#if>

                <@emptyProjectsSection />
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
        <div class="span10">
            <form name="searchheader" action="<@s.url value="/search/results"/>">
                <input type="text" name="query" class="searchbox" placeholder="Search ${siteAcronym} &hellip; ">
                <@s.checkboxlist id="includedResourceTypes" numColumns=4 spanClass="span2" name='resourceTypes' list='resourceTypes'  listValue='label' label="Resource Type"/>
                <@s.submit value="Search" cssClass="btn btn-primary" />
            </form>
        </div>    
    </div>
</#macro>


<#macro gettingStarted>
    <div class="row">
        <div class="span10">
            <h2>Getting Started</h2>
            <ol style='list-style-position:inside'>
                <li><a href="<@s.url value="/project/add"/>">Start a new Project</a></li>
                <li><a href="<@s.url value="/resource/add"/>">Add a new Resource</a></li>
            </ol>
        </div>
    </div>
</#macro>

<#macro resourcePieChart>
    <div>
        <h2>At a glance</h2>
    </div>
</#macro>

<#macro recentlyUpdatedSection>
    <div class="row">
        <div class="span10">
            <h2><@s.text name="dashboard.recently_updated"/></h2>
            <ol id='recentlyEditedResources'>
                <#list recentlyEditedResources as res>
                    <li id="li-recent-resource-${res.id?c}">
                        <span class="fixed">
                            <@commonr.cartouche res true>
                                <div class="btn-group pull-right recent-nav">
                                    <a class="btn btn-mini" href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.edit" /></a> |
                                    <a class="btn btn-mini" href="<@s.url value='/resource/delete?'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.delete" /></a>
                                </div>
                                <small>[ID: ${res.id?c}]</small> 
                                <a href="<@s.url value='${res.detailUrl}' />"><@common.truncate res.title 60 /></a>
                            </@commonr.cartouche>
                        </span>
                    </li>
                </#list>
            </ol>
        </div>
    </div>
</#macro>

<#macro emptyProjectsSection>
    <#if (emptyProjects?? && !emptyProjects.empty )>
        <div class="row">
            <div class="span10" id="divEmptyProjects">
                <h2>Empty Projects</h2>
                <ol id="emptyProjects">
                    <#list emptyProjects as res>
                        <li id="li-empty-project-${res.id?c}">
                            
                            <a href="<@s.url value="${res.detailUrl}"/>">
                                <@common.truncate res.title 60 />
                            </a>
                            
                            <small>(ID: ${res.id?c})</small>
                   
                                <div class="btn-group inline recent-nav pull-right">
                                    <a class="btn btn-mini" href="<@s.url value='/resource/add?projectId=${res.id?c}'><@s.param name="id" value="${res.id?c}"/></@s.url>"
                                       title="add a resource">add resource</a>
                                    <a class="btn btn-mini" href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.edit" /></a>
                                    <a class="btn btn-mini"  href="<@s.url value='/resource/delete?'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.delete" /></a>
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
        <div class="span10">
            <h2>Browse Resources</h2>
            <div>   
                <@edit.resourceDataTable span="span10" />
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
    });
</script>
</#escape>
