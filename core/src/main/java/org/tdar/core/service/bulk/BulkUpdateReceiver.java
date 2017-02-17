package org.tdar.core.service.bulk;

import org.tdar.core.bean.AsyncUpdateReceiver.DefaultReceiver;

public class BulkUpdateReceiver extends DefaultReceiver {

    private Long collectionId;

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }
}
