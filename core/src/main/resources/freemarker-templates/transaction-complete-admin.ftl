${date?datetime} -- ${invoice.total}

The system created and processed a new invoice:
Invoice ID:  ${invoice.id!}
Invoice Owner: ${invoice.owner}
Transacted by: ${invoice.transactedBy}
Files Requested: ${invoice.numberOfFiles!0}
Space Requested: ${invoice.numberOfMb!0} MB
Cost: $${invoice.total}
<#if invoice.coupon?has_content>Coupon: ${invoice.coupon.code}</#if>

Transaction Status ${invoice.transactionStatus}