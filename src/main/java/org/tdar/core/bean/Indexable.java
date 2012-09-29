package org.tdar.core.bean;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.lucene.search.Explanation;

/*     
 * The Indexable interface is way to ensure that certain additional info is available to the search interface.
 * This includes, the score and the explanation. for Lucene
 */

public interface Indexable extends Persistable {

    /**
     * The Lucene Scoring for the relevance of the item
     * @param score
     */
    public void setScore(Float score);

    @Transient
    @XmlTransient
    public Float getScore();

    @Transient
    @XmlTransient
    public Explanation getExplanation();

    
    public Long getId();
    
    /**
     * The Lucene Explanation for why the item was found
     * @param explanation
     */
    public void setExplanation(Explanation ex);

}
