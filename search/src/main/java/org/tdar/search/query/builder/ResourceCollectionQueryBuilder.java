package org.tdar.search.query.builder;

import org.tdar.search.index.LookupSource;
import org.tdar.search.service.CoreNames;

public class ResourceCollectionQueryBuilder extends QueryBuilder implements HasCreator {

    private boolean creatorCreatedEmphasized;

    public ResourceCollectionQueryBuilder() {
        setTypeLimit(LookupSource.COLLECTION.name());
    }

    @Override
    public String getCoreName() {
        return CoreNames.RESOURCES;
    }

    public boolean isCreatorCreatedEmphasized() {
        return creatorCreatedEmphasized;
    }

    public void setCreatorCreatedEmphasized(boolean creatorCreatedEmphasized) {
        this.creatorCreatedEmphasized = creatorCreatedEmphasized;
    }
}
