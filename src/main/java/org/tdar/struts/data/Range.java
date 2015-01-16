package org.tdar.struts.data;

import org.tdar.core.bean.Validatable;

public interface Range<T> extends Validatable {

    T getStart();

    void setStart(T start);

    T getEnd();

    void setEnd(T end);

    boolean isInitialized();

}