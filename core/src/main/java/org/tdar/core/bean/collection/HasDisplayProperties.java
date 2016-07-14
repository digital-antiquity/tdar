package org.tdar.core.bean.collection;

import org.tdar.core.bean.HasName;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Addressable;

public interface HasDisplayProperties extends OaiDcProvider, Sortable, HasName, Slugable, Addressable, Validatable {

    public CollectionDisplayProperties getProperties();

    public void setProperties(CollectionDisplayProperties properties);

    public default String getAllFieldSearch() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTitle()).append(" ").append(getDescription()).append(" ");
        return sb.toString();
    }

    public void setSortBy(SortOption option);

    public void setName(String name);

    public void setDescription(String descr);
    
    public boolean isHidden();

    public TdarUser getOwner();

    public void setHidden(boolean b);
}
