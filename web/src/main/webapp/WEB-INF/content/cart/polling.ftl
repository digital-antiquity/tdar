<#import "/WEB-INF/content/cart/common-invoice.ftl" as invoicecommon >
<#escape _untrusted as _untrusted?html>
<h1>Processing Payment</h1>

<div class="row" >
    <div class="col-8" style="height:450px">
        <div class="cartpane" style="height:420px">
            <h3>Instructions</h3>
            <p>
                <b>Do not close this window</b>.
                tDAR uses an external payment gateway through Arizona State University called NelNet. Please use the NelNet forms to complete your tDAR payment.
                Print and/or store a copy of your receipt for your records.
                <!-- Once your payment has been processed, you will receive a confirmation email from ASU. -->
            </p>

            <p>
                Once you have completed your payment, this window will take you to the "Select Account" page. You will then be to
                create and manage your Billing Accounts.
            </p>

            <p>
                Once your payment has been <i>successfully completed</i> you will be able to start creating resources in tDAR.
            </p>

            <h3>Invoice Information</h3>
            <@invoicecommon.printSubtotal invoice />

        </div>
    </div>

    <div class="col-4">
        <img alt="nelnet exmaple" src="<@s.url value="/includes/nellnet_screenshot.png"/>" title="Nellnet Screenshot"
             class="img-polaroid responsive-image">
    </div>
</div>


<div class="row">
    <div class="col-12">
        <span class="badge pull-right" id="polling-status" data-invoiceid="${invoice.id?c}"></span>
        <div class="form-actions">
            <a class="button tdar-button" id="btnOpenPaymentWindow" href="<#noescape>${redirectUrl}</#noescape>" target="_blank">Click Here To Begin Payment Process</a>
            <em>Note: opens in a new window</em>.
        </div>
    </div>
</div>

<form>
        <@s.hidden name="invoiceId" value="${invoice.id?c}" />
        <#if accountId?has_content>    
        <@s.hidden name="accountId" value="${accountId?c}" />
        </#if>    
</form>
<div id="async-errors">
</div>
<script>
    $(function () {
        TDAR.pricing.initPolling();
    });
</script>
</#escape>
