package org.tdar.core.bean;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;

public interface HasSubmitter extends Persistable {

    public TdarUser getSubmitter();
}
