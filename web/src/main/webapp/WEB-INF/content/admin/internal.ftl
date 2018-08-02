<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/common.ftl" as common>
    <#import "admin-common.ftl" as admin>
<title>Admin Pages</title>

    <@admin.header/>


<div class="glide">
    <h3>Current Resource Breakdown</h3>
    <#assign currentResourceStatsKeys = currentResourceStats?keys?sort />
    <table class="tableFormat table">
        <thead>
        <tr>
            <th>Resource Type</th>
            <th>Total active</th>
            <th>drafts</th>
            <th># with files</th>
            <th># with confidential files</th>
        </tr>
        </thead>
        <#list currentResourceStatsKeys as key>
            <tr>
                <td> ${key.label}</td>
                <#assign vals = currentResourceStats.get(key) />
                <#list vals as val>
                    <td>${val}</td>
                </#list>
            </tr>
        </#list>
    </table>
</div>

    <@admin.statsTable historicalRepositorySizes "Repository Size" "repositorySize" "filesize" />


<div class="glide">
    <h3>Recent Logins </h3>
    <table class="tableFormat table">
        <thead>
        <tr>
            <th>User</th>
            <th>Email</th>
            <th>Login Date / Total</th>
        </tr>
        </thead>
        <#list recentLogins as user>
            <tr>
                <td><a href="<@s.url value="/browse/creators/${user.id?c}"/>">${user.properName}</a></td>
                <td> ${user.email!""}</td>
                <td> ${user.lastLogin!"never"} (${user.totalLogins!0})</td>
            </tr>
        </#list>
    </table>
</div>


<div class="glide">
    <h3>Recently Created or Updated Resources in the Last Week</h3>
    <table class="tableFormat table">
        <tr>
            <th>Id</th>
            <th>Title</th>
            <th nowrap>Resource Type</th>
        </tr>
        <#list recentlyUpdatedResources as resource>
            <tr>
                <td> ${resource.id?c}</td>
                <td><a href="<@s.url value="/${resource.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></td>
                <td> ${resource.resourceType.label}</td>
            </tr>
            <tr>
                <td/>
                <td colspan=2>
                    <small>created on ${resource.dateCreated} by <a
                            href="<@s.url value="/browse/creators/${resource.submitter.id?c}"/>">${resource.submitter.properName}</a>;
                        updated on  ${resource.dateUpdated} by <a
                                href="<@s.url value="/browse/creators/${resource.updatedBy.id?c}"/>">${resource.updatedBy.properName}</a></small>
                </td>
            </tr>
        </#list>
    </table>
</div>

<#if administrator>
<div class="row">
    <div class="col">
    
    <h4>Rebuild Caches</h4>
    <form action="/admin/rebuildCaches" method="POST">
        <input type="submit" name="submit" value="run" cssClass="button btn"/>
    </form>
    
    <h4>Verify Filestore</h4>
    <form action="/admin/verifyFilestore" method="POST">
        <input type="submit" name="submit" value="run" cssClass="button btn"/>
    </form>
    
    
    </div>
    <div class="col">
    
    <h4>Manually run weekly tasks</h4>
    <form action="/admin/runWeekly" method="POST">
        <input type="submit" name="submit" value="run" cssClass="button btn"/>
    </form>
    
    
    <h4>Update Doi's</h4>
    <form action="/admin/updateDois" method="POST">
        <input type="submit" name="submit" value="run" cssClass="button btn"/>
    </form>
    
    </div>
</div>
</#if>


</#escape>
