<#escape _untrusted as _untrusted?html>
<h1>Processing Payment</h1>

<h2>Instructions</h2>
<div class="row">
    <div class="span8">
        <p>
            tDAR uses an external payment gateway through Arizona State University called NelNet. Please use the NelNet forms to complete your tDAR payment.
            Print and/or store a copy of your receipt for your records. Once your payment has been processed, you will receive a confirmation email from ASU.
        </p>

        <p>
            <b>Do not close this window</b>. Once you have completed your payment, this window will take you to the "Select Account" page. You will then be to
            create and manage your Billing Accounts.
        </p>

        <p>
            Once your payment has been <i>successfully completed</i> you will be able to start creating resources in tDAR.
        </p>
        <br/>

        <p>
            <a class="button" id="btnOpenPaymentWindow" href="<#noescape>${redirectUrl}</#noescape>" target="_blank">click here</a>
            <em>If the payment window does not open automatically</em>.
        </p>

    </div>
    <div class="span4">
        <img alt="nelnet exmaple" src="<@s.url value="/includes/nellnet_screenshot.png"/>" title="Nellnet Screenshot" cssClass="img-polaroid responsive-image"/>
    </div>
</div>
<form>
        <@s.hidden name="invoiceId" value="${invoice.id?c}" />
        <#if accountId?has_content>    
        <@s.hidden name="accountId" value="${accountId?c}" />
        </#if>    
</form>
<div class="" id="polling-status">

</div>
<div id="async-errors">
</div>
<script>
    var TIMEOUT = 1500; //2fps is all we need.
    var pollingUrl = "<@s.url value="/cart/${invoice.id?c}/polling-check"/>";
    $(document).ready(function () {
        TDAR.pricing.initPolling();
    });


</script>
</#escape>
