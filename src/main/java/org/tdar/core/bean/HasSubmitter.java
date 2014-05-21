package org.tdar.core.bean;

import org.tdar.core.bean.entity.Person;

public interface HasSubmitter extends Persistable {

    public Person getSubmitter();
}
