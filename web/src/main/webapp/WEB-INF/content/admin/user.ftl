<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
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
<table class="table tableFormat">
        <#list agreementCounts?keys as key>
        <tr>
			<th><@s.text name="AgreementTypes.${key}" /> </th><td>${agreementCounts.get(key)}</td>	
        </tr>
		</#list>
</table>


<h3>User Affiliation Stats</h3>
<h4>All Users</h4>
<table class="table tableFormat">
        <#list affiliationCounts?keys as key>
        <tr>
            <th><@s.text name="UserAffiliation.${key}" /> </th><td>${affiliationCounts.get(key)}</td>   
        </tr>
        </#list>
</table>
<h4>Contributors</h4>
<table class="table tableFormat">
        <#list contributorAffiliationCounts?keys as key>
        <tr>
            <th><@s.text name="UserAffiliation.${key}" /> </th><td>${contributorAffiliationCounts.get(key)}</td>   
        </tr>
        </#list>
</table>


<div class="glide">
    <h3>Recently Registered Users </h3>
    <table class="tableFormat">
        <thead>
        <tr>
            <th>User</th>
            <th>Email</th>
            <th>Date Registered</th>
        </tr>
        </thead>
        <#list recentUsers as user>
            <tr>
                <td><a href="<@s.url value="/browse/creators/${user.id?c}"/>">${user.properName}</a></td>
                <td> <#if user.email?has_content>${user.email}</#if></td>
                <td> <#if user.dateCreated?has_content>
						<@view.shortDate user.dateCreated?datetime true />
			</#if></td>
            </tr>
            <tr>
                <td colspan=3>
                    <div style="padding-left:25px">${user.contributorReason!""}</div>
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
