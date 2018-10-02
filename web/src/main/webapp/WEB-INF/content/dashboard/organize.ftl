<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "dashboard-resource.ftl" as dash />
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/search-macros.ftl" as search>
<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
<#import "/WEB-INF/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />

</head>


<div id="titlebar" parse="true">
    <h1>Dashboard &raquo; <span class="red">My Library</span></h1>

</div>
<div class="row">
<div class="col-2">
    <@dash.sidebar current="collections" />
</div>
<div class="col-10">
        <div class="table table-sm table-striped">
            <table class="table ">
              <thead class="thead-dark">

            <tr>
                <th>Collection</th>
                <th>Description</th>
                <th></th>
                <th></th>
            </tr>
            </thead>
                <tbody>
            <#list allResourceCollections as collection>
                    <tr>
                        <th><a href="<@s.url value="${collection.detailUrl}"/>">${collection.name}</a></th>
                        <td>${collection.description!"no description available"}</td>
                        <td><@dash.collectionLegend collection /> </td>
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

    <#macro moremenu collection>
            <a href="<@s.url value="/collection/${collection.id?c}/edit"/>"class="btn btn-sm">Edit</a>

    </#macro>


<script>
    $(document).ready(function () {
        TDAR.notifications.init();
        TDAR.common.collectionTreeview();
        $("#myCarousel").carousel('cycle');
    });
</script>


</#escape>