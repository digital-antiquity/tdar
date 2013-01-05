<#escape _untrusted as _untrusted?html>

<#macro accountInfoForm>
    <@s.hidden name="invoiceId" />    
    
    <#if billingAdmin>
    	<b>allow user to change owner of account</b>
    </#if>
    <h3>Who can charge to this account </h3>
    <@edit.listMemberUsers />
    
    <@edit.submit fileReminder=false />

</#macro>

</#escape>