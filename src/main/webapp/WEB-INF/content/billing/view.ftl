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
<@nav.toolbar "${account.urlNamespace}" "view">
  <@nav.makeLink "cart" "add" "add invoice" "add" "" false false />
	<#if administrator>
  <@nav.makeLink "billing" "updateQuotas?id=${account.id?c}" "Reset Totals" "add" "" false false />
	</#if>
</@nav.toolbar>
<h1>${account.name!"Your account"} <#if accountGroup?has_content><span>${accountGroup.name}</span></#if></h1>

<@view.pageStatusCallout />


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
        <td><a href="<@s.url value="/cart/${invoice.id?c}" />" >${invoice.dateCreated}</a></td>
        <td>
            <a href="<@s.url value="/browse/creator/${invoice.owner.id}"/>">${invoice.owner.properName}</a>
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


	<h3>Voucher Codes</h3>
	<table class="tableFormat table">
	    <tr>
			<th>files</th>
			<th>space</th>
			<th>expires</th>
	        <th>code</th>
	        <th>redeemed</th>
	        <th>email code</th>
	    </tr>
	<#list account.coupons as coupon>
		<#assign extraClass=""/>
		<#assign sentence>The following voucher code is good for up to <#if (coupon.numberOfFiles?has_content && coupon.numberOfFiles > 0)>${coupon.numberOfFiles} file<#if (coupon.numberOfFiles > 1)>s</#if><#else>${coupon.numberOfMb} MB</#if>.</#assign>
		<#assign suffix = "?cc=${authenticatedUser.email}&subject=tDAR%20Voucher&body=${sentence?url}%0A%0A${coupon.code?upper_case}" />
	    <tr class="">
	        <td>${coupon.numberOfFiles}</td>
	        <td>${coupon.numberOfMb}</td>
	        <td>${coupon.dateExpires}</td>
	        <td class="voucherCode">${coupon.code?upper_case}</td>
	        <td><#if coupon.dateRedeemed?has_content>${coupon.dateRedeemed} <#if coupon.user?has_content>(${coupon.user.properName})</#if></#if></td>
	        <td><#if !coupon.dateRedeemed?has_content><a href="mailto:${authenticatedUser.email}?${suffix}">send via email</a></#if></td>
	    </tr>
	</#list>
	</table>
<div class="well">
<h3> Create Voucher</h3>
<@s.form name="couponForm" action="create-code" cssClass="form-horizontal">
<div class="row">
	<div class="span4">
		<@s.select name="quantity" list="{1,5,10,25,50,100}" value="1" label="Quantity" cssClass="input-small"/>
	    <@s.hidden name="id" value="${account.id?c!-1}" />    
		<@s.textfield name="exipres" cssClass="date  input-small datepicker" label="Date Expires" />
	</div>
	<div class="span4">    
		<@s.textfield name="numberOfFiles" cssClass="integer" label="Number of Files"/>
		<@s.textfield name="numberOfMb" cssClass="integer" label="Number of MB"/>
	</div>
	</div>
	<@s.submit name="_tdar.submit" value="Create Voucher" cssClass="button submit-btn btn" />
</@s.form>
</div>

<h3>Users who can charge to this account</h3>
<table class="tableFormat table">
    <tr>
        <th>name</th><th>email</th>
    </tr>
    <tr>
    	<td>
    	<a href="<@s.url value="/browse/creator/${account.owner.properName}"/>">${account.owner.properName}</a> (owner)</td><td>${account.owner.email}</td>
	</tr>
<#list account.authorizedMembers as member>
<tr>
    <td><a href="<@s.url value="/browse/creators/${member.id?c}"/>">${member.properName}</a></td><td>${member.email}</td>
</tr>
</#list>
</table>

<h3>Resources associated with this account</h3>
<table class="tableFormat table">
<thead>
    <tr>
        <th>Id</th>
        <th>Status</th>
        <th>Name</th>
        <th>Resource Type</th>
        <th>Files</th>
        <th>Space (MB)</th>
    </tr>
    <thead>
    <tbody>
<#list resources as resource>
	<#assign stat = ""/>
	<#if resource.status == 'FLAGGED_ACCOUNT_BALANCE'>
	<#assign stat = "error"/>
	</#if>
	<#if resource.status == 'DELETED'>
		<#assign stat="strikethrough" />
	</#if>

<tr class="${stat} ${resource.status}">
    <td>${resource.id?c}</td>
	<td>${resource.status.label}</td>
    <td><a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></td>
    <td>${resource.resourceType.label}</td>
    <td>${resource.filesUsed}</td>
    <td>${resource.spaceUsedInMb}</td>
</tr>
</#list>
</tbody>
</table>

<script>
$(document).ready(function() {
    $('.datepicker').datepicker({
        dateFormat : 'm/d/y'
    });

 });
</script>
</body>
</#escape>