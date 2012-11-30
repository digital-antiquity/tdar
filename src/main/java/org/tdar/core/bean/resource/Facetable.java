package org.tdar.core.bean.resource;

public  interface  Facetable<F extends Facetable> {

    public Integer getCount();
    
    public void setCount(Integer count);
    
    public String getLuceneFieldName();
    
    public F getValueOf(String val);
}
