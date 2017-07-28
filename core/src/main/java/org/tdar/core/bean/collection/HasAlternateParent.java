package org.tdar.core.bean.collection;

public interface HasAlternateParent<C extends HierarchicalCollection> {

    abstract C getAlternateParent();
}
