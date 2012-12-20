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
<@view.toolbar "${account.urlNamespace}" "view" />

<h1>${account.name!"Your account"} <#if accountGroup?has_content><span>${accountGroup.name}</span></#if></h1>

<p>${account.description!""}</p>

<h3>Overall Usage</h3>
<table class="tableFormat table">
<tr>
 <th></th>
 <th>Used</th>
 <th>Available</th>
</tr>
<tr>
 <th>Files</th>
 <td> ${account.filesUsed}</td><td>
 <#if billingActivityModel.countingFiles>
 	${account.availableNumberOfFiles}
 <#else>
   <b>Unlimited</b>
 </#if>
 </td>
</tr>
<tr>
 <th>Space</th>
 <td> ${account.spaceUsedInMb} mb</td><td>
 <#if billingActivityModel.countingSpace>
 ${account.availableSpaceInMb} mb
 <#else>
   <b>Unlimited</b>
 </#if>
 </td>
</tr>
 <tr>
 <th>Resources</th>
 <td> ${account.resourcesUsed}</td><td>
 <#if billingActivityModel.countingResources>
 ${account.availableResources}
 <#else>
   <b>Unlimited</b>
 </#if>
 </td>
</tr>
</table>
 
<h3>Invoices</h3>
<table class="tableFormat table">
    <tr>
        <th>name</th>
		<th>files</th>
		<th>space</th>
		<th>resources</th>
        <th>total</th>
    </tr>
<#list account.invoices as invoice>
    <tr>
        <td><a href="<@s.url value="/cart/${invoice.id?c}" />"/>${invoice.dateCreated}</a></td>
        <td> ${invoice.totalNumberOfFiles}</td>
        <td> ${invoice.totalSpaceInMb}</td>
        <td> ${invoice.totalResources}</td>
        <td> $${invoice.total!0}</td>
    </tr>
</#list>
</table>

<h3>Users who can charge to this account</h3>
<table class="tableFormat table">
    <tr>
        <th>name</th><th>email</th>
    </tr>
    <tr>
    	<td>${account.owner.properName}</td><td>${account.owner.email}</td>
	</tr>
<#list account.authorizedMembers as member>
<tr>
    <td><a href="<@s.url value="/browse/creators/${member.id?c}"/>">${member.properName}</a></td><td>${member.email}</td>
</tr>
</#list>
</table>

<h3>Resources associated with this account</h3>
<table class="tableFormat table">
    <tr>
        <th>Id</th>
        <th>Status</th>
        <th>Name</th>
        <th>Resource Type</th>
    </tr>
<#list account.resources as resource>
<tr>
    <td>${resource.id?c}</td>
	<td>${resource.status.label}</td>
    <td><a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></td>
    <td>${resource.resourceType.label}</td>
</tr>
</#list>
</table>

</body>
</#escape>