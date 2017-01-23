<#escape _untrusted as _untrusted?html>

    <#macro accountInfoForm hideUsers=false>
        <@s.hidden name="invoiceId" />

        <#if billingManager>
            <#if invoice?? && invoice.proxy>
            <div class="alert-info info">
                creating proxy account for ${invoice.owner.properName}
            </div>
            </#if>
        </#if>
        <#if !hideUsers>
        <h3>Who can charge to this account </h3>
            <@edit.listMemberUsers />
        </#if>
        <@edit.submit fileReminder=false />

    </#macro>

</#escape>