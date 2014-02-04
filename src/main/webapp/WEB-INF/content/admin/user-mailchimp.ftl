<#escape _untrusted as _untrusted?html >
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "admin-common.ftl" as admin>
<head>
<title>User Info Pages (mailchimp)</title>
</head>

<@admin.header />

<h3>Users</h3>
<table class="table tableFormat">
	<thead>
		<tr>
			<th>Username</th>
			<th>Email Address</th>
			<th>First Name</th>
			<th>Last Name</th>
			<th>Institution</th>
			<th>User</th>
			<th>Contributor</th>
			<th>tDAR ID</th>
		</tr>
	</thead>
   <#list recentUsers as user>
   <#if !user.contributor>
	<tr>
		<td>${user.username}</td>
		<td>${user.email}</td>
		<td>${user.firstName}</td>
		<td>${user.lastName}</td>
		<td>${user.institutionName!""}</td>
		<td>TRUE</td>
		<td>FALSE</td>
		<td>${user.id?c}</td>
	</tr>
	</#if>
	</#list>
</table>

<h3>Contributors</h3>
<table class="table tableFormat">
	<thead>
		<tr>
			<th>Username</th>
			<th>Email Address</th>
			<th>First Name</th>
			<th>Last Name</th>
			<th>Institution</th>
			<th>User</th>
			<th>Contributor</th>
			<th>tDAR ID</th>
		</tr>
	</thead>
   <#list recentUsers as user>
   <#if user.contributor>
	<tr>
		<td>${user.username}</td>
		<td>${user.email}</td>
		<td>${user.firstName}</td>
		<td>${user.lastName}</td>
		<td>${user.institutionName!""}</td>
		<td>TRUE</td>
		<td>TRUE</td>
		<td>${user.id?c}</td>
	</tr>
	</#if>
	</#list>
</table>
</#escape>
