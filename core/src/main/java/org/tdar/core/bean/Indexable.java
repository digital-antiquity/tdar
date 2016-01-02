package org.tdar.core.bean;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The Indexable interface is way to ensure that certain additional info is available to the search interface.
 * This includes, the score and the explanation. for Lucene
 */

public interface Indexable extends Persistable {

    /**
     * The Lucene Scoring for the relevance of the item
     * 
     * @param score
     */
    void setScore(Float score);

    @Transient
    @XmlTransient
    Float getScore();

    @Override
    Long getId();


}
