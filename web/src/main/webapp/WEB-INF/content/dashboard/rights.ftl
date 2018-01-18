<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "common-dashboard.ftl" as dash />
    <#import "/${config.themeDir}/settings.ftl" as settings>

<head>
    <title>Rights and Permissions: ${authenticatedUser.properName} </title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Dashboard &raquo; <span class="red">Manage Collections &amp; Permissions</span></h1>
</div>
<div class="row">
    <div class="span2">
    <@dash.sidebar current="rights" />
    </div>
    <div class="span10">
	    <#if ((findUsersSharedWith?size!0) > 0)>
	        <h5>Users you've shared with</h5>
	        <@common.listUsers users=findUsersSharedWith span=10 baseUrl="/entity/user/rights" well=true  />
	
	    </#if>
	    <@collectionsSection />
    </div>

</div>

    <#macro repeat num val>
        <#if (num > 0)>
            <@repeat (num-1) val /><#noescape>${val}</#noescape>
        </#if>
    </#macro>

    <#macro collectionsSection>

        <table class="table">
            <thead>
            <tr>
                <th>Collection (${allResourceCollections?size})</th>
                <th>Resources</th>
                <th>Users</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <#list allResourceCollections as collection>
                <tr>
                <td><a href="${collection.detailUrl}">${collection.name!'no name'}</a><br/>
                <@common.truncate collection.description!'' 250 />
                    </td>
                   <td>${((collection.managedResources![])+(collection.unmanagedResources![]))?size}</td>
                   <td>${(collection.authorizedUsers![])?size}</td>
                <td>
                    <div class="btn-group inline">
                      <a class="btn btn-mini" href="/collection/${collection.id?c}/edit">edit</a>
                      <a class="btn btn-mini" href="/collection/delete?id=${collection.id?c}">delete</a>
                    </div>
                 </td>
                </tr>
            </#list>
            </tbody>
        </table>
    </#macro>


</#escape>
