<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/common.ftl" as common>

<head>
<title>Your account ${account.name}</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<h1>${account.name!"Your account"} <#if accountGroup?has_content><span>${accountGroup.name}</span></#if></h1>
<p>${account.description}</p>

<h3>Invoices</h3>
<table class="tableFormat">
	<tr>
		<th>name</th><th>total</th>
	</tr>
<#list account.invoices as invoice>
	<tr>
		<td><a href="<@s.url value="/cart/${invoice.id?c}" />"/>${invoice.dateCreated}</a></td>
		<td> ${invoice.total!0}</td>
	</tr>
</#list>
</table>

<h3>Users who can charge to this account</h3>
<table class="tableFormat">
	<tr>
		<th>name</th><th>email</th>
	</tr>
<#list account.authorizedMembers as member>
<tr>
	<td><a href="<@s.url value="/browse/creators/${member.id?c}"/>">${member.properName}</a></td><td>${member.email}</td>
</tr>
</#list>
</table>

<h3>Resources associated with this account</h3>
<table class="tableFormat">
	<tr>
		<th>Resource Type</th><th>Id</th><th>Name</th>
	</tr>
<#list account.resources as resource>
<tr>
	<td>${resource.resourceType.label}</td>
	<td>${resource.id}</td>
	<td><a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></td>
</tr>
</#list>
</table>

</body>
</#escape>