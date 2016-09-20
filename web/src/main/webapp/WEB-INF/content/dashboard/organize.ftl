<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "dashboard-common.ftl" as dash />
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />

</head>


<div id="titlebar" parse="true">
    <h2>My Library</h2>

</div>
<div class="row">
<div class="span2">
    <@dash.sidebar current="collections" />
</div>
<div class="span10">
        <div class="table">
            <table class="table ">
            <thead>
            <tr>
                <th>Collection</th>
                <th>Description</th>
                <th></th>
            </tr>
            </thead>
                <tbody>
            <#list allResourceCollections as collection>
                    <tr>
                        <th><a href="<@s.url value="${collection.detailUrl}"/>">${collection.name}</th>
                        <td>${collection.description!"no description available"}
                          <ul class="inline">
                            <li><i class="icon-hdd"></i> ${collection.unmanagedResources?size!0} Resources</li>
                            <li><i class="icon-tasks"></i> ${collection.transientChildren?size!0} Child Collections</li>
                            <li><i class="icon-user"></i> ${collection.authorizedUsers?size!0} Users</li>
                            <li><i class="icon-circle-arrow-right" data-toggle="collapse" data-target="#details${collection.id?c}"></i> </li>
                        </ul>
                        <div id="details${collection.id?c}" class="collapse">
                          <ul class="">
                            <li><i class="icon-tasks"></i> <#list collection.transientChildren as child>${child.name}<#sep>&bull;</#sep></#list></li>
                            <li><i class="icon-user"></i> <#list collection.authorizedUsers as user>${user.user.properName} (${user.generalPermission.name})<#sep>&bull;</#sep></#list></li>
                        </ul>
                        
                        </div>
                        </td>
                        <td><@moremenu collection /></td>
                    </tr>
           </#list>
            <tr>
                <td colspan="3"><a href="/collection/add" class="tdarbutton button">Create a New Collection</a></td>
            </tr>
                </tbody>
            </table>
        </div>



</div>


</div>

    <#macro moremenu collection="">
    <div class=" pull-right">
        <div class="btn-group">
            <#if collection?has_content>
            <a href="<@s.url value="${collection.detailUrl}"/>"class="btn btn-mini">View</a>
            <a href="<@s.url value="/collection/${collection.id?c}/edit"/>"class="btn btn-mini">Edit</a>
            <#else>
            <a href="<@s.url value="/"/>"class="btn btn-mini">View</a>
            <a href="<@s.url value="/collection/0/edit"/>"class="btn btn-mini">Edit</a>
            </#if>
            </div>
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