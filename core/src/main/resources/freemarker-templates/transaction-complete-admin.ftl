<#import "email-macro.ftl" as mail /> 

<@mail.content>
${date?datetime} -- ${invoice.total?c}

The system created and processed a new invoice:
Invoice ID:  ${invoice.id!?c}
Invoice Owner: ${invoice.owner}
Transacted by: ${invoice.transactedBy}
Files Requested: ${invoice.numberOfFiles!0}
Space Requested: ${invoice.numberOfMb!0} MB
Cost: $${invoice.total}
<#if invoice.coupon?has_content>Coupon: ${invoice.coupon.code}</#if>

Transaction Status ${invoice.transactionStatus}
</@mail.content>