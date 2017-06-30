<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as commonr>
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "dashboard-common.ftl" as dash />
    <#import "/${config.themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
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
                <th>action</th>
            </tr>
            </thead>
            <tbody>
            <#list allResourceCollections as collection>
                <tr>
                <td><a href="${collection.detailUrl}">${collection.name!'no name'}</a> <@dash.collectionLegend collection /><br/>
                ${collection.description!''}
                    </td>
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
<#-- 
        <table class="table" id="allResources">
            <thead>
                <tr>
                <th>Name</th>
                <th># of users</th>
                <th>action</th>
            </tr>
            </thead>
            <tbody>
            <#list internalCollections![] as collection>
                <#if (collection.resources?size > 0 )>
                    <#list collection.resources as resource>
                        <tr>
                        <td><a href="${resource.detailUrl}">${resource.title}</a></td>
                        <td>${collection.authorizedUsers?size}</td>
                        <td>
                            <div class="btn-group">
                              <a class="btn btn-mini" href="/${resource.urlNamespace}/${resource.id?c}/edit">Edit</a>
                            </div>
                        </td>
                        </tr>
                    </#list>
                </#if>
            </#list>
            </tbody>
        </table>
-->
    </#macro>


</#escape>
