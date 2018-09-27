<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "common-dashboard.ftl" as dash />
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
<#import "/WEB-INF/macros/common.ftl" as common>
<#import "/WEB-INF/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>

</head>


<div id="titlebar" parse="true">
    <h1>Dashboard &raquo; <span class="red">My Bookmarks</span></h1>

</div>
<div class="row">
<div class="col-2">
    <@dash.sidebar current="bookmarks" />
</div>
<div class="col-10">
    <@bookmarksSection />
        </div>
</div>

</div>


</div>

    <#macro moremenu>
    <div class="moremenu pull-right">
        <div class="btn-group">
            <button class="btn btn-sm">View</button>
            <button class="btn btn-sm">Edit</button>
            </div>
    </div>

    </#macro>


    <#macro bookmarksSection>
        <div id="bookmarks">
            <#if ( bookmarkedResources?size > 0)>
                <@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE' listTag='ol' headerTag="h3" titleTag="b" />
            <#else>
            <h3>Bookmarked resources appear in this section</h3>
            Bookmarks are a quick and useful way to access resources from your dashboard. To bookmark a resource, click on the star <i class="icon-star"></i> icon next to any resource's title.
            </#if>
        </div>
    </#macro>

<script>
    $(document).ready(function () {
        TDAR.notifications.init();
        TDAR.common.collectionTreeview();
        $("#myCarousel").carousel('cycle');
    });
</script>



</#escape>