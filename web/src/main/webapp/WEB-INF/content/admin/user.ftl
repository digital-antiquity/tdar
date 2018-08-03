<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "admin-common.ftl" as admin>
<head>
    <title>User Info Pages</title>

</head>

    <@admin.header />

<a class="button" href="<@s.url value="/admin/user/user-mailchimp" />">Import for Mailchimp/ full user report</a>
<br/>
    <@admin.statsTable historicalUserStats "User Statistics" "userstats" />
<h3>Repeated Logins</h3>
<table class="table tableFormat">
    <thead>
    <tr>
        <th># of logins</th>
        <th>user count</th>
    </tr>
        <#list userLoginStats as count>
        <tr>
            <td>${count.first?c}</td>
            <td>${count.second?c}</td>
        </tr>
        </#list>
    </thead>
</table>



<h3>User Agreement Stats</h3>
<div class="row">
    <div class="col-6">
        <table class="table table-bordered table-condensed table-hover">
            <colgroup>
                <col style="width:40%">
                <col>
            </colgroup>
            <#list agreementCounts?keys as key>
                <tr>
                    <td><@s.text name="AgreementTypes.${key}" /> </td>
                    <td><div class="text-right">${agreementCounts.get(key)}</div></td>
                </tr>
            </#list>
        </table>
    </div>
</div>


<h3>User Affiliation Stats</h3>
<div class="row">
    <div class="col-6">
        <h4>All Users</h4>
        <table class="table table-bordered table-condensed table-hover">
            <colgroup>
                <col style="width:40%">
                <col>
            </colgroup>
                <#list affiliationCounts?keys as key>
                <tr>
                    <td><@s.text name="UserAffiliation.${key}" /> </td>
                    <td><div class="text-right">${affiliationCounts.get(key)}</div></td>
                </tr>
                </#list>
        </table>
    </div>

    <div class="col-6">
        <h4>Contributors</h4>
        <table class="table table-bordered table-condensed table-hover">
            <colgroup>
                <col style="width:40%">
                <col class="text-right">
            </colgroup>
                <#list contributorAffiliationCounts?keys as key>
                <tr>
                    <td><@s.text name="UserAffiliation.${key}" /> </td>
                    <td><div class="text-right">${contributorAffiliationCounts.get(key)}</div></td>
                </tr>
                </#list>
        </table>
    </div>

</div>


<div>
    <h3>Recently Registered Users </h3>
    <table class="table tableFormat">
        <thead>
        <tr>
            <th>User</th>
            <th>Email</th>
            <th>Date Registered</th>
            <th style="width:60%">Contributor Reason</th>
        </tr>
        </thead>
        <#list recentUsers as user>
            <tr>
                <td><a href="<@s.url value="/browse/creators/${user.id?c}"/>">${user.properName}</a></td>
                <td> ${(user.email)!'n/a'}</td>
                <td> ${(user.dateCreated)!}</td>
                <td style="width:60%">
                    ${user.contributorReason!""}
                </td>
            </tr>
        </#list>
    </table>
</div>

<div class="glide">
    <h3>Registered Users</h3>
    <table id="dataTable" class="tableFormat"></table>
</div>
<script>
$(document).ready(function () {
    TDAR.datatable.initUserDataTable();
});
</script>
</#escape>
