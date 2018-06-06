<#import "email-macro.ftl" as mail /> 

<@mail.content>
${date?datetime} -- ${invoice.total?c}
<br/>
<b>The system created and processed a new invoice:</b>
Invoice ID:  ${invoice.id!?c}<br/>
Invoice Owner: ${invoice.owner}<br/>
Transacted by: ${invoice.transactedBy}<br/>
Files Requested: ${invoice.numberOfFiles!0}<br/>
Space Requested: ${invoice.numberOfMb!0} MB<br/>
Cost: $${invoice.total}<br/>
<#if invoice.coupon?has_content>Coupon: ${invoice.coupon.code}<br/></#if>

Transaction Status ${invoice.transactionStatus}
</@mail.content>