package org.tdar.core.bean.resource;

/**
 * Abstraction to represent something that can be faceted. This allows us to store data in enums.
 * @author abrin
 *
 * @param <F>
 */
public interface Facetable<F extends Facetable<?>> {

    public Integer getCount();

    public void setCount(Integer count);

    public String getLuceneFieldName();

    public F getValueOf(String val);
}
