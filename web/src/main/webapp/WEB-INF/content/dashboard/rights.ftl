<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "common-dashboard.ftl" as dash />
    <#import "/WEB-INF/settings.ftl" as settings>

<head>
    <title>Rights and Permissions: ${authenticatedUser.properName} </title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Dashboard &raquo; <span class="red">Manage Collections &amp; Permissions</span></h1>
</div>
<div class="row">
    <div class="col-2">
    <@dash.sidebar current="rights" />
    </div>
    <div class="col-10">
    
        <div class="well">
            <div class="row">
                <div class="col-12">
                    <h3>Collections</h3>
                </div>
            </div>
            <div class="row">
                <div class="col-10">
                        In ${siteAcronym}, a collection is an organizational tool with two purposes. The first is to allow contributors and users to create groups and
                        hierarchies of resources in any way they find useful. A secondary use of collections allows users to easily administer view and edit
                        permissions for large numbers of persons and resources.<br/><br/>
                        <a href="/collection/add" class="button tdar-button">Create Collection</a>
                </div>
                <div class="col-2">  <svg class="svgicon svg100"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_collection"></use></svg></div>
            </div>
        </div>
    <div class="row">
        <div class="col-12">
	    <#if ((findUsersSharedWith?size!0) > 0)>
	        <h5>Users you've shared with</h5>
	        <@common.listUsers users=findUsersSharedWith span=12 baseUrl="/entity/user/rights" well=false  />
	
	    </#if>
	    <@collectionsSection />
    </div>
    </div>
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
                <#if collection.editable!false>
                    <div class="btn-group inline">
                      <a class="btn btn-mini" href="/collection/${collection.id?c}/edit">edit</a>
                      <a class="btn btn-mini" href="/collection/delete?id=${collection.id?c}">delete</a>
                    </div>
                </#if>
                 </td>
                </tr>
            </#list>
            </tbody>
        </table>
    </#macro>


</#escape>
