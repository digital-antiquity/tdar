package org.tdar.core.bean.collection;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

@DiscriminatorValue(value = "INTERNAL")
@XmlRootElement(name = "internalCollection")
@Entity
public class InternalCollection extends RightsBasedResourceCollection {

    private static final long serialVersionUID = 2238608996291414672L;

    public InternalCollection() {
        this.setType(CollectionType.INTERNAL);
    }

    @Override
    public boolean isValidForController() {
        return true;
    }
    
}
