package org.tdar.struts.data;
import org.tdar.core.bean.Validatable;

public interface Range<T> extends Validatable {

    public T getStart();

    public void setStart(T start);

    public T getEnd();

    public void setEnd(T end);
 
    public boolean isInitialized();
    
}