package org.tdar.core.bean;

/**
 * The Indexable interface is way to ensure that certain additional info is available to the search interface.
 * This includes, the score and the explanation. for Lucene
 */

public interface Indexable extends Persistable {

    @Override
    Long getId();


}
