package org.tdar.core.bean.collection;

public interface HasParent<C extends HierarchicalCollection> {

    abstract C getParent();
}
