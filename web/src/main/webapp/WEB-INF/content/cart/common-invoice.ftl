<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

    <#macro printInvoice>
    <#local calculatedCost = 0 />
    <#if invoice?? && invoice.calculatedCost??>
        <#local calculatedCost = invoice.calculatedCost /> 
    </#if>
    <!-- FOR testing total:$${calculatedCost!0} -->
    <table class="table  table-invoice">
          <thead class="thead-dark">

            <tr>
                <th>Item</th>
                <th>Quantity</th>
                <th>Cost</th>
                <th>Files</th>
                <th>Space</th>
                <th>Resources</th>
                <th>Subtotal</th>
            </tr>
        </thead>
        <#list invoice.items as item>
            <tr>
                <td>${item.activity.name}</td>
                <td>${item.quantity!0}</td>
                <td><#if invoice.proxy && !billingManager>N/A<#else>$${item.activity.price}</#if></td>
                <td> ${(item.quantity!0) * (item.activity.numberOfFiles!0)} </td>
                <td> ${(item.quantity!0) * (item.activity.numberOfMb!0)} </td>
                <td> ${(item.quantity!0) * (item.activity.numberOfResources!0)}</td>
                <td><#if invoice.proxy && !billingManager>N/A<#else>$${item.subtotal}
                    <!-- for testing: ${item.activity.name}:${item.quantity!0}:$${item.activity.price}:$${item.subtotal}-->
                </#if>
                </td>
            </tr>
        </#list>
        <#if invoice.coupon?has_content>
            <tr>
                <td>Coupon ${invoice.coupon.code}</td>
                <td>1</td>
                <td></td>
                <td>${invoice.coupon.numberOfFiles}</td>
                <td>${invoice.coupon.numberOfMb}</td>
                <td></td>
                <td></td>
            </tr>
        </#if>
        <tfoot>
            <tr>
                <th>Total:</th>
                <th colspan=6 class="invoice-total text-right">$${invoice.calculatedCost!0}</th>
            </tr>
        </tfoot>
    </table>
    </#macro>

<#macro invoiceOwner invoice>
    ${(invoice.owner.properName)!''}
</#macro>


    <#macro printSubtotal invoice>
    <div id="divInvoiceSubtotal" class="invoice-subtotal">
        <#--<h3>Subtotal</h3>-->
        <#--<span class="amt">$${invoice.calculatedCost}</span>-->
        <span class="item-desc">

        <#if ((invoice.numberOfFiles!0) > 0)> ${invoice.numberOfFiles!0 } File<#if ((invoice.numberOfFiles!0) > 1)>s</#if></#if>
        <#if ((invoice.numberOfFiles!0) > 0) && ((invoice.numberOfMb!0) > 0)> / </#if>
        <#if ((invoice.numberOfMb!0) > 0)> ${invoice.numberOfMb!0}mb</#if>

        </span>
        <span class="item-desc status">Status: ${invoice.transactionStatus.label}</span>
        <span class="item-desc">Payment by <@s.text name="${invoice.paymentMethod.localeKey}"/></span>
        <#if invoice.owner??>
        <span class="item-desc">Owner: <@invoiceOwner invoice/></span>
        </#if>
        <#if (billingManager!false)>
            <@s.a href="/cart/continue?invoiceId=${invoice.id?c}"  >Customer Link</@s.a>
            <#--<#noescape><@s.a href="/cart/add?invoice.numberOfFiles=${invoice.numberOfFiles?c}&invoice.numberOfMb=${invoice.numberOfMb!0?c}}&code=${((invoice.coupon.code)!'')}">Customer Link</@s.a></#noescape> -->
        </#if>
    </div>

    </#macro>

    <#macro paymentMethod includePhone=false>
        <@s.radio list="allPaymentMethods" name="invoice.paymentMethod" label="Payment Method"
        listValue="label"    cssClass="transactionType" emptyOption='false' />

        <#if includePhone>
        <div class="typeToggle credit_card invoice manual">
            <@s.textfield name="billingPhone" cssClass="input-xlarge phoneUS  required-visible" label="Billing Phone #" />
        </div>
        </#if>
    <div class="typeToggle invoice">
        <@s.textfield name="invoice.invoiceNumber" cssClass="input-xlarge" label="Invoice #" />
    </div>
    <div class="typeToggle check">
        <@s.textfield name="invoice.invoiceNumber" cssClass="input-xlarge" label="Check #" />
    </div>
    <div class="typeToggle manual">
        <@s.textarea name="invoice.otherReason" cssClass="input-xlarge" label="Other Reason"  cols="80" rows="4" />
    </div>

    </#macro>

    <#macro accountInfoForm>
        <@s.textfield name="account.name" cssClass="input-xlarge" label="Account Name"/>
        <@s.textarea name="account.description" cssClass="input-xlarge" label="Account Description"  cols="80" rows="4" />
        <@s.hidden name="invoiceId" />

        <#if billingAdmin>
        <b>allow user to change owner of account</b>
        </#if>
    <h3>Who can charge to this account </h3>
        <@edit.listMemberUsers />

        <@edit.submit fileReminder=false label="Save" />

    </#macro>

    <#macro proxyNotice>
        <#if invoice.proxy>
        <div class="alert">
            <strong>Proxy Invoice:</strong>
            You are creating this invoice on behalf of ${invoice.owner.properName}.
        </div>
        </#if>
    </#macro>

    <#--Show invoice information that is pertinent only to admins, billing-managers -->
    <#macro invoiceAdminSection invoice>
        <#if (!billingManager && !admin)><#return></#if>
    <div class="admin-well">
        <dl>
            <dt>Invoice Type</dt>
            <dd>${invoice.proxy?string("Proxy Invoice", "Normal Invoice")}</dd>
        </dl>
    </div>
    </#macro>

    <#macro pricingOption label files storage cost id=label?lower_case>
    <div class="col-2 well" id=div${id}>
        <h3><span class="red">$${cost}</span>: ${label}</h3>
        <ul>
            <li>${files}</li>
            <li>${storage}</li>
        </ul>

        <button type="button" class="tdar-button" id="${id}-option">SELECT</button>
    </div>
    </#macro>

    </#escape>