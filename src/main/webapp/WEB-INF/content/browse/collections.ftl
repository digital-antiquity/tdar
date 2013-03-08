<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#-- @search.initResultPagination/ -->
<#global searchResultsLayout=true>
<head>
<title><#if collection??>${collection.name}<#else>All Collections</#if></title>
</head>
<body>

<div id="titlebar" parse="true">
    <h1>Browsing <span>All Collections</span></h1>
</div>

<div id="sidebar-left" parse="true" class="options hidden-phone">
    <div id="searchOptions">
        <h3 class="totalRecords">Search Options</h3>
        <ul class="tools media-list">
            <li class="media"><a href="/search/collection"><i class="search-magnify-icon-red" ></i> Find specific collections &raquo;</a></li>
            <!--
            <li><a class="subscribe"  href="${rssUrl!''}">Subscribe via RSS  &raquo;</a>
            -->
        </ul>
    </div>




</div>

<div id="divResultsSortControl">
    <div class="row">
        <div class="span4">
            <h2 class="totalRecords">${paginationHelper.firstItem + 1}-${paginationHelper.lastItem + 1} (${paginationHelper.totalNumberOfItems} Results)</h2>
        </div>
        <div class="span5">
            <#if !hideFacetsAndSort>
            <div class="form-horizontal pull-right">
               <@search.sortFields true/>
            </div>
            </#if>
        </div>
    </div>
</div>

<div class="tdarresults">
    <@list.listResources resourcelist=results sortfield=sortField expanded=true listTag="span" itemTag="span" titleTag="h3" orientation=orientation mapPosition="top" mapHeight="450"/>
</div>



<@search.basicPagination "Collections"/>


</body>
</#escape>