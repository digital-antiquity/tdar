${date?datetime} -- ${invoice.total}
An invoice was created and processed for ${invoice.owner} by ${invoice.transactedBy} for a total of $${invoice.total}
Files Requested: ${invoice.numberOfFiles!0}
Space Requested: ${invoice.numberOfMb!0} MB

Transaction Status ${invoice.transactionStatus}