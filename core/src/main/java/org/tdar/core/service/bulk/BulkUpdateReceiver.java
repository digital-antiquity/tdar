package org.tdar.core.service.bulk;

import org.tdar.core.service.AsynchronousStatus;

public class BulkUpdateReceiver extends AsynchronousStatus {

    static final String BULK_UPLOAD = "BulkUpload";

    public BulkUpdateReceiver() {
        super();
    }
    
    public BulkUpdateReceiver(String key) {
        super(BULK_UPLOAD+key);
    }

    private static final long serialVersionUID = -4922744729683571221L;
    private Long collectionId;

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }
}
