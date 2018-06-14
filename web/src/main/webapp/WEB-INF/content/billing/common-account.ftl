<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/common-rights.ftl" as rights>

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

		<@rights.addUsersForRights showDate=false />
        </#if>
        <@edit.submit fileReminder=false />

    </#macro>

</#escape>