<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>

<head>
    <title>${account.name!"Your Account"}</title>
    <meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>

	<h1>${account.name!"Your account"} <#if accountGroup?has_content><span>${accountGroup.name}</span></#if></h1>
    <@nav.billingToolbar "${account.urlNamespace}" "view"/>

    <@view.pageStatusCallout />

    <#if account.description?has_content>
    <p>${account.description!""}</p>
    </#if>

<h2>Overall Usage</h2>
<table class="table table-sm table-striped"">
    <tr>
        <th></th>
        <th>Used</th>
        <th>Available</th>
    </tr>
    <tr>
        <th>Files</th>
        <td class="filesused">${account.filesUsed}</td>
        <td>
            <#if billingActivityModel.countingFiles>
            ${account.availableNumberOfFiles}
            <#else>
                <b>n/a</b>
            </#if>
        </td>
    </tr>
    <tr>
        <th>Space</th>
        <td class="spaceused">${account.spaceUsedInMb} mb</td>
        <td>
            <#if billingActivityModel.countingSpace>
            ${account.availableSpaceInMb} mb
            <#else>
                <b>n/a</b>
            </#if>
        </td>
    </tr>
    <tr>
        <th>Resources</th>
        <td> ${account.resourcesUsed}</td>
        <td>
            <#if billingActivityModel.countingResources>
            ${account.availableResources}
            <#else>
                <b>n/a</b>
            </#if>
        </td>
    </tr>
</table>

<h2>Invoices</h2>
<table class="table table-sm table-striped"">
    <tr>
        <th>name</th>
        <th>owner</th>
        <th>files</th>
        <th>space</th>
        <th>resources</th>
        <th>total</th>
    </tr>
    <#list invoices as invoice>
        <#assign extraClass=""/>
        <#if invoice.transactionStatus.invalid>
            <#assign extraClass="strikethrough" />
        </#if>
        <tr class="${extraClass}">
            <td><a href="<@s.url value="/cart/${invoice.id?c}" />">${invoice.dateCreated}</a></td>
            <td>
                <#if (invoice.owner.id)??>
                <a href="<@s.url value="/browse/creators/${invoice.owner.id?c}"/>">${invoice.owner.properName}</a>
                <#else>
                    <em>not assigned</em>
                </#if>
                <#if invoice.proxy && invoice.transactedBy?has_content >
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


<#if (account.coupons?has_content && account.coupons?size > 0)>
<h2>Voucher Codes</h2>
<table class="table table-sm table-striped"">
    <tr>
        <th>files</th>
        <th>space</th>
        <th>expires</th>
        <th>code</th>
        <th>redeemed</th>
        <th>email code</th>
    </tr>
    <#list coupons?sort_by("dateCreated")?reverse as coupon>
        <#assign extraClass=""/>
        <#assign sentence>The following voucher code is good for up
            to <#if (coupon.numberOfFiles?has_content && coupon.numberOfFiles > 0)>${coupon.numberOfFiles} file<#if (coupon.numberOfFiles > 1)>
            s</#if><#else>${coupon.numberOfMb} MB</#if>.</#assign>
        <#assign suffix = "?cc=${authenticatedUser.email}&subject=tDAR%20Voucher&body=${sentence?url}%0A%0A${coupon.code!''?upper_case}" />
        <#if coupon.dateRedeemed?has_content>
            <#assign extraClass="strikethrough" />
        </#if>
        <tr class="${extraClass}">
            <td>${coupon.numberOfFiles}</td>
            <td>${coupon.numberOfMb}</td>
            <td>${coupon.dateExpires}</td>
            <td class="voucherCode">${coupon.code!''?upper_case}</td>
            <td><#if coupon.dateRedeemed?has_content>${coupon.dateRedeemed} <#if coupon.user?has_content>(${coupon.user.properName})</#if></#if></td>
            <td><#if !coupon.dateRedeemed?has_content><a href="mailto:${authenticatedUser.email}?${suffix}">send via email</a></#if></td>
        </tr>
    </#list>
</table>
</#if>
<div class="card">
<div class="card-body">
<h2 class="card-title"> Create Voucher</h2>
	<p>Voucher codes can be used to allow another tDAR user to use files or space without providing them full access to this account.  Simply create a voucher below by specifying either the number of MB or files <b> To redeem a voucher, please go <a href="<@s.url value="/cart/add" />">here</a></b></p>
    <@s.form name="couponForm" action="create-code" cssClass="form-horizontal">
        <div class="row">
            <div class="col-4">
	            <@s.hidden name="id" value="${account.id?c!-1}" />
	            <#if editor>
    	            <@s.select name="quantity" list="{1,5,10,25,50,100}" value="1" label="Quantity" cssClass="input-small"/>
                <#else>
		            <@s.hidden name="quantity" value="1" />
                </#if>
                
            </div>
                <div class="col-4">
                    <div class="form-group">
                <label class="col-form-label">Date Expires</label>
                    <div class="input-group">
                      <input name="expires" class="input-small form-control datepicker" data-date-format="mm/dd/yy" value="${expires?string["MM/dd/yyyy"]}" />
                      <span class="input-group-text"><i class="far fa-calendar-alt"></i></span>
                    </div>
                </div>
                </div>

            <div class="col-2">
                <@s.textfield name="numberOfFiles" cssClass="integer couponFilesOrSpace" label="Number of Files"  value=""/>
            </div>
            <#if accessionFeeEnabled && editor>
            <div class="col-2">
                <div class="form-group">
                <@s.checkbox name="accessionFeeWaived" id="cbAccessionFeeWaived" cssClass="" label="Waive Fees" />
                </div>
            </div>
            </#if>
        </div>
        <div class="row">
            <div class="col-8">
                <div class="control-group">
                    <div class="controls">
                        <@s.submit name="_tdar.submit" value="Create Voucher" cssClass="button submit-btn btn" />
                    </div>
                </div>
            </div>
        </div>
    </@s.form>
</div>
</div>

<h2>Who can use this account</h2>
<table class="table table-sm table-striped" table-bordered">
    <#list account.authorizedUsers as member>
        <tr>
            <td><a href="<@s.url value="/browse/creators/${member.user.id?c}"/>">${member.user.properName}</a></td>
            <td>${member.user.email!""}</td>
            <td>${member.generalPermission.label}</td>
        </tr>
    </#list>
</table>

<#if editor>
<h2>Account File Settings</h2>
<p>
    <b>Files Expire After:</b> ${account.daysFilesExpireAfter}<br/>
    <b>Full Service Enabled:</b> ${account.fullService?c}<br/>
    <b>Initial Review Phase:</b> ${account.initialReview?c}<br/>
    <b>External Review Phase:</b> ${account.externalReview?c}<br/>
    
</#if>

<h2>Resources associated with this account</h2>
<table class="table table-sm table-striped"">
      <thead class="thead-dark">

    <tr>
        <th>Id</th>
        <th>Date Created</th>
        <th>Created By</th>
        <th>Date Updated</th>
        <th>Status</th>
        <th>Name</th>
        <th>Resource Type</th>
        <th>Files</th>
        <th>Space (MB)</th>
    </tr>
      <thead class="thead-dark">

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
            <td>${resource.dateCreated}</td>
            <td>${resource.submitter.properName}</td>
            <td>${resource.dateUpdated}</td>
            <td>${resource.status.label}</td>
            <td><a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}"/>">${resource.title}</a></td>
            <td>${resource.resourceType.label}</td>
            <td>${resource.filesUsed}</td>
            <td>${resource.spaceUsedInMb}</td>
        </tr>
        </#list>
    </tbody>
</table>

<table class="table table-sm table-striped">
  <thead class="thead-dark">

    <tr> <th>Date</th><th>Files Used</th><th>Space Used (in bytes)</th><th>Resources Used</th></tr>
    <#list account.usageHistory as history>
    <tr>
        <td>${history.date}</td>
        <td>${history.filesUsed}</td>
        <td>${history.spaceUsedInMb}</td>
        <td>${history.resourcesUsed}</td>
    </tr>
    </#list>
</thead>
<tbody>
</tbody>
</table>

<script>
    //FIXME: replace with declaritive implementation e.g <input type="text" name="expiration"  data-datepicker  data-dateformat="m/d/y">
    //FIXME: validation errors are ugly due to bootstrap layout issues in form.  Better than no validation, but need to fix.
    $(function() {
        $('.datepicker').datepicker().on('changeDate', function(ev){
            $(ev.target).datepicker('hide');
        });
        //register datepickers and validation rules
        $("#create-code")
            .end().validate({
                    errorClass: "text-error",
                    rules: {
                        numberOfFiles: {
                            required: function(el) {
                                return $.trim($("#create-code_numberOfMb").val()) < 1
                            }
                        }
                    },
                    messages: {
                        numberOfFiles: {
                            required:"Specify a value for Files or MB",
                        	couponFilesOrSpace : "choose files or space"
                        },
                        numberOfMb: {
                        	couponFilesOrSpace : "choose files or space"
                    	}
                    }
        });
    });
</script>
</body>
</#escape>
