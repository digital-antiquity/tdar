package org.tdar.core.bean.collection;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;

@Entity
@DiscriminatorValue(value = "RIGHTS")
@Immutable
public  class RightsBasedResourceCollection extends ResourceCollection {

    private static final long serialVersionUID = -7452202939323561883L;



}
