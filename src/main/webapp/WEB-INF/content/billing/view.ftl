<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/resource/common.ftl" as common>

<head>
<title>Your account ${account.name!"Your Account"}</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@view.toolbar "${account.urlNamespace}" "view">
  <@view.makeLink "cart" "add" "add invoice" "add" "" false false />
	<#if administrator>
  <@view.makeLink "billing" "updateQuotas?id=${account.id?c}" "Reset Totals" "add" "" false false />
	</#if>
</@view.toolbar>
<h1>${account.name!"Your account"} <#if accountGroup?has_content><span>${accountGroup.name}</span></#if></h1>

<#if account.description?has_content>
<p>${account.description!""}</p>
</#if>

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
   <b>n/a</b>
 </#if>
 </td>
</tr>
<tr>
 <th>Space</th>
 <td> ${account.spaceUsedInMb} mb</td><td>
 <#if billingActivityModel.countingSpace>
 ${account.availableSpaceInMb} mb
 <#else>
   <b>n/a</b>
 </#if>
 </td>
</tr>
 <tr>
 <th>Resources</th>
 <td> ${account.resourcesUsed}</td><td>
 <#if billingActivityModel.countingResources>
 ${account.availableResources}
 <#else>
   <b>n/a</b>
 </#if>
 </td>
</tr>
</table>
 
<h3>Invoices</h3>
<table class="tableFormat table">
    <tr>
        <th>name</th>
        <th>owner</th>
		<th>files</th>
		<th>space</th>
		<th>resources</th>
        <th>total</th>
    </tr>
<#list account.invoices as invoice>
	<#assign extraClass=""/>
	<#if invoice.transactionStatus.invalid>
		<#assign extraClass="strikethrough" />
	</#if>
    <tr class="${extraClass}">
        <td><a href="<@s.url value="/cart/${invoice.id?c}" />"/>${invoice.dateCreated}</a></td>
        <td>
            ${invoice.owner.properName}
            <#if invoice.proxy>
                c/o ${invoice.transactedBy.properName}
            </#if>
        </td>
        <td> ${invoice.totalNumberOfFiles}</td>
        <td> ${invoice.totalSpaceInMb}</td>
        <td> ${invoice.totalResources}</td>
        <td> <#if invoice.proxy && !billingManager>n/a<#else>$${invoice.total!0}</#if></td>
    </tr>
</#list>
</table>


<#if editor>
	<h3>Coupon Codes</h3>
	<table class="tableFormat table">
	    <tr>
			<th>files</th>
			<th>space</th>
			<th>expires</th>
	        <th>code</th>
	    </tr>
	<#list account.coupons as coupon>
		<#assign extraClass=""/>
	    <tr class="">
	        <td>${coupon.numberOfFiles}</td>
	        <td>${coupon.numberOfMb}</td>
	        <td>${coupon.dateExpires}</td>
	        <td>${coupon.code}</td>
	    </tr>
	</#list>
	</table>
</#if>

<@s.form name="couponForm" action="create-code" cssClass="form-horizontal">
    <@s.hidden name="id" value="${account.id?c!-1}" />    
	<@s.textfield name="numberOfFiles" cssClass="integer" label="Number of Files"/>
	<@s.textfield name="numberOfMb" cssClass="integer" label="Number of MB"/>
	<@s.textfield name="exipres" cssClass="date" label="Date Expires"/>
	<@s.submit name="_tdar.submit" value="Create Coupon" />
</@s.form>

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
        <th>Files</th>
        <th>Space (MB)</th>
    </tr>
<#list resources as resource>
	<#assign stat = ""/>
	<#if resource.status == 'FLAGGED_ACCOUNT_BALANCE'>
	<#assign stat = "error"/>
	</#if>
<tr class="${stat}">
    <td>${resource.id?c}</td>
	<td>${resource.status.label}</td>
    <td><a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></td>
    <td>${resource.resourceType.label}</td>
    <td>${resource.filesUsed}</td>
    <td>${resource.spaceUsedInMb}</td>
</tr>
</#list>
</table>

</body>
</#escape>